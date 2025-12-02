package com.kapil.personalwebsite.controller;

import com.kapil.personalwebsite.dto.ApiResponse;
import com.kapil.personalwebsite.entity.PersonalInfo;
import com.kapil.personalwebsite.service.PersonalInfoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST controller for managing PersonalInfo/Portfolio entities.
 * Provides endpoints for public portfolio access and admin management.
 *
 * @author Kapil Garg
 */
@RestController
@RequestMapping("/portfolio")
@RequiredArgsConstructor
public class PersonalInfoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersonalInfoController.class);

    private final PersonalInfoService personalInfoService;

    /**
     * Retrieves the personal information/portfolio (public access).
     *
     * @return a ResponseEntity containing the personal info if found, or a 404 status if not found
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PersonalInfo>> getPersonalInfo() {
        LOGGER.info("GET /portfolio - Fetching personal information (public)");
        Optional<PersonalInfo> personalInfo = personalInfoService.getPersonalInfo();
        if (personalInfo.isPresent()) {
            ApiResponse<PersonalInfo> response = ApiResponse.success(
                    personalInfo.get(),
                    "Personal information retrieved successfully"
            );
            return ResponseEntity.ok(response);
        } else {
            ApiResponse<PersonalInfo> response = ApiResponse.error(
                    "Personal information not found",
                    HttpStatus.NOT_FOUND.value()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Updates the personal information (admin only).
     *
     * @param personalInfo the updated personal information
     * @return a ResponseEntity containing the updated personal information wrapped in ApiResponse
     */
    @PutMapping
    public ResponseEntity<ApiResponse<PersonalInfo>> updatePersonalInfo(@Valid @RequestBody PersonalInfo personalInfo) {
        LOGGER.info("PUT /portfolio - Updating personal information (admin)");
        PersonalInfo updatedInfo = personalInfoService.updatePersonalInfo(personalInfo);
        ApiResponse<PersonalInfo> response = ApiResponse.success(
                updatedInfo,
                "Personal information updated successfully"
        );
        return ResponseEntity.ok(response);
    }

}
