package com.kapil.personalwebsite.ai.vector;

import com.kapil.personalwebsite.util.AppConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits long text into overlapping chunks for embedding and vector retrieval.
 *
 * @author Kapil Garg
 */
public final class TextChunker {

    private TextChunker() {
        throw new UnsupportedOperationException(AppConstants.UTILITY_CLASS_INSTANTIATION_MSG);
    }

    /**
     * Splits the given text into chunks of at most maxChars characters.
     *
     * @param text     non-null text (maybe empty)
     * @param maxChars maximum characters per chunk (&gt; 0)
     * @param overlap  characters repeated between consecutive chunks (&gt;= 0, &lt; maxChars)
     */
    public static List<String> chunk(String text, int maxChars, int overlap) {
        String normalized = text != null ? text.replaceAll("\\s+", " ").trim() : "";
        if (normalized.isEmpty()) {
            return List.of();
        }
        if (maxChars <= 0) {
            throw new IllegalArgumentException("maxChars must be positive");
        }
        if (overlap < 0 || overlap >= maxChars) {
            throw new IllegalArgumentException("overlap must be in [0, maxChars)");
        }
        List<String> parts = new ArrayList<>();
        int start = 0;
        while (start < normalized.length()) {
            int end = Math.min(start + maxChars, normalized.length());
            parts.add(normalized.substring(start, end).trim());
            if (end >= normalized.length()) {
                break;
            }
            start = end - overlap;
            if (start < 0) {
                start = 0;
            }
        }
        return parts.stream().filter(s -> !s.isEmpty()).toList();
    }

}
