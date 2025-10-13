package com.kapil.personalwebsite.service.blog;

import com.kapil.personalwebsite.entity.Blog;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for public blog operations.
 * Provides read-only access to published blogs for website visitors.
 *
 * @author Kapil Garg
 */
public interface BlogPublicService {

    /**
     * Retrieves all published blogs.
     *
     * @return a list of published blogs ordered by published date (newest first)
     */
    List<Blog> getPublishedBlogs();

    /**
     * Retrieves a published blog by its slug.
     *
     * @param slug the slug of the blog
     * @return an Optional containing the published blog if found, or empty if not found
     */
    Optional<Blog> getPublishedBlogBySlug(String slug);

    /**
     * Retrieves a published blog by its ID.
     *
     * @param id the ID of the blog
     * @return an Optional containing the published blog if found, or empty if not found
     */
    Optional<Blog> getPublishedBlogById(String id);

}
