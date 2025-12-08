package com.kapil.personalwebsite.exception;

/**
 * Exception thrown when attempting to create a blog with a slug that already exists.
 *
 * @author Kapil Garg
 */
public class BlogSlugAlreadyExistsException extends RuntimeException {

    public BlogSlugAlreadyExistsException(String message) {
        super(message);
    }

}
