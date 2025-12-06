package com.kapil.personalwebsite.config;

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
import java.util.concurrent.*;

/**
 * Rate limiting filter for contact form endpoint to prevent spam and abuse.
 * Uses a sliding window algorithm with per-IP address tracking.
 *
 * @author Kapil Garg
 */
@Component
@Order(2)
public class RateLimitFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitFilter.class);

    private static final String POST_METHOD = "POST";
    private static final String CONTACT_PATH = "/contact";

    private final Map<String, RequestWindow> requestCache = new ConcurrentHashMap<>();

    private final ScheduledExecutorService cleanupScheduler = Executors.newScheduledThreadPool(1);

    @Value("${rate.limit.contact.max-requests}")
    private int maxRequests;

    @Value("${rate.limit.contact.window-minutes}")
    private int windowMinutes;

    @Override
    public void init(jakarta.servlet.FilterConfig filterConfig) {
        cleanupScheduler.scheduleAtFixedRate(this::cleanupExpiredEntries, 1, 1, TimeUnit.MINUTES);
        LOGGER.info("RateLimitFilter initialized - Max requests: {} per {} minutes", maxRequests, windowMinutes);
    }

    @Override
    public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        if (!isContactEndpoint(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }
        String clientIp = getClientIp(httpRequest);
        if (!isAllowed(clientIp)) {
            LOGGER.warn("Rate limit exceeded for IP: {} - Path: {}", clientIp, httpRequest.getRequestURI());
            sendRateLimitExceededResponse(httpResponse);
            return;
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        cleanupScheduler.shutdown();
    }

    /**
     * Checks if the request is for the contact endpoint with POST method.
     *
     * @param request the HTTP servlet request
     * @return true if it's a POST request to /contact, false otherwise
     */
    private boolean isContactEndpoint(HttpServletRequest request) {
        return POST_METHOD.equalsIgnoreCase(request.getMethod()) &&
                request.getRequestURI().contains(CONTACT_PATH);
    }

    /**
     * Extracts the client IP address from the request, considering possible proxy headers.
     *
     * @param request the HTTP servlet request
     * @return the client IP address
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    /**
     * Checks if the request from the given IP is allowed under the rate limit.
     *
     * @param clientIp the client IP address
     * @return true if allowed, false if rate limit exceeded
     */
    private boolean isAllowed(String clientIp) {
        long currentTime = Instant.now().toEpochMilli();
        long windowStart = currentTime - (windowMinutes * 60 * 1000L);
        RequestWindow window = requestCache.computeIfAbsent(clientIp, k -> new RequestWindow());
        window.removeOldRequests(windowStart);
        if (window.getRequestCount() >= maxRequests) {
            return false;
        }
        window.addRequest(currentTime);
        return true;
    }

    /**
     * Sends a 429 Too Many Requests response with JSON body.
     *
     * @param response the HTTP servlet response
     * @throws IOException in case of I/O errors
     */
    private void sendRateLimitExceededResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                String.format(
                        "{\"error\":\"Rate limit exceeded\",\"message\":\"Too many requests. Please try again after %d minutes.\",\"retryAfter\":%d}",
                        windowMinutes,
                        windowMinutes * 60
                )
        );
    }

    /**
     * Cleans up expired entries from the request cache.
     * Removes requests older than the defined time window.
     */
    private void cleanupExpiredEntries() {
        long currentTime = Instant.now().toEpochMilli();
        long windowStart = currentTime - (windowMinutes * 60 * 1000L);
        requestCache.entrySet().removeIf(entry -> {
            entry.getValue().removeOldRequests(windowStart);
            return entry.getValue().getRequestCount() == 0;
        });
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
