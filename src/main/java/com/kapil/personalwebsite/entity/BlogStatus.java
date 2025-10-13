package com.kapil.personalwebsite.entity;

/**
 * Enum representing the status of a blog post.
 *
 * @author Kapil Garg
 */
public enum BlogStatus {

    /**
     * Blog is in draft state - not visible to public
     */
    DRAFT,

    /**
     * Blog is published and visible to public
     */
    PUBLISHED,

    /**
     * Blog is archived - not visible to public but preserved
     */
    ARCHIVED;

    /**
     * Checks if the blog status allows public visibility.
     *
     * @return true if the blog is visible to public, false otherwise
     */
    public boolean isPubliclyVisible() {
        return this == PUBLISHED;
    }

    /**
     * Checks if the blog status allows editing.
     *
     * @return true if the blog can be edited, false otherwise
     */
    public boolean isEditable() {
        return this == DRAFT || this == PUBLISHED;
    }

}
