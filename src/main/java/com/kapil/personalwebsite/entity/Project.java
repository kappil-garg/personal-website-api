package com.kapil.personalwebsite.entity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity representing Project.
 * Maps to the "projects" collection in MongoDB.
 *
 * @author Kapil Garg
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "projects")
public class Project {

    @Id
    private String id;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    @Field("title")
    private String title;

    @Size(max = 500, message = "Short description must not exceed 500 characters")
    @Field("short_description")
    private String shortDescription;

    @Field("overview")
    private List<String> overview;

    @Field("key_features")
    private List<String> keyFeatures;

    @Field("engineering")
    private List<String> engineering;

    @Field("decisions")
    private List<String> decisions;

    @Field("impact")
    private List<String> impact;

    @Field("highlights")
    private List<String> highlights;

    @NotBlank(message = "Featured image is required")
    @Size(max = 1000, message = "Featured image URL must not exceed 1000 characters")
    @Field("featured_image")
    private String featuredImage;

    @Size(max = 1000, message = "Project URL must not exceed 1000 characters")
    @Field("project_url")
    private String projectUrl;

    @Valid
    @Field("github_links")
    private List<GithubLink> githubLinks;

    @Size(max = 7, message = "Start date must be in YYYY-MM format")
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "Start date must be in YYYY-MM format (e.g., 2025-12)")
    @Field("start_date")
    private String startDate;

    @Size(max = 7, message = "End date must be in YYYY-MM format")
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "End date must be in YYYY-MM format (e.g., 2025-12)")
    @Field("end_date")
    private String endDate;

    @Field("is_active")
    private Boolean isActive = true;

    @Field("display_order")
    private Integer displayOrder = 0;

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GithubLink {

        @NotBlank(message = "GitHub link type is required")
        @Size(max = 100, message = "GitHub link type must not exceed 100 characters")
        @Field("type")
        private String type;

        @NotBlank(message = "GitHub link label is required")
        @Size(max = 200, message = "GitHub link label must not exceed 200 characters")
        @Field("label")
        private String label;

        @NotBlank(message = "GitHub link URL is required")
        @Size(max = 1000, message = "GitHub link URL must not exceed 1000 characters")
        @Field("url")
        private String url;

    }

}
