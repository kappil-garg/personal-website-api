package com.kapil.personalwebsite.service.blog.impl;

import com.kapil.personalwebsite.entity.Blog;
import com.kapil.personalwebsite.entity.BlogStatus;
import com.kapil.personalwebsite.repository.BlogRepository;
import com.kapil.personalwebsite.service.blog.BlogPublicService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of BlogPublicService for public blog operations.
 * Provides read-only access to published blogs for website visitors.
 *
 * @author Kapil Garg
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BlogPublicServiceImpl implements BlogPublicService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlogPublicServiceImpl.class);

    private final BlogRepository blogRepository;

    /**
     * Retrieves all published blogs.
     *
     * @return a list of published blogs ordered by published date (newest first)
     */
    @Override
    public List<Blog> getPublishedBlogs() {
        LOGGER.info("Fetching all published blogs for public access");
        return blogRepository.findByStatusAndIsActiveTrueOrderByPublishedAtDesc(BlogStatus.PUBLISHED);
    }

    /**
     * Retrieves a published blog by its slug.
     *
     * @param slug the slug of the blog
     * @return an Optional containing the published blog if found, or empty if not found
     */
    @Override
    public Optional<Blog> getPublishedBlogBySlug(String slug) {
        LOGGER.info("Fetching published blog by slug: {}", slug);
        return blogRepository.findBySlugAndStatusAndIsActiveTrue(slug, BlogStatus.PUBLISHED);
    }

    /**
     * Retrieves a published blog by its ID.
     *
     * @param id the ID of the blog
     * @return an Optional containing the published blog if found, or empty if not found
     */
    @Override
    public Optional<Blog> getPublishedBlogById(String id) {
        LOGGER.info("Fetching published blog by ID: {}", id);
        return blogRepository.findByIdAndStatusAndIsActiveTrue(id, BlogStatus.PUBLISHED);
    }

}
