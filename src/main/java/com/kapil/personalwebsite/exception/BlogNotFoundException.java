package com.kapil.personalwebsite.exception;

/**
 * Exception thrown when a blog is not found.
 *
 * @author Kapil Garg
 */
public class BlogNotFoundException extends RuntimeException {

    public BlogNotFoundException(String message) {
        super(message);
    }

    public BlogNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
