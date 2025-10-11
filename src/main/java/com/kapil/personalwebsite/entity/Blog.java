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

    @Field("is_active")
    private Boolean isActive = true;

    public Blog(String title, String content, String slug) {
        this.title = title;
        this.content = content;
        this.slug = slug;
    }

}
