package com.kapil.personalwebsite.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standardized API Response wrapper for all API endpoints.
 * Provides consistent response structure across the application.
 *
 * @author Kapil Garg
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime timestamp;
    
    private String path;
    private Integer status;

    /**
     * Creates a successful response with data.
     *
     * @param data the response data
     * @param message the success message
     * @param <T> the type of data
     * @return ApiResponse with success status
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a successful response with data and custom status.
     *
     * @param data the response data
     * @param message the success message
     * @param status the HTTP status code
     * @param <T> the type of data
     * @return ApiResponse with success status
     */
    public static <T> ApiResponse<T> success(T data, String message, Integer status) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .status(status)
                .build();
    }

    /**
     * Creates an error response.
     *
     * @param message the error message
     * @param status the HTTP status code
     * @param <T> the type of data
     * @return ApiResponse with error status
     */
    public static <T> ApiResponse<T> error(String message, Integer status) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .status(status)
                .build();
    }

    /**
     * Creates an error response with path information.
     *
     * @param message the error message
     * @param status the HTTP status code
     * @param path the request path
     * @param <T> the type of data
     * @return ApiResponse with error status
     */
    public static <T> ApiResponse<T> error(String message, Integer status, String path) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .status(status)
                .path(path)
                .build();
    }
    
}
