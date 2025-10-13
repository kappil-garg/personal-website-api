package com.kapil.personalwebsite.controller;

import com.kapil.personalwebsite.entity.Blog;
import com.kapil.personalwebsite.service.blog.BlogAdminService;
import com.kapil.personalwebsite.service.blog.BlogAnalyticsService;
import com.kapil.personalwebsite.service.blog.BlogPublicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Blog entities.
 * Provides endpoints for public blog access, admin management, and analytics.
 *
 * @author Kapil Garg
 * @version 2.0
 */
@Slf4j
@RestController
@RequestMapping("/blogs")
@RequiredArgsConstructor
public class BlogController {

    private final BlogPublicService blogPublicService;
    private final BlogAdminService blogAdminService;
    private final BlogAnalyticsService blogAnalyticsService;

    /**
     * Retrieves all blogs (admin only).
     *
     * @return a ResponseEntity containing the list of all blogs
     */
    @GetMapping
    public ResponseEntity<List<Blog>> getAllBlogs() {
        log.info("GET /blogs - Fetching all blogs (admin)");
        List<Blog> blogs = blogAdminService.getAllBlogs();
        return ResponseEntity.ok(blogs);
    }

    /**
     * Retrieves a blog by its slug (admin only).
     *
     * @param slug the slug of the blog
     * @return a ResponseEntity containing the blog if found, or a 404 status if not found
     */
    @GetMapping("/{slug}")
    public ResponseEntity<Blog> getBlogBySlug(@PathVariable String slug) {
        log.info("GET /blogs/{} - Fetching blog by slug (admin)", slug);
        Optional<Blog> blog = blogAdminService.getBlogBySlug(slug);
        return blog.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves a blog by its ID (admin only).
     *
     * @param id the ID of the blog
     * @return a ResponseEntity containing the blog if found, or a 404 status if not found
     */
    @GetMapping("/id/{id}")
    public ResponseEntity<Blog> getBlogById(@PathVariable String id) {
        log.info("GET /blogs/id/{} - Fetching blog by ID (admin)", id);
        Optional<Blog> blog = blogAdminService.getBlogById(id);
        return blog.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves all published blogs (public access).
     *
     * @return a ResponseEntity containing the list of published blogs
     */
    @GetMapping("/published")
    public ResponseEntity<List<Blog>> getPublishedBlogs() {
        log.info("GET /blogs/published - Fetching all published blogs (public)");
        List<Blog> blogs = blogPublicService.getPublishedBlogs();
        return ResponseEntity.ok(blogs);
    }

    /**
     * Retrieves a published blog by its slug (public access).
     *
     * @param slug the slug of the blog
     * @return a ResponseEntity containing the published blog if found, or a 404 status if not found
     */
    @GetMapping("/published/{slug}")
    public ResponseEntity<Blog> getPublishedBlogBySlug(@PathVariable String slug) {
        log.info("GET /blogs/published/{} - Fetching published blog by slug (public)", slug);
        Optional<Blog> blog = blogPublicService.getPublishedBlogBySlug(slug);
        return blog.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Increments the view count of a blog (public access).
     *
     * @param id the ID of the blog
     * @return a ResponseEntity containing the blog with updated view count if found, or a 404 status if not found
     */
    @PostMapping("/{id}/view")
    public ResponseEntity<Blog> incrementViewCount(@PathVariable String id) {
        log.info("POST /blogs/{}/view - Incrementing view count (public)", id);
        Optional<Blog> blog = blogAnalyticsService.incrementViewCount(id);
        return blog.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Creates a new blog (admin only).
     *
     * @param blog the blog to create
     * @return a ResponseEntity containing the created blog
     */
    @PostMapping
    public ResponseEntity<Blog> createBlog(@RequestBody Blog blog) {
        log.info("POST /blogs - Creating new blog: {} (admin)", blog.getTitle());
        Blog createdBlog = blogAdminService.createBlog(blog);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBlog);
    }

    /**
     * Updates an existing blog (admin only).
     *
     * @param id   the ID of the blog to update
     * @param blog the updated blog details
     * @return a ResponseEntity containing the updated blog if found, or a 404 status if not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<Blog> updateBlog(@PathVariable String id, @RequestBody Blog blog) {
        log.info("PUT /blogs/{} - Updating blog (admin)", id);
        Optional<Blog> updatedBlog = blogAdminService.updateBlog(id, blog);
        return updatedBlog.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Deletes a blog by its ID.
     *
     * @param id the ID of the blog to delete
     * @return a ResponseEntity with no content if deleted, or 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBlog(@PathVariable String id) {
        log.info("DELETE /blogs/{} - Deleting blog", id);
        boolean deleted = blogAdminService.deleteBlog(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    /**
     * Publishes a blog.
     *
     * @param id the ID of the blog to publish
     * @return a ResponseEntity containing the published blog if found, or a 404 status if not found
     */
    @PutMapping("/{id}/publish")
    public ResponseEntity<Blog> publishBlog(@PathVariable String id) {
        log.info("PUT /blogs/{}/publish - Publishing blog", id);
        Optional<Blog> publishedBlog = blogAdminService.publishBlog(id);
        return publishedBlog.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Unpublishes a blog.
     *
     * @param id the ID of the blog to unpublish
     * @return a ResponseEntity containing the unpublished blog if found, or a 404 status if not found
     */
    @PutMapping("/{id}/unpublish")
    public ResponseEntity<Blog> unpublishBlog(@PathVariable String id) {
        log.info("PUT /blogs/{}/unpublish - Unpublishing blog", id);
        Optional<Blog> unpublishedBlog = blogAdminService.unpublishBlog(id);
        return unpublishedBlog.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
