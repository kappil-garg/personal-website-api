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
 * Entity representing Certification or Professional Development Course.
 * Maps to the "certifications" collection in MongoDB.
 *
 * @author Kapil Garg
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "certifications")
public class Certification {

    @Id
    private String id;

    @NotBlank(message = "Certification name is required")
    @Size(max = 200, message = "Certification name must not exceed 200 characters")
    @Field("certification_name")
    private String certificationName;

    @NotBlank(message = "Issuing organization is required")
    @Size(max = 200, message = "Issuing organization must not exceed 200 characters")
    @Field("issuing_organization")
    private String issuingOrganization;

    @Size(max = 7, message = "Issue date must be in MM-YYYY format")
    @Pattern(regexp = "^(0[1-9]|1[0-2])-\\d{4}$", message = "Issue date must be in MM-YYYY format (e.g., 12-2025)")
    @Field("issue_date")
    private String issueDate;

    @Size(max = 7, message = "Expiration date must be in MM-YYYY format")
    @Pattern(regexp = "^(0[1-9]|1[0-2])-\\d{4}$", message = "Expiration date must be in MM-YYYY format (e.g., 12-2025)")
    @Field("expiration_date")
    private String expirationDate;

    @Field("does_not_expire")
    private Boolean doesNotExpire = false;

    @Size(max = 500, message = "Credential ID must not exceed 500 characters")
    @Field("credential_id")
    private String credentialId;

    @Size(max = 1000, message = "Credential URL must not exceed 1000 characters")
    @Field("credential_url")
    private String credentialUrl;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @Field("description")
    private String description;

    @Size(max = 500, message = "Organization logo URL must not exceed 500 characters")
    @Field("organization_logo")
    private String organizationLogo;

    @Size(max = 500, message = "Organization website must not exceed 500 characters")
    @Field("organization_website")
    private String organizationWebsite;

    @Field("display_order")
    private Integer displayOrder = 0;

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;

}
