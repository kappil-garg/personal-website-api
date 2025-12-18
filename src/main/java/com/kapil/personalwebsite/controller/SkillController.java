package com.kapil.personalwebsite.controller;

import com.kapil.personalwebsite.dto.ApiResponse;
import com.kapil.personalwebsite.entity.Skill;
import com.kapil.personalwebsite.service.SkillService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for managing Skill entities in the personal website.
 *
 * @author Kapil Garg
 */
@RestController
@RequestMapping("/skills")
@RequiredArgsConstructor
public class SkillController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SkillController.class);

    private final SkillService skillService;

    /**
     * Retrieves all skills (public access).
     *
     * @return a ResponseEntity containing the list of all skills
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Skill>>> getAllSkills() {
        LOGGER.info("GET /skills - Fetching all skills (public)");
        List<Skill> skills = skillService.getAllSkills();
        ApiResponse<List<Skill>> response = ApiResponse.success(
                skills,
                "Skills retrieved successfully"
        );
        return ResponseEntity.ok(response);
    }

}
