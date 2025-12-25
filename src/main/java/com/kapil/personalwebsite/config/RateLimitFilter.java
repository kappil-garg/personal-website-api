package com.kapil.personalwebsite.config;

import com.kapil.personalwebsite.util.AppConstants;
import com.kapil.personalwebsite.util.ExceptionUtils;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Rate limiting filter for contact form and blog endpoints to prevent spam and abuse.
 * Uses a sliding window algorithm with per-IP address tracking.
 *
 * @author Kapil Garg
 */
@Component
@Order(2)
public class RateLimitFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitFilter.class);

    private final int maxRequests;
    private final int windowMinutes;
    private final int blogMaxRequests;
    private final int blogWindowMinutes;
    private final boolean trustProxyHeaders;

    private final Map<String, RequestWindow> requestCache = new ConcurrentHashMap<>();

    public RateLimitFilter(@Value("${rate.limit.contact.max-requests}") int maxRequests,
                           @Value("${rate.limit.contact.window-minutes}") int windowMinutes,
                           @Value("${rate.limit.blog.max-requests}") int blogMaxRequests,
                           @Value("${rate.limit.blog.window-minutes}") int blogWindowMinutes,
                           @Value("${rate.limit.trust-proxy-headers:false}") boolean trustProxyHeaders) {
        this.maxRequests = maxRequests;
        this.windowMinutes = windowMinutes;
        this.blogMaxRequests = blogMaxRequests;
        this.blogWindowMinutes = blogWindowMinutes;
        this.trustProxyHeaders = trustProxyHeaders;
        if (trustProxyHeaders) {
            LOGGER.warn("Proxy header trust is enabled. Ensure your proxy/load balancer strips " +
                    "client-supplied X-Forwarded-For and X-Real-IP headers to prevent rate limit bypass.");
        }
    }

    @Override
    public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        if (isContactEndpoint(httpRequest)) {
            String clientIp = getClientIp(httpRequest);
            String rateLimitKey = buildRateLimitKey(clientIp, AppConstants.ENDPOINT_TYPE_CONTACT);
            if (isRateLimitExceeded(rateLimitKey, maxRequests, windowMinutes)) {
                LOGGER.warn("Rate limit exceeded for contact endpoint - IP: {} - Path: {}", clientIp, httpRequest.getRequestURI());
                sendRateLimitExceededResponse(httpResponse, windowMinutes);
                return;
            }
        } else if (isBlogEndpoint(httpRequest)) {
            String clientIp = getClientIp(httpRequest);
            String rateLimitKey = buildRateLimitKey(clientIp, AppConstants.ENDPOINT_TYPE_BLOG);
            if (isRateLimitExceeded(rateLimitKey, blogMaxRequests, blogWindowMinutes)) {
                LOGGER.warn("Rate limit exceeded for blog endpoint - IP: {} - Path: {}", clientIp, httpRequest.getRequestURI());
                sendRateLimitExceededResponse(httpResponse, blogWindowMinutes);
                return;
            }
        }
        try {
            chain.doFilter(request, response);
        } catch (IOException | ServletException e) {
            ExceptionUtils.handleClientDisconnect(e, LOGGER, httpRequest);
        }
    }

    /**
     * Checks if the request is for the contact endpoint with POST method.
     *
     * @param request the HTTP servlet request
     * @return true if it's a POST request to /contact, false otherwise
     */
    private boolean isContactEndpoint(HttpServletRequest request) {
        if (!AppConstants.POST_METHOD.equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String servletPath = request.getServletPath();
        return servletPath != null &&
                (servletPath.equals(AppConstants.CONTACT_PATH) || servletPath.equals(AppConstants.CONTACT_PATH + "/"));
    }

    /**
     * Checks if the request is for a blog endpoint.
     *
     * @param request the HTTP servlet request
     * @return true if it's a request to /blogs, false otherwise
     */
    private boolean isBlogEndpoint(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        if (servletPath == null) {
            return false;
        }
        return AppConstants.PUBLIC_BLOG_PATHS.stream().anyMatch(servletPath::startsWith);
    }

    /**
     * Extracts the client IP address from the request considering proxy headers if configured.
     *
     * @param request the HTTP servlet request
     * @return the client IP address
     */
    private String getClientIp(HttpServletRequest request) {
        if (!trustProxyHeaders) {
            return request.getRemoteAddr();
        }
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.trim().isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.trim().isEmpty()) {
            return xRealIp.trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Builds a composite rate limit key from IP address and endpoint type.
     * This isolates rate limits per endpoint type (blog vs contact).
     *
     * @param clientIp     the client IP address
     * @param endpointType the endpoint type (BLOG or CONTACT)
     * @return the composite rate limit key
     */
    private String buildRateLimitKey(String clientIp, String endpointType) {
        return clientIp + ":" + endpointType;
    }

    /**
     * Checks if the rate limit has been exceeded for the given rate limit key.
     *
     * @param rateLimitKey the composite rate limit key (IP:ENDPOINT_TYPE)
     * @param maxReqs      the maximum number of requests allowed
     * @param windowMins   the time window in minutes
     * @return true if rate limit exceeded, false if allowed
     */
    private boolean isRateLimitExceeded(String rateLimitKey, int maxReqs, int windowMins) {
        long currentTime = Instant.now().toEpochMilli();
        long windowStart = currentTime - ((long) windowMins * 60 * 1000L);
        RequestWindow window = requestCache.computeIfAbsent(rateLimitKey, k -> new RequestWindow());
        // Synchronize on the window object to ensure thread safety for this IP's request tracking
        synchronized (window) {
            window.removeOldRequests(windowStart);
            if (window.getRequestCount() >= maxReqs) {
                return true;
            }
            window.addRequest(currentTime);
            return false;
        }
    }

    /**
     * Sends a 429 Too Many Requests response with JSON body.
     *
     * @param response   the HTTP servlet response
     * @param windowMins the time window in minutes
     * @throws IOException in case of I/O errors
     */
    private void sendRateLimitExceededResponse(HttpServletResponse response, int windowMins) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(AppConstants.APPLICATION_JSON);
        response.setCharacterEncoding(AppConstants.UTF_ENCODING);
        String jsonResponse = """
                {
                    "error": "Rate limit exceeded",
                    "message": "Too many requests. Please try again after %d minutes.",
                    "retryAfter": %d
                }
                """.formatted(windowMins, ((long) windowMins * 60));
        response.getWriter().write(jsonResponse);
    }

    /**
     * Cleans up expired entries from the request cache.
     * Removes requests older than the defined time windows for both endpoint types.
     */
    public void cleanupExpiredEntries() {
        long currentTime = Instant.now().toEpochMilli();
        long windowStart = currentTime - (((long) Math.max(windowMinutes, blogWindowMinutes) + 1) * 60 * 1000L);
        int beforeSize = requestCache.size();
        requestCache.entrySet().removeIf(entry -> {
            RequestWindow window = entry.getValue();
            synchronized (window) {
                window.removeOldRequests(windowStart);
                return window.getRequestCount() == 0;
            }
        });
        int afterSize = requestCache.size();
        if (beforeSize != afterSize) {
            LOGGER.debug("Cleaned up {} expired rate limit entries. Cache size: {} -> {}",
                    beforeSize - afterSize, beforeSize, afterSize);
        }
    }

    /**
     * Represents a sliding window of requests for a specific IP address.
     */
    private static class RequestWindow {

        private final ConcurrentLinkedQueue<Long> requests = new ConcurrentLinkedQueue<>();

        void addRequest(long timestamp) {
            requests.offer(timestamp);
        }

        void removeOldRequests(long windowStart) {
            while (!requests.isEmpty() && requests.peek() < windowStart) {
                requests.poll();
            }
        }

        int getRequestCount() {
            return requests.size();
        }

    }

}
