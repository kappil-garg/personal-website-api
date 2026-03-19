package com.kapil.personalwebsite.controller;

import com.kapil.personalwebsite.dto.ApiResponse;
import com.kapil.personalwebsite.entity.Project;
import com.kapil.personalwebsite.exception.ProjectNotFoundException;
import com.kapil.personalwebsite.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectControllerTest {

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private ProjectController projectController;

    private static List<Project> getProjectList() {
        Project project1 = createProject("project-1", "Project A", "Short description A", 1);
        Project project2 = createProject("project-2", "Project B", "Short description B", 2);
        return Arrays.asList(project1, project2);
    }

    private static Project createProject(String id, String title, String shortDescription, int displayOrder) {
        Project project = new Project();
        project.setId(id);
        project.setTitle(title);
        project.setShortDescription(shortDescription);
        project.setFeaturedImage("https://example.com/project.png");
        project.setIsActive(true);
        project.setDisplayOrder(displayOrder);
        return project;
    }

    @Test
    void getAllProjects_WhenProjectsExist_ShouldReturnSuccessResponse() {
        List<Project> projects = getProjectList();
        when(projectService.getAllProjects()).thenReturn(projects);
        ResponseEntity<ApiResponse<List<Project>>> response = projectController.getAllProjects();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Projects retrieved successfully", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        assertEquals(2, response.getBody().getData().size());
        assertEquals("Project A", response.getBody().getData().get(0).getTitle());
        assertEquals("Project B", response.getBody().getData().get(1).getTitle());
        verify(projectService).getAllProjects();
    }

    @Test
    void getAllProjects_WhenNoProjectsExist_ShouldReturnEmptyList() {
        List<Project> emptyList = Collections.emptyList();
        when(projectService.getAllProjects()).thenReturn(emptyList);
        ResponseEntity<ApiResponse<List<Project>>> response = projectController.getAllProjects();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Projects retrieved successfully", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        assertTrue(response.getBody().getData().isEmpty());
        verify(projectService).getAllProjects();
    }

    @Test
    void getAllProjects_ShouldCallServiceMethod() {
        List<Project> projects = Collections.emptyList();
        when(projectService.getAllProjects()).thenReturn(projects);
        projectController.getAllProjects();
        verify(projectService).getAllProjects();
    }

    @Test
    void getAllProjects_ShouldReturnApiResponseWrapper() {
        Project project = createProject("project-1", "Test Project", "Test short description", 1);
        List<Project> projects = List.of(project);
        when(projectService.getAllProjects()).thenReturn(projects);
        ResponseEntity<ApiResponse<List<Project>>> response = projectController.getAllProjects();
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getTimestamp());
        assertNull(response.getBody().getStatus());
    }

    @Test
    void getProjectById_WhenProjectExists_ShouldReturnSuccessResponse() {
        String projectId = "project-1";
        Project project = createProject(projectId, "Project A", "Short description A", 1);
        when(projectService.getProjectById(projectId)).thenReturn(Optional.of(project));
        ResponseEntity<ApiResponse<Project>> response = projectController.getProjectById(projectId);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(String.format("Project with ID '%s' retrieved successfully", projectId), response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        assertEquals(projectId, response.getBody().getData().getId());
        assertEquals("Project A", response.getBody().getData().getTitle());
        verify(projectService).getProjectById(projectId);
    }

    @Test
    void getProjectById_WhenProjectMissing_ShouldThrowProjectNotFoundException() {
        String missingProjectId = "missing-project-id";
        when(projectService.getProjectById(missingProjectId)).thenReturn(Optional.empty());

        ProjectNotFoundException exception = assertThrows(
                ProjectNotFoundException.class,
                () -> projectController.getProjectById(missingProjectId)
        );
        assertEquals(String.format("Project with ID '%s' not found", missingProjectId), exception.getMessage());
        verify(projectService).getProjectById(missingProjectId);
    }

}
