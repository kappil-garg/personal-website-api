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
 * Entity representing Work Experience.
 * Maps to the "experiences" collection in MongoDB.
 *
 * @author Kapil Garg
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "experiences")
public class Experience {

    @Id
    private String id;

    @NotBlank(message = "Company name is required")
    @Size(max = 200, message = "Company name must not exceed 200 characters")
    @Field("company_name")
    private String companyName;

    @NotBlank(message = "Position is required")
    @Size(max = 200, message = "Position must not exceed 200 characters")
    @Field("position")
    private String position;

    @Size(max = 100, message = "Location must not exceed 100 characters")
    @Field("location")
    private String location;

    @NotBlank(message = "Start date is required")
    @Size(max = 7, message = "Start date must be in YYYY-MM format")
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "Start date must be in YYYY-MM format (e.g., 2025-12)")
    @Field("start_date")
    private String startDate;

    @Size(max = 7, message = "End date must be in YYYY-MM format")
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "End date must be in YYYY-MM format (e.g., 2025-12)")
    @Field("end_date")
    private String endDate;

    @Field("is_current")
    private Boolean isCurrent = false;

    @Field("summary")
    private List<String> summary;

    @Field("impact")
    private List<String> impact;

    @Field("highlights")
    private List<String> highlights;

    @Field("company_logo")
    private String companyLogo;

    @Size(max = 500, message = "Company website must not exceed 500 characters")
    @Field("company_website")
    private String companyWebsite;

    @Field("display_order")
    private Integer displayOrder = 0;

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;

}
