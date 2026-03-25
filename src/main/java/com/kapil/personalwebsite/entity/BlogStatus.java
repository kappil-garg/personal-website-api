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
    ARCHIVED

}
