package com.kapil.personalwebsite.entity;

/**
 * Enum representing the category of a blog post used for classification and filtering.
 *
 * @author Kapil Garg
 */
public enum BlogCategory {

    /**
     * Technical blog posts about programming, software development, and technology
     */
    TECHNICAL,

    /**
     * Personal life experiences, thoughts, and reflections
     */
    LIFE,

    /**
     * Career-related content including professional development and industry insights
     */
    CAREER;

    /**
     * Gets the display label for the category.
     *
     * @return the human-readable label for the category
     */
    public String getLabel() {
        return switch (this) {
            case TECHNICAL -> "Technical";
            case LIFE -> "Life";
            case CAREER -> "Career";
        };
    }

    /**
     * Gets the icon for the category.
     *
     * @return the emoji icon for the category
     */
    public String getIcon() {
        return switch (this) {
            case TECHNICAL -> "fas fa-code";
            case LIFE -> "fas fa-heart";
            case CAREER -> "fas fa-briefcase";
        };
    }

    /**
     * Gets the description for the category.
     *
     * @return the description of what this category represents
     */
    public String getDescription() {
        return switch (this) {
            case TECHNICAL -> "Technical posts about programming, software development, and technology";
            case LIFE -> "Personal experiences, thoughts, and life reflections";
            case CAREER -> "Career development, professional insights, and industry trends";
        };
    }

}
