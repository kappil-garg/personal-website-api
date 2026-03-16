package com.kapil.personalwebsite.ai.util;

import com.kapil.personalwebsite.util.AppConstants;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Utility methods for building AI-friendly text sections from domain data.
 * Kept in the AI package as these helpers are specific to prompt/context construction.
 *
 * @author Kapil Garg
 */
public final class AiTextUtils {

    private AiTextUtils() {
        throw new UnsupportedOperationException(AppConstants.UTILITY_CLASS_INSTANTIATION_MSG);
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

    /**
     * Appends a titled section made of list items to the provided StringBuilder.
     * Each item is formatted using the given lineAppender.
     *
     * @param sb           the StringBuilder to append to
     * @param title        the section title
     * @param items        the list of items
     * @param lineAppender a function that appends a single item's content to the builder
     * @param <T>          the item type
     */
    public static <T> void appendSection(StringBuilder sb, String title, List<T> items,
                                         BiConsumer<StringBuilder, T> lineAppender) {
        if (items == null || items.isEmpty()) {
            return;
        }
        sb.append(title).append(":\n");
        items.forEach(item -> {
            lineAppender.accept(sb, item);
            sb.append("\n");
        });
        sb.append("\n");
    }

}
