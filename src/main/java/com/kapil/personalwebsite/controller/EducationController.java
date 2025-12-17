package com.kapil.personalwebsite.controller;

import com.kapil.personalwebsite.dto.ApiResponse;
import com.kapil.personalwebsite.entity.Education;
import com.kapil.personalwebsite.service.EducationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for managing Education entities in the personal website.
 *
 * @author Kapil Garg
 */
@RestController
@RequestMapping("/educations")
@RequiredArgsConstructor
public class EducationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(EducationController.class);

    private final EducationService educationService;

    /**
     * Retrieves all educations (public access).
     *
     * @return a ResponseEntity containing the list of all educations
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Education>>> getAllEducations() {
        LOGGER.info("GET /educations - Fetching all educations (public)");
        List<Education> educations = educationService.getAllEducations();
        ApiResponse<List<Education>> response = ApiResponse.success(
                educations,
                "Educations retrieved successfully"
        );
        return ResponseEntity.ok(response);
    }

}
