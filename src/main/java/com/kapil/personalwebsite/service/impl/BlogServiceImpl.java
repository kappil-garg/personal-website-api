package com.kapil.personalwebsite.service.impl;

import com.kapil.personalwebsite.entity.Blog;
import com.kapil.personalwebsite.repository.BlogRepository;
import com.kapil.personalwebsite.service.BlogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for managing Blog entities.
 *
 * @author Kapil Garg
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BlogServiceImpl implements BlogService {

    private final BlogRepository blogRepository;

    /**
     * Retrieves all active blogs ordered by creation date in descending order.
     *
     * @return a list of all active blogs
     */
    @Override
    @Transactional(readOnly = true)
    public List<Blog> getAllBlogs() {
        log.info("Fetching all active blogs");
        return blogRepository.findByIsActiveTrueOrderByCreatedAtDesc();
    }

    /**
     * Retrieves a blog by its slug.
     *
     * @param slug the slug of the blog
     * @return an Optional containing the blog if found, or empty if not found
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Blog> getBlogBySlug(String slug) {
        log.info("Fetching blog by slug: {}", slug);
        return blogRepository.findBySlugAndIsActiveTrue(slug);
    }

    /**
     * Retrieves a blog by its ID.
     *
     * @param id the ID of the blog
     * @return an Optional containing the blog if found, or empty if not found
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Blog> getBlogById(String id) {
        log.info("Fetching blog by ID: {}", id);
        return blogRepository.findByIdAndIsActiveTrue(id);
    }

    /**
     * Creates a new blog.
     *
     * @param blog the blog to create
     * @return the created blog
     * @throws IllegalArgumentException if a blog with the same slug already exists
     */
    @Override
    public Blog createBlog(Blog blog) {
        log.info("Creating new blog: {}", blog.getTitle());
        if (blogRepository.existsBySlug(blog.getSlug())) {
            throw new IllegalArgumentException("Blog with slug '" + blog.getSlug() + "' already exists");
        }
        return blogRepository.save(blog);
    }

    /**
     * Updates an existing blog.
     *
     * @param id          the ID of the blog to update
     * @param blogDetails the updated blog details
     * @return an Optional containing the updated blog if found, or empty if not found
     */
    @Override
    public Optional<Blog> updateBlog(String id, Blog blogDetails) {
        log.info("Updating blog: {}", id);
        return blogRepository.findByIdAndIsActiveTrue(id)
                .map(existingBlog -> {
                    existingBlog.setTitle(blogDetails.getTitle());
                    existingBlog.setContent(blogDetails.getContent());
                    existingBlog.setSlug(blogDetails.getSlug());
                    return blogRepository.save(existingBlog);
                });
    }

    /**
     * Deletes a blog by its ID (soft delete).
     *
     * @param id the ID of the blog to delete
     * @return true if the blog was deleted, false otherwise
     */
    @Override
    public boolean deleteBlog(String id) {
        log.info("Deleting blog: {}", id);
        return blogRepository.findByIdAndIsActiveTrue(id)
                .map(blog -> {
                    blog.setIsActive(false);
                    blogRepository.save(blog);
                    return true;
                })
                .orElse(false);
    }

}
