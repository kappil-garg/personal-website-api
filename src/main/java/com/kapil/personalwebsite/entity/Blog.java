package com.kapil.personalwebsite.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * Entity representing a Blog post.
 * Maps to the "blogs" collection in MongoDB.
 *
 * @author Kapil Garg
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "blogs")
public class Blog {

    @Id
    private String id;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    @Field("title")
    private String title;

    @NotBlank(message = "Content is required")
    @Field("content")
    private String content;

    @NotBlank(message = "Slug is required")
    @Size(max = 250, message = "Slug must not exceed 250 characters")
    @Indexed(unique = true)
    @Field("slug")
    private String slug;

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;

    @Size(max = 500, message = "Excerpt must not exceed 500 characters")
    @Field("excerpt")
    private String excerpt;

    @Field("featured_image")
    private String featuredImage;

    @Field("reading_time")
    private Integer readingTime;

    @Field("view_count")
    private Long viewCount = 0L;

    @Field("status")
    private BlogStatus status = BlogStatus.DRAFT;

    @Field("published_at")
    private LocalDateTime publishedAt;

    @Field("is_active")
    private Boolean isActive = true;

    @Field("category")
    private BlogCategory category;

    /**
     * Constructor for creating new blogs.
     *
     * @param title   the blog title
     * @param content the blog content
     * @param slug    the blog slug
     */
    public Blog(String title, String content, String slug) {
        this.title = title;
        this.content = content;
        this.slug = slug;
    }

    public Blog(String title, String content, String slug, BlogCategory category) {
        this.title = title;
        this.content = content;
        this.slug = slug;
        this.category = category;
    }

    /**
     * Increments the view count by 1.
     */
    public void incrementViewCount() {
        this.viewCount = (viewCount != null) ? viewCount + 1 : 1L;
    }

    /**
     * Publishes the blog by setting status to PUBLISHED and setting published date.
     */
    public void publish() {
        this.status = BlogStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }

    /**
     * Checks if the blog is publicly visible.
     *
     * @return true if the blog is published and active, false otherwise
     */
    public boolean isPubliclyVisible() {
        return isActive && status.isPubliclyVisible();
    }

    /**
     * Calculates reading time based on content length.
     * Assumes average reading speed of 200 words per minute.
     *
     * @return estimated reading time in minutes
     */
    public int calculateReadingTime() {
        if (content == null || content.trim().isEmpty()) {
            return 0;
        }
        int wordCount = content.trim().split("\\s+").length;
        return Math.max(1, (int) Math.ceil(wordCount / 200.0));
    }

}
