package com.kapil.personalwebsite.config;

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

    private static final String VIEW_PATH = "/view";
    private static final String ORIGIN_HEADER = "Origin";
    private static final String REFERER_HEADER = "Referer";
    private static final String OPTIONS_METHOD = "OPTIONS";
    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String BLOGS_PUBLISHED_PATH = "/blogs/published";

    @Value("${api.server-key}")
    private String serverApiKey;

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    // Cache for parsed allowed origins
    private Set<String> allowedOriginsSet;

    @Override
    public void init(FilterConfig filterConfig) {
        if (StringUtils.hasText(allowedOrigins)) {
            allowedOriginsSet = Arrays.stream(allowedOrigins.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toSet());
            LOGGER.info("Initialized OriginVerificationFilter with {} allowed origins", allowedOriginsSet.size());
        } else {
            allowedOriginsSet = Collections.emptySet();
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
            chain.doFilter(request, response);
            return;
        }
        if (OPTIONS_METHOD.equalsIgnoreCase(httpRequest.getMethod())) {
            chain.doFilter(request, response);
            return;
        }
        if (!isRequestAuthorized(httpRequest)) {
            LOGGER.warn("Unauthorized request blocked - Path: {}, Origin: {}, Referer: {}, RemoteAddr: {}",
                    requestPath,
                    httpRequest.getHeader(ORIGIN_HEADER),
                    httpRequest.getHeader(REFERER_HEADER),
                    httpRequest.getRemoteAddr());
            sendForbiddenResponse(httpResponse);
            return;
        }
        chain.doFilter(request, response);
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
        return requestPath.startsWith(BLOGS_PUBLISHED_PATH) ||
                requestPath.contains(BLOGS_PUBLISHED_PATH) ||
                requestPath.endsWith(VIEW_PATH) ||
                requestPath.contains(VIEW_PATH);
    }

    /**
     * Verify if the request is authorized based on Origin, Referer headers or API key.
     *
     * @param request the HTTP servlet request
     * @return true if the request is authorized, false otherwise
     */
    private boolean isRequestAuthorized(HttpServletRequest request) {
        String origin = request.getHeader(ORIGIN_HEADER);
        String referer = request.getHeader(REFERER_HEADER);
        String apiKey = request.getHeader(API_KEY_HEADER);
        if (StringUtils.hasText(apiKey) && StringUtils.hasText(serverApiKey)) {
            return serverApiKey.equals(apiKey);
        }
        if (StringUtils.hasText(origin)) {
            return isAllowedOrigin(origin);
        }
        if (StringUtils.hasText(referer)) {
            String refererOrigin = extractOriginFromReferer(referer);
            if (refererOrigin != null) {
                return isAllowedOrigin(refererOrigin);
            }
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
        response.getWriter().write("""
                {"error":"%s","status":403}
                """.formatted("Request origin not authorized"));
    }

}
