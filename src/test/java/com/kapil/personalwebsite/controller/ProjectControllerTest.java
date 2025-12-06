package com.kapil.personalwebsite.controller;

import com.kapil.personalwebsite.dto.ApiResponse;
import com.kapil.personalwebsite.entity.Project;
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
        Project project1 = createProject("project-1", "Project A", "Description A", 1);
        Project project2 = createProject("project-2", "Project B", "Description B", 2);
        return Arrays.asList(project1, project2);
    }

    private static Project createProject(String id, String title, String description, int displayOrder) {
        Project project = new Project();
        project.setId(id);
        project.setTitle(title);
        project.setDescription(description);
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
        Project project = createProject("project-1", "Test Project", "Test Description", 1);
        List<Project> projects = List.of(project);
        when(projectService.getAllProjects()).thenReturn(projects);
        ResponseEntity<ApiResponse<List<Project>>> response = projectController.getAllProjects();
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getTimestamp());
        assertNull(response.getBody().getStatus());
    }

}
