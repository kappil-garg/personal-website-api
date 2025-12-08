package com.kapil.personalwebsite.exception;

import com.kapil.personalwebsite.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Global exception handler for the application.
 *
 * @author Kapil Garg
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles blog not found exceptions.
     *
     * @param ex      the exception
     * @param request the HTTP request
     * @return a ResponseEntity with error details
     */
    @ExceptionHandler(BlogNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBlogNotFound(BlogNotFoundException ex, HttpServletRequest request) {
        LOGGER.warn("Blog not found: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.NOT_FOUND, "Blog Not Found", ex.getMessage(), request);
    }

    /**
     * Handles blog slug already exists exceptions.
     *
     * @param ex      the exception
     * @param request the HTTP request
     * @return a ResponseEntity with error details
     */
    @ExceptionHandler(BlogSlugAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleBlogSlugAlreadyExists(BlogSlugAlreadyExistsException ex,
                                                                     HttpServletRequest request) {
        LOGGER.warn("Blog slug already exists: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.CONFLICT, "Blog Slug Already Exists", ex.getMessage(), request);
    }

    /**
     * Handles email sending exceptions.
     *
     * @param ex      the exception
     * @param request the HTTP request
     * @return a ResponseEntity with error details
     */
    @ExceptionHandler(EmailSendingException.class)
    public ResponseEntity<ErrorResponse> handleEmailSendingException(EmailSendingException ex,
                                                                     HttpServletRequest request) {
        LOGGER.error("Email sending failed: {}", ex.getMessage(), ex.getCause());
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Email Sending Failed", ex.getMessage(), request);
    }

    /**
     * Handles authentication failures.
     *
     * @param ex      the exception
     * @param request the HTTP request
     * @return a ResponseEntity with error details
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        LOGGER.warn("Authentication failed: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.UNAUTHORIZED, "Authentication Failed", "Invalid credentials", request);
    }

    /**
     * Handles access denied exceptions.
     *
     * @param ex      the exception
     * @param request the HTTP request
     * @return a ResponseEntity with error details
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        LOGGER.warn("Access denied: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.FORBIDDEN, "Access Denied",
                "You don't have permission to access this resource", request);
    }

    /**
     * Handles validation exceptions from request body validation.
     *
     * @param ex      the exception
     * @param request the HTTP request
     * @return a ResponseEntity with error details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex,
                                                                   HttpServletRequest request) {
        LOGGER.warn("Validation error: {}", ex.getMessage());
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " - " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        String fullMessage = "Validation failed: " + errorMessage;
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Validation Error", fullMessage, request);
    }

    /**
     * Handles constraint violation exceptions.
     *
     * @param ex      the exception
     * @param request the HTTP request
     * @return a ResponseEntity with error details
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex,
                                                                   HttpServletRequest request) {
        LOGGER.warn("Constraint violation: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Validation Error", ex.getMessage(), request);
    }

    /**
     * Handles all other unexpected exceptions.
     *
     * @param ex      the exception
     * @param request the HTTP request
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        LOGGER.error("Unexpected error occurred: ", ex);
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred", request);
    }

    /**
     * Helper method to create consistent error responses.
     *
     * @param status  the HTTP status
     * @param error   the error title
     * @param message the error message
     * @param request the HTTP request
     * @return a ResponseEntity with error details
     */
    private ResponseEntity<ErrorResponse> createErrorResponse(HttpStatus status, String error, String message,
                                                              HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(error)
                .message(message)
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(status).body(errorResponse);
    }

}
