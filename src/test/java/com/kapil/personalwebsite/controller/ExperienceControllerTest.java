package com.kapil.personalwebsite.controller;

import com.kapil.personalwebsite.dto.ApiResponse;
import com.kapil.personalwebsite.entity.Experience;
import com.kapil.personalwebsite.service.ExperienceService;
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
class ExperienceControllerTest {

    @Mock
    private ExperienceService experienceService;

    @InjectMocks
    private ExperienceController experienceController;

    private static List<Experience> getExperienceList() {
        Experience experience1 = createExperience("exp-1", "Company A", "Software Engineer",
                "01-2019", "11-2022", false, 2);
        Experience experience2 = createExperience("exp-2", "Company B", "Senior Engineer",
                "12-2022", null, true, 3);
        return Arrays.asList(experience1, experience2);
    }

    private static Experience createExperience(String id, String companyName, String position,
                                               String startDate, String endDate, boolean isCurrent, int displayOrder) {
        Experience experience = new Experience();
        experience.setId(id);
        experience.setCompanyName(companyName);
        experience.setPosition(position);
        experience.setStartDate(startDate);
        experience.setEndDate(endDate);
        experience.setIsCurrent(isCurrent);
        experience.setDisplayOrder(displayOrder);
        return experience;
    }

    @Test
    void getAllExperiences_WhenExperiencesExist_ShouldReturnSuccessResponse() {
        List<Experience> experiences = getExperienceList();
        when(experienceService.getAllExperiences()).thenReturn(experiences);
        ResponseEntity<ApiResponse<List<Experience>>> response = experienceController.getAllExperiences();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Experiences retrieved successfully", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        assertEquals(2, response.getBody().getData().size());
        assertEquals("Company A", response.getBody().getData().get(0).getCompanyName());
        assertEquals("Company B", response.getBody().getData().get(1).getCompanyName());
        verify(experienceService).getAllExperiences();
    }

    @Test
    void getAllExperiences_WhenNoExperiencesExist_ShouldReturnEmptyList() {
        List<Experience> emptyList = Collections.emptyList();
        when(experienceService.getAllExperiences()).thenReturn(emptyList);
        ResponseEntity<ApiResponse<List<Experience>>> response = experienceController.getAllExperiences();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Experiences retrieved successfully", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        assertTrue(response.getBody().getData().isEmpty());
        verify(experienceService).getAllExperiences();
    }

    @Test
    void getAllExperiences_ShouldCallServiceMethod() {
        List<Experience> experiences = Collections.emptyList();
        when(experienceService.getAllExperiences()).thenReturn(experiences);
        experienceController.getAllExperiences();
        verify(experienceService).getAllExperiences();
    }

    @Test
    void getAllExperiences_ShouldReturnApiResponseWrapper() {
        Experience experience = createExperience("exp-1", "Test Company", "Test Position",
                "01-2019", null, false, 1);
        List<Experience> experiences = List.of(experience);
        when(experienceService.getAllExperiences()).thenReturn(experiences);
        ResponseEntity<ApiResponse<List<Experience>>> response = experienceController.getAllExperiences();
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getTimestamp());
        assertNull(response.getBody().getStatus());
    }

}
