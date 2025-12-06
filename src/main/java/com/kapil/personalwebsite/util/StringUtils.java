package com.kapil.personalwebsite.util;

/**
 * Utility class for string manipulation and sanitization operations.
 * Provides methods for sanitizing strings for safe use in various contexts.
 *
 * @author Kapil Garg
 */
public final class StringUtils {

    private StringUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Sanitizes a string for use in email headers to prevent header injection attacks.
     * Removes newlines, carriage returns, and other control characters to ensure safety.
     *
     * @param input the input string to sanitize
     * @return the sanitized string safe for email headers, or empty string if input is null
     */
    public static String sanitizeForEmailHeader(String input) {
        if (input == null) {
            return "";
        }
        return input
                .replace("\r", "")
                .replace("\n", "")
                .replace("\t", " ")
                .replaceAll("[\\x00-\\x1F\\x7F]", "")
                .trim();
    }

    /**
     * Sanitizes a string for use in email body content.
     * Normalizes line endings and removes potentially problematic control characters.
     *
     * @param input the input string to sanitize
     * @return the sanitized string safe for email body, or empty string if input is null
     */
    public static String sanitizeForEmailBody(String input) {
        if (input == null) {
            return "";
        }
        return input
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replaceAll("[\\x00-\\x08\\x0B-\\x0C\\x0E-\\x1F\\x7F]", "")
                .replace("\t", "    ");
    }

    /**
     * Checks if a string is null, empty, or contains only whitespace.
     *
     * @param str the string to check
     * @return true if the string is null, empty, or whitespace only
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Checks if a string is not null, not empty, and contains non-whitespace characters.
     *
     * @param str the string to check
     * @return true if the string has content
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

}
