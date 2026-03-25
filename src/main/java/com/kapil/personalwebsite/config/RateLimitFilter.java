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
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;

/**
 * Rate limiting filter for contact form, blog, and AI endpoints to prevent spam and abuse.
 * Uses a sliding window algorithm with per-IP address tracking.
 *
 * @author Kapil Garg
 */
@Component
@Order(2)
public class RateLimitFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitFilter.class);

    private final List<EndpointRule> rules;
    private final boolean trustProxyHeaders;
    private final Map<String, RequestWindow> requestCache = new ConcurrentHashMap<>();

    public RateLimitFilter(RateLimitProperties properties) {
        this.trustProxyHeaders = properties.trustProxyHeaders();
        this.rules = List.of(
                new EndpointRule(this::isContactPolishEndpoint, AppConstants.ENDPOINT_TYPE_CONTACT_POLISH, toConfig(properties.contactPolish())),
                new EndpointRule(this::isContactEndpoint, AppConstants.ENDPOINT_TYPE_CONTACT, toConfig(properties.contact())),
                new EndpointRule(this::isBlogAskEndpoint, AppConstants.ENDPOINT_TYPE_BLOG_ASK, toConfig(properties.blogAsk())),
                new EndpointRule(this::isPortfolioChatEndpoint, AppConstants.ENDPOINT_TYPE_PORTFOLIO_CHAT, toConfig(properties.portfolioChat())),
                new EndpointRule(this::isBlogEndpoint, AppConstants.ENDPOINT_TYPE_BLOG, toConfig(properties.blog()))
        );
        if (trustProxyHeaders) {
            LOGGER.warn("Proxy header trust is enabled. Ensure your proxy/load balancer strips " +
                    "client-supplied X-Forwarded-For and X-Real-IP headers to prevent rate limit bypass.");
        }
    }

    private static RateLimitConfig toConfig(RateLimitProperties.EndpointLimitConfig cfg) {
        return new RateLimitConfig(cfg.maxRequests(), cfg.windowMinutes());
    }

    @Override
    public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        if (HttpMethod.OPTIONS.matches(httpRequest.getMethod())) {
            chain.doFilter(request, response);
            return;
        }
        String clientIp = getClientIp(httpRequest);
        for (EndpointRule rule : rules) {
            if (rule.matcher().test(httpRequest)) {
                String rateLimitKey = buildRateLimitKey(clientIp, rule.endpointType());
                if (isRateLimitExceeded(rateLimitKey, rule.config())) {
                    LOGGER.warn("Rate limit exceeded for {} endpoint - IP: {} - Path: {}",
                            rule.endpointType(), clientIp, httpRequest.getRequestURI());
                    sendRateLimitExceededResponse(httpResponse, rule.config().windowMinutes());
                    return;
                }
                break; // at most one rule matches per request
            }
        }
        try {
            chain.doFilter(request, response);
        } catch (IOException | ServletException e) {
            ExceptionUtils.handleClientDisconnect(e, LOGGER, httpRequest);
        }
    }

    /**
     * Checks if the request is for the contact polish endpoint with POST method.
     *
     * @param request the HTTP servlet request
     * @return true if it's a POST request to /contact/polish, false otherwise
     */
    private boolean isContactPolishEndpoint(HttpServletRequest request) {
        if (!AppConstants.POST_METHOD.equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String servletPath = request.getServletPath();
        return servletPath != null &&
                (servletPath.equals(AppConstants.CONTACT_POLISH_PATH) || servletPath.equals(AppConstants.CONTACT_POLISH_PATH + "/"));
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
     * Checks if the request is for the blog ask endpoint (POST /blogs/published/.../ask).
     *
     * @param request the HTTP servlet request
     * @return true if it's a POST request to /blogs/published/{slug}/ask, false otherwise
     */
    private boolean isBlogAskEndpoint(HttpServletRequest request) {
        String method = request.getMethod();
        if (!AppConstants.POST_METHOD.equalsIgnoreCase(method) && !HttpMethod.GET.matches(method)) {
            return false;
        }
        String servletPath = request.getServletPath();
        if (servletPath == null || !servletPath.startsWith("/blogs/published/")) {
            return false;
        }
        return servletPath.endsWith("/ask") || servletPath.endsWith("/ask/stream");
    }

    /**
     * Checks if the request is for a blog endpoint.
     *
     * @param request the HTTP servlet request
     * @return true if it's a request to /blogs, false otherwise
     */
    private boolean isBlogEndpoint(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        return servletPath != null && AppConstants.PUBLIC_BLOG_PATHS.stream().anyMatch(servletPath::startsWith);
    }

    /**
     * Checks if the request is for the portfolio chat endpoint with POST method.
     *
     * @param request the HTTP servlet request
     * @return true if it's a POST request to /ai/chat, false otherwise
     */
    private boolean isPortfolioChatEndpoint(HttpServletRequest request) {
        if (!AppConstants.POST_METHOD.equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String servletPath = request.getServletPath();
        return servletPath != null &&
                (servletPath.equals(AppConstants.AI_CHAT_PATH) || servletPath.equals(AppConstants.AI_CHAT_PATH + "/"));
    }

    /**
     * Extracts the client IP address from the request considering proxy headers if configured.
     * Normalizes the IP address (trim and lowercase) to prevent key duplication issues.
     *
     * @param request the HTTP servlet request
     * @return the normalized client IP address
     */
    private String getClientIp(HttpServletRequest request) {
        String clientIp;
        if (!trustProxyHeaders) {
            clientIp = request.getRemoteAddr();
        } else {
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.trim().isEmpty()) {
                clientIp = xForwardedFor.split(",")[0].trim();
            } else {
                String xRealIp = request.getHeader("X-Real-IP");
                clientIp = (xRealIp != null && !xRealIp.trim().isEmpty()) ? xRealIp.trim() : request.getRemoteAddr();
            }
        }
        // Normalize IP address to prevent key duplication (trim and lowercase)
        return clientIp.trim().toLowerCase();
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
     * @param config       the rate limit configuration for the endpoint
     * @return true if rate limit exceeded, false if allowed
     */
    private boolean isRateLimitExceeded(String rateLimitKey, RateLimitConfig config) {
        long currentTime = Instant.now().toEpochMilli();
        long windowStart = currentTime - (config.windowMinutes() * 60_000L);
        RequestWindow window = requestCache.computeIfAbsent(rateLimitKey, k -> new RequestWindow());
        // Synchronize on the window object to ensure thread safety for this IP's request tracking
        synchronized (window) {
            window.removeOldRequests(windowStart);
            if (window.getRequestCount() >= config.maxRequests()) {
                return true;
            }
            window.addRequest(currentTime);
            return false;
        }
    }

    /**
     * Sends a 429 Too Many Requests response with JSON body and Retry-After header.
     *
     * @param response   the HTTP servlet response
     * @param windowMins the time window in minutes
     * @throws IOException in case of I/O errors
     */
    private void sendRateLimitExceededResponse(HttpServletResponse response, int windowMins) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(AppConstants.APPLICATION_JSON);
        response.setCharacterEncoding(AppConstants.UTF_ENCODING);
        long retryAfterSeconds = (long) windowMins * 60;
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        String jsonResponse = """
                {
                    "error": "Rate limit exceeded",
                    "message": "Too many requests. Please try again after %d minutes.",
                    "retryAfter": %d
                }
                """.formatted(windowMins, retryAfterSeconds);
        response.getWriter().write(jsonResponse);
    }

    /**
     * Cleans up expired entries from the request cache called by the scheduled cleanup service.
     */
    public void cleanupExpiredEntries() {
        long currentTime = Instant.now().toEpochMilli();
        long windowStart = currentTime - ((getMaxWindowMinutes() + 1) * 60_000L);
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
     * Calculates the maximum window duration in minutes across all configured rate limits.
     *
     * @return the maximum window duration in minutes
     */
    private long getMaxWindowMinutes() {
        return rules.stream()
                .mapToLong(r -> r.config().windowMinutes())
                .max()
                .orElse(0L);
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

    private record RateLimitConfig(int maxRequests, int windowMinutes) {
    }

    /**
     * Associates an endpoint-matching predicate with its rate-limit configuration and key name.
     * Add a new instance in the constructor to rate-limit an additional endpoint.
     */
    private record EndpointRule(Predicate<HttpServletRequest> matcher, String endpointType, RateLimitConfig config) {
    }

}
