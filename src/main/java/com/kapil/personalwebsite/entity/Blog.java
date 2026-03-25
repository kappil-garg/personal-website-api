package com.kapil.personalwebsite.entity;

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

    @Field("title")
    private String title;

    @Field("content")
    private String content;

    @Indexed(unique = true)
    @Field("slug")
    private String slug;

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;

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
     * Constructor for creating new blogs (used in tests).
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

}
