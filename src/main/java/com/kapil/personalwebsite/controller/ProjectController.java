package com.kapil.personalwebsite.controller;

import com.kapil.personalwebsite.dto.ApiResponse;
import com.kapil.personalwebsite.entity.Project;
import com.kapil.personalwebsite.exception.ProjectNotFoundException;
import com.kapil.personalwebsite.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for managing Project entities in the personal website.
 * Provides endpoints for public project access.
 *
 * @author Kapil Garg
 */
@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectController.class);

    private final ProjectService projectService;

    /**
     * Retrieves all active projects (public access).
     *
     * @return a ResponseEntity containing the list of all active projects
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Project>>> getAllProjects() {
        LOGGER.info("GET /projects - Fetching all active projects (public)");
        List<Project> projects = projectService.getAllProjects();
        ApiResponse<List<Project>> response = ApiResponse.success(
                projects,
                "Projects retrieved successfully"
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a single active project by ID (public access).
     *
     * @param id project ID
     * @return a ResponseEntity containing the project
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Project>> getProjectById(@PathVariable String id) {
        LOGGER.info("GET /projects/{} - Fetching active project by ID (public)", id);
        Project project = projectService
                .getProjectById(id)
                .orElseThrow(() -> new ProjectNotFoundException(String.format("Project with ID '%s' not found", id)));

        ApiResponse<Project> response = ApiResponse.success(
                project,
                String.format("Project with ID '%s' retrieved successfully", id)
        );
        return ResponseEntity.ok(response);
    }

}
