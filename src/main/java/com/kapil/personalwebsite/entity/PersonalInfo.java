package com.kapil.personalwebsite.entity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity representing Personal Information/Portfolio.
 * Maps to the "personal_info" collection in MongoDB.
 *
 * @author Kapil Garg
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "personal_info")
public class PersonalInfo {

    @Id
    private String id;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    @Field("name")
    private String name;

    @NotBlank(message = "Tagline is required")
    @Size(max = 200, message = "Tagline must not exceed 200 characters")
    @Field("tagline")
    private String tagline;

    @Field("description")
    private List<String> description;

    @NotBlank(message = "Profile image is required")
    @Field("profile_image")
    private String profileImage;

    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Field("email")
    private String email;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    @Field("phone")
    private String phone;

    @Size(max = 100, message = "Location must not exceed 100 characters")
    @Field("location")
    private String location;

    @Field("social_links")
    private SocialLinks socialLinks;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;

    /**
     * Nested class for social media links.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SocialLinks {

        @Field("github")
        private String github;

        @Field("linkedin")
        private String linkedin;

        @Field("twitter")
        private String twitter;

        @Field("website")
        private String website;

    }

}
