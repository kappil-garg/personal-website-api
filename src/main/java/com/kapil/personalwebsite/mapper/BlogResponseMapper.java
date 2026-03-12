package com.kapil.personalwebsite.mapper;

import com.kapil.personalwebsite.dto.ApiResponse;
import com.kapil.personalwebsite.entity.Blog;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Mapper for building blog response entities.
 *
 * @author Kapil Garg
 */
public class BlogResponseMapper {

    /**
     * Builds a blog response entity.
     *
     * @param slug the slug of the blog
     * @param blog the blog entity, or {@code null} if not found
     * @return the blog response entity
     */
    public static ResponseEntity<ApiResponse<Blog>> buildBlogResponse(String slug, Blog blog) {
        if (blog != null) {
            ApiResponse<Blog> response = ApiResponse.success(
                    blog,
                    String.format("Blog with slug '%s' retrieved successfully", slug)
            );
            return ResponseEntity.ok(response);
        } else {
            ApiResponse<Blog> response = ApiResponse.error(
                    String.format("Blog with slug '%s' not found", slug),
                    HttpStatus.NOT_FOUND.value()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

}
