package com.kapil.personalwebsite.exception;

/**
 * Checked exception for email service failures.
 *
 * @author Kapil Garg
 */
public class EmailServiceException extends Exception {

    public EmailServiceException(String message) {
        super(message);
    }

    public EmailServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
