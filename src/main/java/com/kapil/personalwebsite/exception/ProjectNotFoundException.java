package com.kapil.personalwebsite.exception;

/**
 * Exception thrown when a project is not found.
 *
 * @author Kapil Garg
 */
public class ProjectNotFoundException extends RuntimeException {

    public ProjectNotFoundException(String message) {
        super(message);
    }

}
