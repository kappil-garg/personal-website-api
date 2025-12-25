package com.kapil.personalwebsite.util;

import java.util.Set;

/**
 * A utility class that holds application-wide constant values.
 * These constants are used throughout the application for various purposes.
 *
 * @author Kapil Garg
 */
public final class AppConstants {

    public static final String POST_METHOD = "POST";
    public static final String UTF_ENCODING = "UTF-8";
    public static final String CONTACT_PATH = "/contact";
    public static final String DATE_FORMAT = "dd-MM-yyyy";
    public static final String APPLICATION_JSON = "application/json";

    public static final String ADMIN_ROLE = "ADMIN";
    public static final String ACCEPT_HEADER = "Accept";
    public static final String ORIGIN_HEADER = "Origin";
    public static final String REFERER_HEADER = "Referer";
    public static final String OPTIONS_METHOD = "OPTIONS";
    public static final String API_KEY_HEADER = "X-API-Key";
    public static final String USER_AGENT_HEADER = "User-Agent";
    public static final String CONTENT_TYPE_HEADER = "Content-Type";
    public static final String AUTHORIZATION_HEADER = "Authorization";

    public static final String BROKEN_PIPE = "broken pipe";
    public static final String CLIENT_ABORT = "clientabort";
    public static final String CONNECTION_RESET = "connection reset";
    public static final String CONNECTION_ABORTED = "connection aborted";

    public static final String EMAIL_NOT_CONFIGURED = "Email service not configured";
    public static final String SENDER_EMAIL_NOT_CONFIGURED = "Sender email not configured";
    public static final String RECIPIENT_EMAIL_NOT_CONFIGURED = "Recipient email not configured";
    public static final String UTILITY_CLASS_INSTANTIATION_MSG = "Utility class cannot be instantiated";

    public static final String MESSAGE_SENT_SUCCESS = "Message sent successfully! I'll get back to you soon.";

    public static final String THANK_YOU_FOR_CONTACTING = "Thank you for contacting me. I will get back to you soon.";

    public static final String ENDPOINT_TYPE_BLOG = "BLOG";
    public static final String ENDPOINT_TYPE_CONTACT = "CONTACT";
    public static final String IPV6_LOCALHOST = "0:0:0:0:0:0:0:1";

    /**
     * Paths that are excluded from origin verification.
     * These endpoints are either monitored externally or protected by other mechanisms.
     */
    public static final Set<String> EXCLUDED_ORIGIN_VERIFICATION_PATHS = Set.of(
            "/actuator/health",
            "/actuator/info"
    );

    /**
     * Paths for public read endpoints (blogs).
     */
    public static final Set<String> PUBLIC_BLOG_PATHS = Set.of(
            "/blogs"
    );

    private AppConstants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

}
