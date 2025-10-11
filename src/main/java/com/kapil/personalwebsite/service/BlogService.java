package com.kapil.personalwebsite.service;

import com.kapil.personalwebsite.entity.Blog;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing Blog entities.
 *
 * @author Kapil Garg
 */
public interface BlogService {

    /**
     * Retrieves all blogs.
     *
     * @return a list of all blogs
     */
    List<Blog> getAllBlogs();

    /**
     * Retrieves a blog by its slug.
     *
     * @param slug the slug of the blog
     * @return an Optional containing the blog if found, or empty if not found
     */
    Optional<Blog> getBlogBySlug(String slug);

    /**
     * Retrieves a blog by its ID.
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
     */
    Blog createBlog(Blog blog);

    /**
     * Updates an existing blog.
     *
     * @param id the ID of the blog to update
     * @param blogDetails the updated blog details
     * @return an Optional containing the updated blog if found, or empty if not found
     */
    Optional<Blog> updateBlog(String id, Blog blogDetails);

    /**
     * Deletes a blog by its ID.
     *
     * @param id the ID of the blog to delete
     * @return true if the blog was deleted, false otherwise
     */
    boolean deleteBlog(String id);

}
