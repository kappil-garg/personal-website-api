package com.kapil.personalwebsite.util;

/**
 * A utility class that holds application-wide constant values.
 * These constants are used throughout the application for various purposes.
 *
 * @author Kapil Garg
 */
public class AppConstants {

    public static final String BROKEN_PIPE = "broken pipe";
    public static final String CLIENT_ABORT = "clientabort";
    public static final String CONNECTION_RESET = "connection reset";
    public static final String CONNECTION_ABORTED = "connection aborted";

    public static final String POST_METHOD = "POST";
    public static final String UTF_ENCODING = "UTF-8";
    public static final String CONTACT_PATH = "/contact";
    public static final String APPLICATION_JSON = "application/json";

    public static final String VIEW_PATH = "/view";
    public static final String ORIGIN_HEADER = "Origin";
    public static final String REFERER_HEADER = "Referer";
    public static final String OPTIONS_METHOD = "OPTIONS";
    public static final String API_KEY_HEADER = "X-API-Key";
    public static final String USER_AGENT_HEADER = "User-Agent";
    public static final String BLOGS_PUBLISHED_PATH = "/blogs/published";

    public static final String SENDER_EMAIL_NOT_CONFIGURED = "Sender email not configured";
    public static final String RECIPIENT_EMAIL_NOT_CONFIGURED = "Recipient email not configured";
    public static final String JAVA_MAIL_SENDER_NOT_CONFIGURED = "JavaMailSender not configured";

    public static final String MESSAGE_SENT_SUCCESS = "Message sent successfully! I'll get back to you soon.";

    public static final String THANK_YOU_FOR_CONTACTING = "Thank you for contacting me. I will get back to you soon.";

    private AppConstants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

}
