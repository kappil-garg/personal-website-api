package com.kapil.personalwebsite.service.blog.impl;

import com.kapil.personalwebsite.dto.blog.BlogCreateRequest;
import com.kapil.personalwebsite.dto.blog.BlogUpdateRequest;
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

import java.time.LocalDateTime;
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
     * Calculates estimated reading time based on word count at 200 words per minute.
     *
     * @param content the blog content (maybe null or empty)
     * @return reading time in minutes, minimum 1 for non-empty content
     */
    private static int calculateReadingTime(String content) {
        if (content == null || content.isBlank()) {
            return 0;
        }
        int wordCount = content.trim().split("\\s+").length;
        return Math.max(1, (int) Math.ceil(wordCount / 200.0));
    }

    /**
     * Retrieves all blogs, including drafts and archived.
     *
     * @return a list of all blogs ordered by creation date (newest first)
     */
    @Override
    @Transactional(readOnly = true)
    public List<Blog> getAllBlogs() {
        LOGGER.debug("Fetching all blogs for admin access");
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
        LOGGER.debug("Fetching blog by slug: {}", slug);
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
        LOGGER.debug("Fetching blog by ID: {}", id);
        return blogRepository.findByIdAndIsActiveTrue(id);
    }

    /**
     * Creates a new blog from the supplied request DTO.
     *
     * @param request the creation request containing author-supplied fields
     * @return the persisted blog
     */
    @Override
    public Blog createBlog(BlogCreateRequest request) {
        LOGGER.info("Creating new blog: {}", request.title());
        if (blogRepository.existsBySlug(request.slug())) {
            throw new BlogSlugAlreadyExistsException("Blog with slug '" + request.slug() + "' already exists");
        }
        Blog blog = new Blog();
        blog.setTitle(request.title());
        blog.setContent(request.content());
        blog.setSlug(request.slug());
        blog.setExcerpt(request.excerpt());
        blog.setFeaturedImage(request.featuredImage());
        blog.setCategory(request.category());
        blog.setReadingTime(calculateReadingTime(request.content()));
        return blogRepository.save(blog);
    }

    /**
     * Updates an existing blog from the supplied request DTO.
     *
     * @param id      the ID of the blog to update
     * @param request the update request containing the new field values
     * @return the updated blog
     */
    @Override
    public Blog updateBlog(String id, BlogUpdateRequest request) {
        LOGGER.info("Updating blog: {}", id);
        return blogRepository.findByIdAndIsActiveTrue(id)
                .map(existingBlog -> {
                    applyUpdateRequest(existingBlog, request);
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
        blog.setStatus(BlogStatus.PUBLISHED);
        blog.setPublishedAt(LocalDateTime.now());
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
     * Applies the fields from a BlogUpdateRequest to an existing Blog entity, mutating it in place.
     * Only updates the reading time if the content has changed or if a new reading time is explicitly provided.
     *
     * @param existingBlog the blog entity to mutate
     * @param request      the incoming update request
     */
    private void applyUpdateRequest(Blog existingBlog, BlogUpdateRequest request) {
        String previousContent = existingBlog.getContent();
        if (request.slug() != null && !request.slug().equals(existingBlog.getSlug())) {
            boolean slugTakenByOther = blogRepository.findBySlugAndIsActiveTrue(request.slug())
                    .filter(other -> !other.getId().equals(existingBlog.getId()))
                    .isPresent();
            if (slugTakenByOther) {
                throw new BlogSlugAlreadyExistsException(
                        "Blog with slug '" + request.slug() + "' already exists");
            }
        }
        existingBlog.setTitle(request.title());
        existingBlog.setContent(request.content());
        existingBlog.setSlug(request.slug());
        existingBlog.setExcerpt(request.excerpt());
        existingBlog.setFeaturedImage(request.featuredImage());
        existingBlog.setCategory(request.category());
        if (request.content() != null && !request.content().equals(previousContent)) {
            existingBlog.setReadingTime(calculateReadingTime(request.content()));
        } else if (request.readingTime() != null) {
            existingBlog.setReadingTime(request.readingTime());
        }
    }

}
