package com.kapil.personalwebsite.service.blog.impl;

import com.kapil.personalwebsite.entity.Blog;
import com.kapil.personalwebsite.repository.BlogRepository;
import com.kapil.personalwebsite.service.blog.BlogAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementation of BlogAnalyticsService for blog analytics operations.
 * Provides view tracking and analytics functionality.
 *
 * @author Kapil Garg
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BlogAnalyticsServiceImpl implements BlogAnalyticsService {

    private final BlogRepository blogRepository;

    /**
     * Increments the view count of a blog by its ID.
     *
     * @param id the ID of the blog
     * @return an Optional containing the blog with updated view count if found, or empty if not found
     */
    @Override
    public Optional<Blog> incrementViewCount(String id) {
        log.info("Incrementing view count for blog: {}", id);
        return blogRepository.findByIdAndIsActiveTrue(id)
                .map(blog -> {
                    blog.incrementViewCount();
                    return blogRepository.save(blog);
                });
    }

    /**
     * Retrieves the current view count of a blog by its ID.
     *
     * @param id the ID of the blog
     * @return an Optional containing the blog with current view count if found, or empty if not found
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Blog> getViewCount(String id) {
        log.info("Getting view count for blog: {}", id);
        return blogRepository.findByIdAndIsActiveTrue(id);
    }

}
