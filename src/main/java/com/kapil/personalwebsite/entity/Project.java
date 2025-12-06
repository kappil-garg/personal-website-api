package com.kapil.personalwebsite.entity;

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

    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @Field("description")
    private String description;

    @Size(max = 500, message = "Short description must not exceed 500 characters")
    @Field("short_description")
    private String shortDescription;

    @NotBlank(message = "Featured image is required")
    @Size(max = 1000, message = "Featured image URL must not exceed 1000 characters")
    @Field("featured_image")
    private String featuredImage;

    @Field("technologies")
    private List<String> technologies;

    @Size(max = 1000, message = "Project URL must not exceed 1000 characters")
    @Field("project_url")
    private String projectUrl;

    @Size(max = 1000, message = "GitHub URL must not exceed 1000 characters")
    @Field("github_url")
    private String githubUrl;

    @Size(max = 7, message = "Start date must be in MM-YYYY format")
    @Pattern(regexp = "^(0[1-9]|1[0-2])-\\d{4}$", message = "Start date must be in MM-YYYY format (e.g., 12-2025)")
    @Field("start_date")
    private String startDate;

    @Size(max = 7, message = "End date must be in MM-YYYY format")
    @Pattern(regexp = "^(0[1-9]|1[0-2])-\\d{4}$", message = "End date must be in MM-YYYY format (e.g., 12-2025)")
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

}
