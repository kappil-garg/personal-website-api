package com.kapil.personalwebsite.util;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * Utility class for exception handling operations.
 * Provides methods for identifying and handling common exception scenarios.
 *
 * @author Kapil Garg
 */
public final class ExceptionUtils {

    private ExceptionUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Checks if the exception is due to client disconnect (expected behavior).
     * Client disconnects occur when:
     * - User navigates away from a page before request completes
     * - Browser cancels an in-flight request
     * - Network connection is interrupted
     *
     * @param e the exception to check
     * @return true if it's a client disconnect exception, false otherwise
     */
    public static boolean isClientDisconnectException(Exception e) {
        if (e == null) {
            return false;
        }
        String className = e.getClass().getName();
        if (className.contains("ClientAbort") || className.contains("ClientAbortException")) {
            return true;
        }
        String message = e.getMessage();
        if (message == null) {
            return false;
        }
        String lowerMessage = message.toLowerCase();
        return lowerMessage.contains(AppConstants.BROKEN_PIPE) ||
                lowerMessage.contains(AppConstants.CLIENT_ABORT) ||
                lowerMessage.contains(AppConstants.CONNECTION_RESET) ||
                lowerMessage.contains(AppConstants.CONNECTION_ABORTED);
    }

    /**
     * Handles client disconnect exceptions. If not a client disconnect, rethrows the original exception.
     * This method centralizes the handling logic across filters.
     *
     * @param e       the exception to handle
     * @param logger  the logger to use for debug logging
     * @param request the HTTP request (used for logging the request path)
     */
    public static void handleClientDisconnect(Exception e, Logger logger, HttpServletRequest request)
            throws IOException, ServletException {
        if (isClientDisconnectException(e)) {
            if (logger.isDebugEnabled()) {
                String requestPath = request != null ? request.getRequestURI() : "unknown";
                logger.debug("Client disconnected during request: {}", requestPath);
            }
            return;
        }
        if (e instanceof IOException) {
            throw (IOException) e;
        }
        if (e instanceof ServletException) {
            throw (ServletException) e;
        }
    }

}
