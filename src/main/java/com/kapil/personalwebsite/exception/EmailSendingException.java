package com.kapil.personalwebsite.exception;

/**
 * Exception thrown when email sending fails.
 *
 * @author Kapil Garg
 */
public class EmailSendingException extends RuntimeException {

    public EmailSendingException(String message, Throwable cause) {
        super(message, cause);
    }

}
