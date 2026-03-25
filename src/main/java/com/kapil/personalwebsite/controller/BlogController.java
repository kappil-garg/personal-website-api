package com.kapil.personalwebsite.controller;

import com.kapil.personalwebsite.ai.vector.PortfolioVectorIndexService;
import com.kapil.personalwebsite.dto.ApiResponse;
import com.kapil.personalwebsite.dto.blog.BlogCreateRequest;
import com.kapil.personalwebsite.dto.blog.BlogUpdateRequest;
import com.kapil.personalwebsite.entity.Blog;
import com.kapil.personalwebsite.mapper.BlogResponseMapper;
import com.kapil.personalwebsite.service.blog.BlogAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for admin blog management endpoints.
 * Handles CRUD, publish/unpublish, and admin-only retrieval operations.
 *
 * @author Kapil Garg
 */
@RestController
@RequestMapping("/blogs")
@RequiredArgsConstructor
public class BlogController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlogController.class);

    private final BlogAdminService blogAdminService;
    private final ObjectProvider<PortfolioVectorIndexService> portfolioVectorIndexService;

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
        return BlogResponseMapper.buildBlogResponse(slug, blog.orElse(null));
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
        return BlogResponseMapper.buildBlogByIdResponse(id, blog.orElse(null));
    }

    /**
     * Creates a new blog (admin only).
     *
     * @return a ResponseEntity containing the created blog
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Blog>> createBlog(@Valid @RequestBody BlogCreateRequest request) {
        LOGGER.info("POST /blogs - Creating new blog: {} (admin)", request.title());
        Blog createdBlog = blogAdminService.createBlog(request);
        triggerPortfolioVectorReindex();
        ApiResponse<Blog> response = ApiResponse.success(createdBlog, "Blog created successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Updates an existing blog (admin only).
     *
     * @param id      the ID of the blog to update
     * @param request the updated blog data
     * @return a ResponseEntity containing the updated blog if found, or a 404 status if not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Blog>> updateBlog(@PathVariable String id,
                                                        @Valid @RequestBody BlogUpdateRequest request) {
        LOGGER.info("PUT /blogs/{} - Updating blog (admin)", id);
        Blog updatedBlog = blogAdminService.updateBlog(id, request);
        triggerPortfolioVectorReindex();
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
        triggerPortfolioVectorReindex();
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
        triggerPortfolioVectorReindex();
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
        triggerPortfolioVectorReindex();
        ApiResponse<Blog> response = ApiResponse.success(unpublishedBlog, "Blog unpublished successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Schedules an async portfolio vector reindex after a blog mutation.
     * Returns immediately; the rebuild runs on a background thread.
     */
    private void triggerPortfolioVectorReindex() {
        portfolioVectorIndexService.ifAvailable(PortfolioVectorIndexService::rebuildIndexAsync);
    }

}
