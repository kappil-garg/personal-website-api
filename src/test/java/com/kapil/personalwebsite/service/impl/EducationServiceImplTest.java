package com.kapil.personalwebsite.service.impl;

import com.kapil.personalwebsite.entity.Education;
import com.kapil.personalwebsite.repository.EducationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EducationServiceImplTest {

    @Mock
    private EducationRepository educationRepository;

    @InjectMocks
    private EducationServiceImpl educationService;

    @Test
    void getAllEducations_WithCompletedEducation_ShouldSortByEndDate() {
        Education edu1 = createEducation("1", "01-2020", "12-2023", false);
        Education edu2 = createEducation("2", "01-2018", "06-2024", false);
        Education edu3 = createEducation("3", "01-2021", "03-2022", false);
        when(educationRepository.findAll()).thenReturn(Arrays.asList(edu1, edu2, edu3));
        List<Education> result = educationService.getAllEducations();
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("2", result.get(0).getId());
        assertEquals("1", result.get(1).getId());
        assertEquals("3", result.get(2).getId());
        verify(educationRepository).findAll();
    }

    @Test
    void getAllEducations_WithCurrentEducation_ShouldSortByStartDate() {
        Education edu1 = createEducation("1", "01-2020", "12-2023", false);
        Education edu2 = createEducation("2", "01-2024", null, true);
        Education edu3 = createEducation("3", "01-2022", null, true);
        when(educationRepository.findAll()).thenReturn(Arrays.asList(edu1, edu2, edu3));
        List<Education> result = educationService.getAllEducations();
        assertEquals("2", result.get(0).getId());
        assertEquals("1", result.get(1).getId());
        assertEquals("3", result.get(2).getId());
        verify(educationRepository).findAll();
    }

    @Test
    void getAllEducations_WithNullEndDate_ShouldUseStartDate() {
        Education edu1 = createEducation("1", "01-2020", null, false);
        Education edu2 = createEducation("2", "01-2024", null, false);
        when(educationRepository.findAll()).thenReturn(Arrays.asList(edu1, edu2));
        List<Education> result = educationService.getAllEducations();
        assertEquals("2", result.get(0).getId());
        assertEquals("1", result.get(1).getId());
        verify(educationRepository).findAll();
    }

    @Test
    void getAllEducations_WithNullDates_ShouldPutNullsLast() {
        Education edu1 = createEducation("1", "01-2023", null, false);
        Education edu2 = createEducation("2", null, null, false);
        Education edu3 = createEducation("3", "01-2024", null, false);
        when(educationRepository.findAll()).thenReturn(Arrays.asList(edu1, edu2, edu3));
        List<Education> result = educationService.getAllEducations();
        assertEquals("3", result.get(0).getId());
        assertEquals("1", result.get(1).getId());
        assertEquals("2", result.get(2).getId());
        verify(educationRepository).findAll();
    }

    @Test
    void getAllEducations_WithEmptyList_ShouldReturnEmptyList() {
        when(educationRepository.findAll()).thenReturn(List.of());
        List<Education> result = educationService.getAllEducations();
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(educationRepository).findAll();
    }

    private Education createEducation(String id, String startDate, String endDate, Boolean isCurrent) {
        Education edu = new Education();
        edu.setId(id);
        edu.setDegree("Test Degree");
        edu.setFieldOfStudy("Test Field");
        edu.setInstitutionName("Test Institution");
        edu.setStartDate(startDate);
        edu.setEndDate(endDate);
        edu.setIsCurrent(isCurrent);
        return edu;
    }

}
