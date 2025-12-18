package com.kapil.personalwebsite.controller;

import com.kapil.personalwebsite.dto.ApiResponse;
import com.kapil.personalwebsite.entity.Skill;
import com.kapil.personalwebsite.service.SkillService;
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
class SkillControllerTest {

    @Mock
    private SkillService skillService;

    @InjectMocks
    private SkillController skillController;

    @Test
    void getAllSkills_WhenSkillsExist_ShouldReturnSuccessResponse() {
        List<Skill> skills = Arrays.asList(
                createSkill("1", "Programming Languages", 1),
                createSkill("2", "Backend Frameworks", 2)
        );
        when(skillService.getAllSkills()).thenReturn(skills);
        ResponseEntity<ApiResponse<List<Skill>>> response = skillController.getAllSkills();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Skills retrieved successfully", response.getBody().getMessage());
        assertEquals(2, response.getBody().getData().size());
        verify(skillService).getAllSkills();
    }

    @Test
    void getAllSkills_WhenNoSkillsExist_ShouldReturnEmptyList() {
        when(skillService.getAllSkills()).thenReturn(Collections.emptyList());
        ResponseEntity<ApiResponse<List<Skill>>> response = skillController.getAllSkills();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertTrue(response.getBody().getData().isEmpty());
        verify(skillService).getAllSkills();
    }

    @Test
    void getAllSkills_ShouldReturnApiResponseWrapper() {
        Skill skill = createSkill("1", "Programming Languages", 1);
        when(skillService.getAllSkills()).thenReturn(List.of(skill));
        ResponseEntity<ApiResponse<List<Skill>>> response = skillController.getAllSkills();
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getTimestamp());
        assertNull(response.getBody().getStatus());
    }

    private Skill createSkill(String id, String categoryName, Integer displayOrder) {
        Skill skill = new Skill();
        skill.setId(id);
        skill.setCategoryName(categoryName);
        skill.setDisplayOrder(displayOrder);
        skill.setSkills(Arrays.asList("Java", "Python"));
        return skill;
    }

}
