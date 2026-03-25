package com.kapil.personalwebsite.service.blog;

import com.kapil.personalwebsite.dto.blog.BlogCreateRequest;
import com.kapil.personalwebsite.dto.blog.BlogUpdateRequest;
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
     * Creates a new blog from the supplied request DTO.
     *
     * @param request the creation request containing author-supplied fields
     * @return the persisted blog
     */
    Blog createBlog(BlogCreateRequest request);

    /**
     * Updates an existing blog from the supplied request DTO.
     *
     * @param id      the ID of the blog to update
     * @param request the update request containing the new field values
     * @return the updated blog
     */
    Blog updateBlog(String id, BlogUpdateRequest request);

    /**
     * Deletes a blog by its ID (soft delete).
     *
     * @param id the ID of the blog to delete
     */
    void deleteBlog(String id);

    /**
     * Publishes a blog by setting its status to PUBLISHED.
     *
     * @param id the ID of the blog to publish
     * @return the published blog
     */
    Blog publishBlog(String id);

    /**
     * Unpublishes a blog by setting its status to DRAFT.
     *
     * @param id the ID of the blog to unpublish
     * @return the unpublished blog
     */
    Blog unpublishBlog(String id);

}
