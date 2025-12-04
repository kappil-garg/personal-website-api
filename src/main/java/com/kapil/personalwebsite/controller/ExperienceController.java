package com.kapil.personalwebsite.controller;

import com.kapil.personalwebsite.dto.ApiResponse;
import com.kapil.personalwebsite.entity.Experience;
import com.kapil.personalwebsite.service.ExperienceService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for managing Experience entities in the personal website.
 *
 * @author Kapil Garg
 */
@RestController
@RequestMapping("/experiences")
@RequiredArgsConstructor
public class ExperienceController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExperienceController.class);

    private final ExperienceService experienceService;

    /**
     * Retrieves all experiences (public access).
     *
     * @return a ResponseEntity containing the list of all experiences
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Experience>>> getAllExperiences() {
        LOGGER.info("GET /experiences - Fetching all experiences (public)");
        List<Experience> experiences = experienceService.getAllExperiences();
        ApiResponse<List<Experience>> response = ApiResponse.success(
                experiences,
                "Experiences retrieved successfully"
        );
        return ResponseEntity.ok(response);
    }

}
