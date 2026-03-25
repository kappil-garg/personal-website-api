package com.kapil.personalwebsite.dto.blog;

import com.kapil.personalwebsite.entity.BlogCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating a new blog post.
 *
 * @author Kapil Garg
 */
public record BlogCreateRequest(

        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title must not exceed 200 characters")
        String title,

        @NotBlank(message = "Content is required")
        String content,

        @NotBlank(message = "Slug is required")
        @Size(max = 250, message = "Slug must not exceed 250 characters")
        String slug,

        @Size(max = 500, message = "Excerpt must not exceed 500 characters")
        String excerpt,

        @Size(max = 1000, message = "Featured image URL must not exceed 1000 characters")
        String featuredImage,

        BlogCategory category

) {
}
