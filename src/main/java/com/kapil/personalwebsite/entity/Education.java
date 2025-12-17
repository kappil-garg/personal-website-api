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

/**
 * Entity representing Education (Professional Degrees).
 * Maps to the "educations" collection in MongoDB.
 *
 * @author Kapil Garg
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "educations")
public class Education {

    @Id
    private String id;

    @NotBlank(message = "Degree is required")
    @Size(max = 200, message = "Degree must not exceed 200 characters")
    @Field("degree")
    private String degree;

    @NotBlank(message = "Field of study is required")
    @Size(max = 200, message = "Field of study must not exceed 200 characters")
    @Field("field_of_study")
    private String fieldOfStudy;

    @NotBlank(message = "Institution name is required")
    @Size(max = 200, message = "Institution name must not exceed 200 characters")
    @Field("institution_name")
    private String institutionName;

    @Size(max = 100, message = "Location must not exceed 100 characters")
    @Field("location")
    private String location;

    @NotBlank(message = "Start date is required")
    @Size(max = 7, message = "Start date must be in MM-YYYY format")
    @Pattern(regexp = "^(0[1-9]|1[0-2])-\\d{4}$", message = "Start date must be in MM-YYYY format (e.g., 12-2025)")
    @Field("start_date")
    private String startDate;

    @Size(max = 7, message = "End date must be in MM-YYYY format")
    @Pattern(regexp = "^(0[1-9]|1[0-2])-\\d{4}$", message = "End date must be in MM-YYYY format (e.g., 12-2025)")
    @Field("end_date")
    private String endDate;

    @Field("is_current")
    private Boolean isCurrent = false;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @Field("description")
    private String description;

    @Size(max = 500, message = "Institution logo URL must not exceed 500 characters")
    @Field("institution_logo")
    private String institutionLogo;

    @Size(max = 500, message = "Institution website must not exceed 500 characters")
    @Field("institution_website")
    private String institutionWebsite;

    @Field("display_order")
    private Integer displayOrder = 0;

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;

}
