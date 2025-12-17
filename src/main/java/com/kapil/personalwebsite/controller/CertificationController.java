package com.kapil.personalwebsite.controller;

import com.kapil.personalwebsite.dto.ApiResponse;
import com.kapil.personalwebsite.entity.Certification;
import com.kapil.personalwebsite.service.CertificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for managing Certification entities in the personal website.
 *
 * @author Kapil Garg
 */
@RestController
@RequestMapping("/certifications")
@RequiredArgsConstructor
public class CertificationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CertificationController.class);

    private final CertificationService certificationService;

    /**
     * Retrieves all certifications (public access).
     *
     * @return a ResponseEntity containing the list of all certifications
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Certification>>> getAllCertifications() {
        LOGGER.info("GET /certifications - Fetching all certifications (public)");
        List<Certification> certifications = certificationService.getAllCertifications();
        ApiResponse<List<Certification>> response = ApiResponse.success(
                certifications,
                "Certifications retrieved successfully"
        );
        return ResponseEntity.ok(response);
    }

}
