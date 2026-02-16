package com.kapil.personalwebsite.config;

import com.kapil.personalwebsite.util.AppConstants;
import com.kapil.personalwebsite.util.ExceptionUtils;
import com.kapil.personalwebsite.util.SecurityStringUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Strict origin verification filter for ALL API endpoints.
 * Protects all endpoints from unauthorized usage by verifying request origin.
 *
 * @author Kapil Garg
 */
@Component
@Order(1)
public class OriginVerificationFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(OriginVerificationFilter.class);

    private final String serverApiKey;
    private final Set<String> allowedOriginsSet;

    public OriginVerificationFilter(@Value("${api.server-key}") String serverApiKey,
                                    @Value("${cors.allowed-origins}") String allowedOrigins) {
        this.serverApiKey = serverApiKey;
        if (StringUtils.hasText(allowedOrigins)) {
            this.allowedOriginsSet = Arrays.stream(allowedOrigins.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toSet());
            LOGGER.info("OriginVerificationFilter initialized with {} allowed origins",
                    allowedOriginsSet.size());
        } else {
            this.allowedOriginsSet = Collections.emptySet();
            LOGGER.warn("No allowed origins configured for OriginVerificationFilter. All non-API key requests will be blocked.");
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String requestPath = httpRequest.getRequestURI();
        if (isExcludedPath(requestPath)) {
            try {
                chain.doFilter(request, response);
            } catch (IOException | ServletException e) {
                ExceptionUtils.handleClientDisconnect(e, LOGGER, httpRequest);
            }
            return;
        }
        if (AppConstants.OPTIONS_METHOD.equalsIgnoreCase(httpRequest.getMethod())) {
            try {
                chain.doFilter(request, response);
            } catch (IOException | ServletException e) {
                ExceptionUtils.handleClientDisconnect(e, LOGGER, httpRequest);
            }
            return;
        }
        if (!isRequestAuthorized(httpRequest)) {
            LOGGER.warn("Unauthorized request blocked - Path: {}, Origin: {}, Referer: {}, RemoteAddr: {}, User-Agent: {}",
                    requestPath,
                    httpRequest.getHeader(AppConstants.ORIGIN_HEADER),
                    httpRequest.getHeader(AppConstants.REFERER_HEADER),
                    httpRequest.getRemoteAddr(),
                    httpRequest.getHeader(AppConstants.USER_AGENT_HEADER));
            sendForbiddenResponse(httpResponse);
            return;
        }
        try {
            chain.doFilter(request, response);
        } catch (IOException | ServletException e) {
            ExceptionUtils.handleClientDisconnect(e, LOGGER, httpRequest);
        }
    }

    /**
     * Check if the request path is excluded from origin verification.
     *
     * @param requestPath the request URI path
     * @return true if the path is excluded, false otherwise
     */
    private boolean isExcludedPath(String requestPath) {
        if (requestPath == null) {
            return false;
        }
        return AppConstants.EXCLUDED_ORIGIN_VERIFICATION_PATHS.stream().anyMatch(requestPath::startsWith);
    }

    /**
     * Verify if the request is authorized based on Origin, Referer headers or API key.
     * Blog endpoints are always authorized as they are public read endpoints.
     *
     * @param request the HTTP servlet request
     * @return true if the request is authorized, false otherwise
     */
    private boolean isRequestAuthorized(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        if (isBlogEndpoint(servletPath)) {
            // Blog endpoints are public - rate limiting provides the real protection
            return true;
        }
        return authorizeOtherEndpoint(request);
    }

    /**
     * Authorize non-blog endpoints based on user authentication, API key, Origin, or Referer headers.
     *
     * @param request the HTTP servlet request
     * @return true if authorized, false otherwise
     */
    private boolean authorizeOtherEndpoint(HttpServletRequest request) {
        if (request.getUserPrincipal() != null || isValidApiKeyProvided(request)) {
            return true;
        }
        String origin = request.getHeader(AppConstants.ORIGIN_HEADER);
        if (StringUtils.hasText(origin) && isAllowedOrigin(origin)) {
            return true;
        }
        String referer = request.getHeader(AppConstants.REFERER_HEADER);
        if (StringUtils.hasText(referer)) {
            String refererOrigin = extractOriginFromReferer(referer);
            return refererOrigin != null && isAllowedOrigin(refererOrigin);
        }
        return false;
    }

    /**
     * Checks if a valid API key is provided in the request.
     *
     * @param request the HTTP servlet request
     * @return true if a valid API key is provided, false otherwise
     */
    private boolean isValidApiKeyProvided(HttpServletRequest request) {
        String apiKey = request.getHeader(AppConstants.API_KEY_HEADER);
        return StringUtils.hasText(apiKey) && isValidApiKey(serverApiKey) &&
                SecurityStringUtils.constantTimeEquals(serverApiKey, apiKey);
    }

    /**
     * Check if the request path is a blog endpoint (public read endpoint).
     *
     * @param servletPath the servlet path
     * @return true if the path is a blog endpoint, false otherwise
     */
    private boolean isBlogEndpoint(String servletPath) {
        if (servletPath == null) {
            return false;
        }
        return AppConstants.PUBLIC_BLOG_PATHS.stream().anyMatch(servletPath::startsWith);
    }

    /**
     * Check if the given origin is in the list of allowed origins.
     *
     * @param origin the origin to check
     * @return true if the origin is allowed, false otherwise
     */
    private boolean isAllowedOrigin(String origin) {
        if (!StringUtils.hasText(origin) || allowedOriginsSet == null || allowedOriginsSet.isEmpty()) {
            return false;
        }
        return allowedOriginsSet.contains(origin);
    }

    /**
     * Extract the origin (scheme + host + port) from the Referer header value.
     *
     * @param referer the Referer header value
     * @return the extracted origin, or null if invalid
     */
    private String extractOriginFromReferer(String referer) {
        try {
            URI uri = URI.create(referer);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            int port = uri.getPort();
            if (scheme == null || host == null) {
                return null;
            }
            return port == -1
                    ? "%s://%s".formatted(scheme, host)
                    : "%s://%s:%d".formatted(scheme, host, port);
        } catch (IllegalArgumentException e) {
            LOGGER.debug("Invalid referer URL format: {}", referer);
            return null;
        }
    }

    /**
     * Send a 403 Forbidden response with a JSON error message.
     *
     * @param response the HTTP servlet response
     * @throws IOException if an I/O error occurs
     */
    private void sendForbiddenResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String jsonResponse = """
                {
                    "error": "%s",
                    "status": 403
                }
                """.formatted("Request origin not authorized");
        response.getWriter().write(jsonResponse);
    }

    /**
     * Validates that the API key is properly configured and not the literal "null" string.
     *
     * @param apiKey the API key to validate
     * @return true if the API key is valid and configured, false otherwise
     */
    private boolean isValidApiKey(String apiKey) {
        return StringUtils.hasText(apiKey) && !"null".equals(apiKey);
    }

}
