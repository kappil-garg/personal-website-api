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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Filter to block sensitive file probes and common attack paths.
 * Returns 404 for requests to sensitive files like .env, .git, wp-config.php, etc.
 *
 * @author Kapil Garg
 */
@Component
@Order(0)
public class SensitiveFileProbeFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SensitiveFileProbeFilter.class);

    /**
     * Default list of sensitive file patterns to block.
     * These are common targets for reconnaissance attacks.
     */
    private static final List<String> DEFAULT_SENSITIVE_PATTERNS = Arrays.asList(
            "/.env",
            "/.git/",
            "/.git/config",
            "/.gitignore",
            "/wp-config.php",
            "/wp-config.inc.php",
            "/wp-config.bak",
            "/wp-config.txt",
            "/settings.py",
            "/config.php",
            "/config.inc.php",
            "/config.bak",
            "/.htaccess",
            "/.htpasswd",
            "/.ssh/",
            "/.aws/",
            "/.docker/",
            "/docker-compose.yml",
            "/docker-compose.yaml",
            "/.env.local",
            "/.env.production",
            "/.env.development",
            "/.env.test",
            "/.env.backup",
            "/composer.json",
            "/package.json",
            "/yarn.lock",
            "/package-lock.json",
            "/.idea/",
            "/.vscode/",
            "/.DS_Store",
            "/web.config",
            "/application.properties",
            "/application.yml",
            "/application.yaml",
            "/application-dev.properties",
            "/application-prod.properties",
            "/application-local.properties"
    );

    private final List<String> sensitivePatterns;

    /**
     * Constructor that allows configuration of blocked paths via properties.
     * If not configured, uses default patterns.
     *
     * @param blockedPaths comma-separated list of paths to block (optional)
     */
    public SensitiveFileProbeFilter(@Value("${security.blocked-paths}") String blockedPaths) {
        if (StringUtils.hasText(blockedPaths)) {
            this.sensitivePatterns = Arrays.stream(blockedPaths.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .map(String::toLowerCase)
                    .collect(Collectors.toList());
            LOGGER.info("SensitiveFileProbeFilter initialized with {} custom blocked paths", sensitivePatterns.size());
        } else {
            this.sensitivePatterns = DEFAULT_SENSITIVE_PATTERNS.stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toCollection(ArrayList::new));
            LOGGER.info("SensitiveFileProbeFilter initialized with {} default blocked paths", sensitivePatterns.size());
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String requestPath = httpRequest.getRequestURI();
        if (requestPath == null) {
            try {
                chain.doFilter(request, response);
            } catch (IOException | ServletException e) {
                ExceptionUtils.handleClientDisconnect(e, LOGGER, httpRequest);
            }
            return;
        }
        String normalizedPath = requestPath.toLowerCase();
        if (isSensitivePath(normalizedPath)) {
            String clientIp = getClientIp(httpRequest);
            LOGGER.warn("Sensitive file probe blocked - Path: {}, IP: {}, User-Agent: {}",
                    requestPath, clientIp, httpRequest.getHeader("User-Agent"));
            sendNotFoundResponse(httpResponse);
            return;
        }
        try {
            chain.doFilter(request, response);
        } catch (IOException | ServletException e) {
            ExceptionUtils.handleClientDisconnect(e, LOGGER, httpRequest);
        }
    }

    /**
     * Checks if the normalized path matches any sensitive pattern.
     *
     * @param normalizedPath the normalized (lowercase) request path
     * @return true if the path matches a sensitive pattern, false otherwise
     */
    private boolean isSensitivePath(String normalizedPath) {
        return sensitivePatterns.stream().anyMatch(normalizedPath::startsWith);
    }

    /**
     * Sends a 404 Not Found response to prevent information disclosure.
     *
     * @param response the HTTP servlet response
     * @throws IOException if an I/O error occurs
     */
    private void sendNotFoundResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.NOT_FOUND.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String jsonResponse = """
                {
                    "error": "Not Found",
                    "status": 404,
                    "message": "The requested resource was not found"
                }
                """;
        response.getWriter().write(jsonResponse);
    }

    /**
     * Gets the real client IP address from the request.
     * After ForwardedHeaderFilter processes the request, getRemoteAddr() will contain the real IP.
     *
     * @param request the HTTP servlet request
     * @return the client IP address
     */
    private String getClientIp(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        if (remoteAddr != null && !remoteAddr.isEmpty() && !remoteAddr.equals(AppConstants.IPV6_LOCALHOST)) {
            return remoteAddr;
        }
        return "unknown";
    }

}
