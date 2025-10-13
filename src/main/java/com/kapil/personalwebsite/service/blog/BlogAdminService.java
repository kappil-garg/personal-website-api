package com.kapil.personalwebsite.service.blog;

import com.kapil.personalwebsite.entity.Blog;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for blog administration operations.
 * Provides full CRUD operations and blog lifecycle management for content creators.
 *
 * @author Kapil Garg
 */
public interface BlogAdminService {

    /**
     * Retrieves all blogs (including drafts and archived).
     *
     * @return a list of all blogs ordered by creation date (newest first)
     */
    List<Blog> getAllBlogs();

    /**
     * Retrieves a blog by its slug (any status).
     *
     * @param slug the slug of the blog
     * @return an Optional containing the blog if found, or empty if not found
     */
    Optional<Blog> getBlogBySlug(String slug);

    /**
     * Retrieves a blog by its ID (any status).
     *
     * @param id the ID of the blog
     * @return an Optional containing the blog if found, or empty if not found
     */
    Optional<Blog> getBlogById(String id);

    /**
     * Creates a new blog.
     *
     * @param blog the blog to create
     * @return the created blog
     * @throws IllegalArgumentException if a blog with the same slug already exists
     */
    Blog createBlog(Blog blog);

    /**
     * Updates an existing blog.
     *
     * @param id          the ID of the blog to update
     * @param blogDetails the updated blog details
     * @return an Optional containing the updated blog if found, or empty if not found
     */
    Optional<Blog> updateBlog(String id, Blog blogDetails);

    /**
     * Deletes a blog by its ID (soft delete).
     *
     * @param id the ID of the blog to delete
     * @return true if the blog was deleted, false otherwise
     */
    boolean deleteBlog(String id);

    /**
     * Publishes a blog by setting its status to PUBLISHED.
     *
     * @param id the ID of the blog to publish
     * @return an Optional containing the published blog if found, or empty if not found
     */
    Optional<Blog> publishBlog(String id);

    /**
     * Unpublishes a blog by setting its status to DRAFT.
     *
     * @param id the ID of the blog to unpublish
     * @return an Optional containing the unpublished blog if found, or empty if not found
     */
    Optional<Blog> unpublishBlog(String id);

}
