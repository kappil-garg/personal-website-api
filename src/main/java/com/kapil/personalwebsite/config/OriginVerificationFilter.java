package com.kapil.personalwebsite.config;

import com.kapil.personalwebsite.util.AppConstants;
import com.kapil.personalwebsite.util.ExceptionUtils;
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
 * Strict origin verification filter for published blog endpoints and view endpoint.
 * Allows requests only from configured allowed origins or with valid API key for server-to-server requests.
 *
 * @author Kapil Garg
 */
@Component
@Order(1)
public class OriginVerificationFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(OriginVerificationFilter.class);

    private final String serverApiKey;
    private final boolean allowNoOriginForSSR;
    private final Set<String> allowedOriginsSet;

    public OriginVerificationFilter(@Value("${api.server-key}") String serverApiKey,
                                    @Value("${cors.allowed-origins}") String allowedOrigins,
                                    @Value("${ssr.allow-no-origin}") boolean allowNoOriginForSSR) {
        this.serverApiKey = serverApiKey;
        this.allowNoOriginForSSR = allowNoOriginForSSR;
        // Initialize allowed origins set
        if (StringUtils.hasText(allowedOrigins)) {
            this.allowedOriginsSet = Arrays.stream(allowedOrigins.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toSet());
            LOGGER.info("OriginVerificationFilter initialized with {} allowed origins, SSR allowed: {}",
                    allowedOriginsSet.size(), allowNoOriginForSSR);
        } else {
            this.allowedOriginsSet = Collections.emptySet();
            LOGGER.warn("No allowed origins configured for OriginVerificationFilter");
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String requestPath = httpRequest.getRequestURI();
        if (!isProtectedEndpoint(requestPath)) {
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
     * Check if the request path is for protected endpoints.
     *
     * @param requestPath the request URI path
     * @return true if the path is protected, false otherwise
     */
    private boolean isProtectedEndpoint(String requestPath) {
        if (requestPath == null) {
            return false;
        }
        return requestPath.startsWith(AppConstants.BLOGS_PUBLISHED_PATH) ||
                requestPath.contains(AppConstants.BLOGS_PUBLISHED_PATH) ||
                requestPath.endsWith(AppConstants.VIEW_PATH) ||
                requestPath.contains(AppConstants.VIEW_PATH);
    }

    /**
     * Verify if the request is authorized based on Origin, Referer headers or API key.
     *
     * @param request the HTTP servlet request
     * @return true if the request is authorized, false otherwise
     */
    private boolean isRequestAuthorized(HttpServletRequest request) {
        String origin = request.getHeader(AppConstants.ORIGIN_HEADER);
        String apiKey = request.getHeader(AppConstants.API_KEY_HEADER);
        String referer = request.getHeader(AppConstants.REFERER_HEADER);
        if (StringUtils.hasText(apiKey) && isValidApiKey(serverApiKey)) {
            return serverApiKey.equals(apiKey);
        }
        if (StringUtils.hasText(origin)) {
            return isAllowedOrigin(origin);
        }
        if (StringUtils.hasText(referer)) {
            String refererOrigin = extractOriginFromReferer(referer);
            if (refererOrigin != null && isAllowedOrigin(refererOrigin)) {
                return true;
            }
        }
        if (allowNoOriginForSSR && isLikelySSRRequest(request)) {
            LOGGER.debug("Allowing SSR request without Origin header from: {}", request.getRemoteAddr());
            return true;
        }
        return false;
    }

    /**
     * Determine if the request is likely an SSR/server request based on absence of Origin header.
     *
     * @param request the HTTP servlet request
     * @return true if this appears to be an SSR/server request
     */
    private boolean isLikelySSRRequest(HttpServletRequest request) {
        String origin = request.getHeader(AppConstants.ORIGIN_HEADER);
        if (!StringUtils.hasText(origin)) {
            LOGGER.debug("Detected server-to-server request (no Origin header) from: {}",
                    request.getRemoteAddr());
            return true;
        }
        return false;
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
        return StringUtils.hasText(apiKey) && !apiKey.equals("null");
    }

}
