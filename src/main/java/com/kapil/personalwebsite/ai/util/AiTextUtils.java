package com.kapil.personalwebsite.ai.util;

import com.kapil.personalwebsite.util.AppConstants;

import java.util.regex.Pattern;

/**
 * Utility methods for building AI-friendly text sections from domain data.
 * Kept in the AI package as these helpers are specific to prompt/context construction.
 *
 * @author Kapil Garg
 */
public final class AiTextUtils {

    private static final Pattern HTML_TAG = Pattern.compile("<[^>]+>");

    private AiTextUtils() {
        throw new UnsupportedOperationException(AppConstants.UTILITY_CLASS_INSTANTIATION_MSG);
    }

    /**
     * Strips HTML tags and collapses whitespace (for blog bodies and rich text fields).
     *
     * @param html the HTML string to strip
     * @return the stripped string
     */
    public static String stripHtmlTags(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }
        String text = HTML_TAG.matcher(html).replaceAll(" ");
        return text.replaceAll("\\s+", " ").trim();
    }

    /**
     * Returns the given value or an empty string if the value is null.
     *
     * @param value the input string
     * @return the value if non-null, otherwise an empty string
     */
    public static String nullSafe(String value) {
        return value != null ? value : "";
    }

}
