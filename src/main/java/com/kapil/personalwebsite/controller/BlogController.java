package com.kapil.personalwebsite.controller;

import com.kapil.personalwebsite.entity.Blog;
import com.kapil.personalwebsite.service.BlogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Blog entities.
 * Provides endpoints to retrieve all blogs, a blog by its slug, and a blog by its ID.
 *
 * @author Kapil Garg
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/blogs")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;

    /**
     * Retrieves all blogs.
     *
     * @return a ResponseEntity containing the list of all blogs
     */
    @GetMapping
    public ResponseEntity<List<Blog>> getAllBlogs() {
        log.info("GET /blogs - Fetching all blogs");
        List<Blog> blogs = blogService.getAllBlogs();
        return ResponseEntity.ok(blogs);
    }

    /**
     * Retrieves a blog by its slug.
     *
     * @param slug the slug of the blog
     * @return a ResponseEntity containing the blog if found, or a 404 status if not found
     */
    @GetMapping("/{slug}")
    public ResponseEntity<Blog> getBlogBySlug(@PathVariable String slug) {
        log.info("GET /blogs/{} - Fetching blog by slug", slug);
        Optional<Blog> blog = blogService.getBlogBySlug(slug);
        return blog.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves a blog by its ID.
     *
     * @param id the ID of the blog
     * @return a ResponseEntity containing the blog if found, or a 404 status if not found
     */
    @GetMapping("/id/{id}")
    public ResponseEntity<Blog> getBlogById(@PathVariable String id) {
        log.info("GET /blogs/id/{} - Fetching blog by ID", id);
        Optional<Blog> blog = blogService.getBlogById(id);
        return blog.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
