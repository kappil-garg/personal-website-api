package com.kapil.personalwebsite.service.blog;

import com.kapil.personalwebsite.entity.Blog;

import java.util.Optional;

/**
 * Service interface for blog analytics operations.
 * Provides view tracking and analytics functionality.
 *
 * @author Kapil Garg
 */
public interface BlogAnalyticsService {

    /**
     * Increments the view count of a blog.
     *
     * @param id the ID of the blog
     * @return an Optional containing the blog with updated view count if found, or empty if not found
     */
    Optional<Blog> incrementViewCount(String id);

    /**
     * Gets the current view count of a blog.
     *
     * @param id the ID of the blog
     * @return an Optional containing the blog with current view count if found, or empty if not found
     */
    Optional<Blog> getViewCount(String id);

}
