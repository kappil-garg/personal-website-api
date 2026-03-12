package com.kapil.personalwebsite.controller;

import com.kapil.personalwebsite.dto.ApiResponse;
import com.kapil.personalwebsite.entity.Blog;
import com.kapil.personalwebsite.entity.BlogCategory;
import com.kapil.personalwebsite.service.blog.BlogAnalyticsService;
import com.kapil.personalwebsite.service.blog.BlogPublicService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for public blog access endpoints.
 * Handles published blog listing, category filtering, detail retrieval and view counts.
 *
 * @author Kapil Garg
 */
@RestController
@RequestMapping("/blogs")
@RequiredArgsConstructor
public class PublicBlogController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublicBlogController.class);

    private final BlogPublicService blogPublicService;
    private final BlogAnalyticsService blogAnalyticsService;

    @NonNull
    static ResponseEntity<ApiResponse<Blog>> getApiResponseResponseEntity(String slug, Optional<Blog> blog) {
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
        return getApiResponseResponseEntity(slug, blog);
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

}
