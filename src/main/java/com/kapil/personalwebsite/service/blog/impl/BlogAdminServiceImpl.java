package com.kapil.personalwebsite.service.blog.impl;

import com.kapil.personalwebsite.entity.Blog;
import com.kapil.personalwebsite.entity.BlogStatus;
import com.kapil.personalwebsite.exception.BlogNotFoundException;
import com.kapil.personalwebsite.exception.BlogSlugAlreadyExistsException;
import com.kapil.personalwebsite.repository.BlogRepository;
import com.kapil.personalwebsite.service.blog.BlogAdminService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of BlogAdminService for blog administration operations.
 * Provides full CRUD operations and blog lifecycle management.
 *
 * @author Kapil Garg
 */
@Service
@Transactional
@RequiredArgsConstructor
public class BlogAdminServiceImpl implements BlogAdminService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlogAdminServiceImpl.class);

    private final BlogRepository blogRepository;

    /**
     * Retrieves all blogs, including drafts and archived.
     *
     * @return a list of all blogs ordered by creation date (newest first)
     */
    @Override
    @Transactional(readOnly = true)
    public List<Blog> getAllBlogs() {
        LOGGER.info("Fetching all blogs for admin access");
        return blogRepository.findByIsActiveTrueOrderByCreatedAtDesc();
    }

    /**
     * Retrieves a blog by its slug, regardless of its status.
     *
     * @param slug the slug of the blog
     * @return an Optional containing the blog if found, or empty if not found
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Blog> getBlogBySlug(String slug) {
        LOGGER.info("Fetching blog by slug: {}", slug);
        return blogRepository.findBySlugAndIsActiveTrue(slug);
    }

    /**
     * Retrieves a blog by its ID, regardless of its status.
     *
     * @param id the ID of the blog
     * @return an Optional containing the blog if found, or empty if not found
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Blog> getBlogById(String id) {
        LOGGER.info("Fetching blog by ID: {}", id);
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
        LOGGER.info("Creating new blog: {}", blog.getTitle());
        if (blogRepository.existsBySlug(blog.getSlug())) {
            throw new BlogSlugAlreadyExistsException("Blog with slug '" + blog.getSlug() + "' already exists");
        }
        if (blog.getReadingTime() == null) {
            blog.setReadingTime(blog.calculateReadingTime());
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
    public Blog updateBlog(String id, Blog blogDetails) {
        LOGGER.info("Updating blog: {}", id);
        return blogRepository.findByIdAndIsActiveTrue(id)
                .map(existingBlog -> {
                    updateBlogFields(existingBlog, blogDetails);
                    return blogRepository.save(existingBlog);
                })
                .orElseThrow(() -> new BlogNotFoundException("Blog with ID '" + id + "' not found"));
    }

    /**
     * Deletes a blog by its ID (soft delete).
     *
     * @param id the ID of the blog to delete
     */
    @Override
    public void deleteBlog(String id) {
        LOGGER.info("Deleting blog: {}", id);
        Blog blog = blogRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new BlogNotFoundException("Blog with ID '" + id + "' not found"));
        blog.setIsActive(false);
        blogRepository.save(blog);
    }

    /**
     * Publishes a blog by setting its status to PUBLISHED.
     *
     * @param id the ID of the blog to publish
     * @return an Optional containing the published blog if found, or empty if not found
     */
    @Override
    public Blog publishBlog(String id) {
        LOGGER.info("Publishing blog: {}", id);
        Blog blog = blogRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new BlogNotFoundException("Blog with ID '" + id + "' not found"));
        blog.publish();
        return blogRepository.save(blog);
    }

    /**
     * Unpublishes a blog by setting its status to DRAFT.
     *
     * @param id the ID of the blog to unpublish
     * @return an Optional containing the unpublished blog if found, or empty if not found
     */
    @Override
    public Blog unpublishBlog(String id) {
        LOGGER.info("Unpublishing blog: {}", id);
        Blog blog = blogRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new BlogNotFoundException("Blog with ID '" + id + "' not found"));
        blog.setStatus(BlogStatus.DRAFT);
        blog.setPublishedAt(null);
        return blogRepository.save(blog);
    }

    /**
     * Updates the fields of an existing blog with the provided details.
     *
     * @param existingBlog the existing blog to update
     * @param blogDetails  the new blog details
     */
    private void updateBlogFields(Blog existingBlog, Blog blogDetails) {
        existingBlog.setTitle(blogDetails.getTitle());
        existingBlog.setContent(blogDetails.getContent());
        existingBlog.setSlug(blogDetails.getSlug());
        existingBlog.setExcerpt(blogDetails.getExcerpt());
        existingBlog.setFeaturedImage(blogDetails.getFeaturedImage());
        existingBlog.setCategory(blogDetails.getCategory());
        if (blogDetails.getContent() != null && !blogDetails.getContent().equals(existingBlog.getContent())) {
            existingBlog.setReadingTime(existingBlog.calculateReadingTime());
        } else if (blogDetails.getReadingTime() != null) {
            existingBlog.setReadingTime(blogDetails.getReadingTime());
        }
    }

}
