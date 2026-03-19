package com.kapil.personalwebsite.entity;

import lombok.Getter;

/**
 * Enum representing the category of a blog post used for classification and filtering.
 *
 * @author Kapil Garg
 */
@Getter
public enum BlogCategory {

    /**
     * Posts focused on backend engineering, distributed systems, and architecture
     */
    BACKEND_AND_SYSTEMS("Backend & Systems"),

    /**
     * Posts about AI tooling, practical engineering, and implementation patterns
     */
    AI_AND_ENGINEERING("AI & Engineering"),

    /**
     * Career-related content including professional development and growth
     */
    CAREER_AND_GROWTH("Career & Growth"),

    /**
     * Posts covering foundational concepts and continuous learning topics
     */
    LEARNING_AND_FUNDAMENTALS("Learning & Fundamentals"),

    /**
     * Personal reflections, stories, and non-technical experiences
     */
    PERSONAL("Personal");

    private final String label;

    BlogCategory(String label) {
        this.label = label;
    }

    /**
     * Resolves a category from either enum name (e.g. AI_AND_ENGINEERING) or display label (e.g. "AI & Engineering").
     *
     * @param raw stored category value
     * @return resolved enum constant
     */
    public static BlogCategory fromStoredValue(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Blog category cannot be null or blank");
        }
        String value = raw.trim();
        for (BlogCategory category : values()) {
            if (category.name().equalsIgnoreCase(value) || category.getLabel().equalsIgnoreCase(value)) {
                return category;
            }
        }
        String normalized = value
                .replace("&", "AND")
                .replaceAll("[^A-Za-z0-9]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "")
                .toUpperCase();

        return BlogCategory.valueOf(normalized);
    }

}
