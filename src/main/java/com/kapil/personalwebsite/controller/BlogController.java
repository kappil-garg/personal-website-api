package com.kapil.personalwebsite.controller;

import com.kapil.personalwebsite.dto.ApiResponse;
import com.kapil.personalwebsite.entity.Blog;
import com.kapil.personalwebsite.entity.BlogCategory;
import com.kapil.personalwebsite.service.blog.BlogAdminService;
import com.kapil.personalwebsite.service.blog.BlogAnalyticsService;
import com.kapil.personalwebsite.service.blog.BlogPublicService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 */
@RestController
@RequestMapping("/blogs")
@RequiredArgsConstructor
public class BlogController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlogController.class);

    private final BlogAdminService blogAdminService;
    private final BlogPublicService blogPublicService;
    private final BlogAnalyticsService blogAnalyticsService;

    /**
     * Retrieves all blogs (admin only).
     *
     * @return a ResponseEntity containing the list of all blogs
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Blog>>> getAllBlogs() {
        LOGGER.info("GET /blogs - Fetching all blogs (admin)");
        List<Blog> blogs = blogAdminService.getAllBlogs();
        ApiResponse<List<Blog>> response = ApiResponse.success(blogs, "Blogs retrieved successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a blog by its slug (admin only).
     *
     * @param slug the slug of the blog
     * @return a ResponseEntity containing the blog if found, or a 404 status if not found
     */
    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<Blog>> getBlogBySlug(@PathVariable String slug) {
        LOGGER.info("GET /blogs/{} - Fetching blog by slug (admin)", slug);
        Optional<Blog> blog = blogAdminService.getBlogBySlug(slug);
        if (blog.isPresent()) {
            ApiResponse<Blog> response = ApiResponse.success(blog.get(),
                    String.format("Blog with slug '%s' retrieved successfully", slug));
            return ResponseEntity.ok(response);
        } else {
            ApiResponse<Blog> response = ApiResponse.error(
                    String.format("Blog with slug '%s' not found", slug),
                    HttpStatus.NOT_FOUND.value());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Retrieves a blog by its ID (admin only).
     *
     * @param id the ID of the blog
     * @return a ResponseEntity containing the blog if found, or a 404 status if not found
     */
    @GetMapping("/id/{id}")
    public ResponseEntity<ApiResponse<Blog>> getBlogById(@PathVariable String id) {
        LOGGER.info("GET /blogs/id/{} - Fetching blog by ID (admin)", id);
        Optional<Blog> blog = blogAdminService.getBlogById(id);
        if (blog.isPresent()) {
            ApiResponse<Blog> response = ApiResponse.success(blog.get(),
                    String.format("Blog with ID '%s' retrieved successfully", id));
            return ResponseEntity.ok(response);
        } else {
            ApiResponse<Blog> response = ApiResponse.error(
                    String.format("Blog with ID '%s' not found", id),
                    HttpStatus.NOT_FOUND.value());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Retrieves all published blogs (public access).
     *
     * @return a ResponseEntity containing the list of published blogs
     */
    @GetMapping("/published")
    public ResponseEntity<ApiResponse<List<Blog>>> getPublishedBlogs() {
        LOGGER.info("GET /blogs/published - Fetching all published blogs (public)");
        List<Blog> blogs = blogPublicService.getPublishedBlogs();
        ApiResponse<List<Blog>> response = ApiResponse.success(blogs, "Published blogs retrieved successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves published blogs by category (public access).
     *
     * @param category the blog category
     * @return a ResponseEntity containing the list of published blogs in the category
     */
    @GetMapping("/published/category/{category}")
    public ResponseEntity<ApiResponse<List<Blog>>> getPublishedBlogsByCategory(@PathVariable BlogCategory category) {
        LOGGER.info("GET /blogs/published/category/{} - Fetching published blogs by category (public)", category);
        List<Blog> blogs = blogPublicService.getPublishedBlogsByCategory(category);
        ApiResponse<List<Blog>> response = ApiResponse.success(blogs,
                String.format("Published blogs in category '%s' retrieved successfully", category));
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a published blog by its slug (public access).
     *
     * @param slug the slug of the blog
     * @return a ResponseEntity containing the published blog if found, or a 404 status if not found
     */
    @GetMapping("/published/{slug}")
    public ResponseEntity<ApiResponse<Blog>> getPublishedBlogBySlug(@PathVariable String slug) {
        LOGGER.info("GET /blogs/published/{} - Fetching published blog by slug (public)", slug);
        Optional<Blog> blog = blogPublicService.getPublishedBlogBySlug(slug);
        if (blog.isPresent()) {
            ApiResponse<Blog> response = ApiResponse.success(blog.get(),
                    String.format("Blog with slug '%s' retrieved successfully", slug));
            return ResponseEntity.ok(response);
        } else {
            ApiResponse<Blog> response = ApiResponse.error(
                    String.format("Blog with slug '%s' not found", slug),
                    HttpStatus.NOT_FOUND.value());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Increments the view count of a blog (public access).
     *
     * @param id the ID of the blog
     * @return a ResponseEntity containing the blog with updated view count if found, or a 404 status if not found
     */
    @PostMapping("/{id}/view")
    public ResponseEntity<ApiResponse<Blog>> incrementViewCount(@PathVariable String id) {
        LOGGER.info("POST /blogs/{}/view - Incrementing view count (public)", id);
        Optional<Blog> blog = blogAnalyticsService.incrementViewCount(id);
        if (blog.isPresent()) {
            ApiResponse<Blog> response = ApiResponse.success(blog.get(),
                    "View count incremented successfully");
            return ResponseEntity.ok(response);
        } else {
            ApiResponse<Blog> response = ApiResponse.error(
                    String.format("Blog with ID '%s' not found", id),
                    HttpStatus.NOT_FOUND.value());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Creates a new blog (admin only).
     *
     * @param blog the blog to create
     * @return a ResponseEntity containing the created blog
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Blog>> createBlog(@RequestBody Blog blog) {
        LOGGER.info("POST /blogs - Creating new blog: {} (admin)", blog.getTitle());
        Blog createdBlog = blogAdminService.createBlog(blog);
        ApiResponse<Blog> response = ApiResponse.success(createdBlog, "Blog created successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Updates an existing blog (admin only).
     *
     * @param id   the ID of the blog to update
     * @param blog the updated blog details
     * @return a ResponseEntity containing the updated blog if found, or a 404 status if not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Blog>> updateBlog(@PathVariable String id, @RequestBody Blog blog) {
        LOGGER.info("PUT /blogs/{} - Updating blog (admin)", id);
        Blog updatedBlog = blogAdminService.updateBlog(id, blog);
        ApiResponse<Blog> response = ApiResponse.success(updatedBlog, "Blog updated successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a blog by its ID.
     *
     * @param id the ID of the blog to delete
     * @return a ResponseEntity with no content if deleted, or 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBlog(@PathVariable String id) {
        LOGGER.info("DELETE /blogs/{} - Deleting blog", id);
        blogAdminService.deleteBlog(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Publishes a blog.
     *
     * @param id the ID of the blog to publish
     * @return a ResponseEntity containing the published blog if found, or a 404 status if not found
     */
    @PutMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<Blog>> publishBlog(@PathVariable String id) {
        LOGGER.info("PUT /blogs/{}/publish - Publishing blog", id);
        Blog publishedBlog = blogAdminService.publishBlog(id);
        ApiResponse<Blog> response = ApiResponse.success(publishedBlog, "Blog published successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Unpublishes a blog.
     *
     * @param id the ID of the blog to unpublish
     * @return a ResponseEntity containing the unpublished blog if found, or a 404 status if not found
     */
    @PutMapping("/{id}/unpublish")
    public ResponseEntity<ApiResponse<Blog>> unpublishBlog(@PathVariable String id) {
        LOGGER.info("PUT /blogs/{}/unpublish - Unpublishing blog", id);
        Blog unpublishedBlog = blogAdminService.unpublishBlog(id);
        ApiResponse<Blog> response = ApiResponse.success(unpublishedBlog, "Blog unpublished successfully");
        return ResponseEntity.ok(response);
    }

}
