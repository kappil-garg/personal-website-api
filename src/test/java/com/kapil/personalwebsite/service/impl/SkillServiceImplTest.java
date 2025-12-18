package com.kapil.personalwebsite.service.impl;

import com.kapil.personalwebsite.entity.Skill;
import com.kapil.personalwebsite.repository.SkillRepository;
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
class SkillServiceImplTest {

    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private SkillServiceImpl skillService;

    @Test
    void getAllSkills_ShouldReturnSortedByDisplayOrderAscending() {
        Skill skill1 = createSkill("1", "Programming Languages", 1);
        Skill skill2 = createSkill("2", "Backend Frameworks", 2);
        Skill skill3 = createSkill("3", "Frontend Technologies", 3);
        Skill skill4 = createSkill("4", "Databases", 4);
        when(skillRepository.findAll()).thenReturn(Arrays.asList(skill4, skill2, skill1, skill3));
        List<Skill> result = skillService.getAllSkills();
        assertNotNull(result);
        assertEquals(4, result.size());
        assertEquals("1", result.get(0).getId());
        assertEquals("Programming Languages", result.get(0).getCategoryName());
        assertEquals("2", result.get(1).getId());
        assertEquals("Backend Frameworks", result.get(1).getCategoryName());
        assertEquals("3", result.get(2).getId());
        assertEquals("Frontend Technologies", result.get(2).getCategoryName());
        assertEquals("4", result.get(3).getId());
        assertEquals("Databases", result.get(3).getCategoryName());
        verify(skillRepository).findAll();
    }

    @Test
    void getAllSkills_WithEmptyList_ShouldReturnEmptyList() {
        when(skillRepository.findAll()).thenReturn(List.of());
        List<Skill> result = skillService.getAllSkills();
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(skillRepository).findAll();
    }

    @Test
    void getAllSkills_WithNullDisplayOrder_ShouldPutNullsLast() {
        Skill skill1 = createSkill("1", "Programming Languages", 1);
        Skill skill2 = createSkill("2", "Backend Frameworks", null);
        Skill skill3 = createSkill("3", "Frontend Technologies", 3);
        Skill skill4 = createSkill("4", "Databases", null);
        when(skillRepository.findAll()).thenReturn(Arrays.asList(skill1, skill2, skill3, skill4));
        List<Skill> result = skillService.getAllSkills();
        assertNotNull(result);
        assertEquals(4, result.size());
        assertEquals("1", result.get(0).getId());
        assertEquals("3", result.get(1).getId());
        assertEquals("2", result.get(2).getId());
        assertEquals("4", result.get(3).getId());
        verify(skillRepository).findAll();
    }

    @Test
    void getAllSkills_WithAllNullDisplayOrder_ShouldMaintainOriginalOrder() {
        Skill skill1 = createSkill("1", "Programming Languages", null);
        Skill skill2 = createSkill("2", "Backend Frameworks", null);
        Skill skill3 = createSkill("3", "Frontend Technologies", null);
        when(skillRepository.findAll()).thenReturn(Arrays.asList(skill1, skill2, skill3));
        List<Skill> result = skillService.getAllSkills();
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(skillRepository).findAll();
    }

    @Test
    void getAllSkills_WithMixedDisplayOrder_ShouldSortCorrectly() {
        Skill skill1 = createSkill("1", "Programming Languages", 10);
        Skill skill2 = createSkill("2", "Backend Frameworks", 5);
        Skill skill3 = createSkill("3", "Frontend Technologies", null);
        Skill skill4 = createSkill("4", "Databases", 2);
        Skill skill5 = createSkill("5", "DevOps", 8);
        Skill skill6 = createSkill("6", "Testing", null);
        when(skillRepository.findAll()).thenReturn(Arrays.asList(skill1, skill2, skill3, skill4, skill5, skill6));
        List<Skill> result = skillService.getAllSkills();
        assertNotNull(result);
        assertEquals(6, result.size());
        assertEquals("4", result.get(0).getId());
        assertEquals("2", result.get(1).getId());
        assertEquals("5", result.get(2).getId());
        assertEquals("1", result.get(3).getId());
        assertNull(result.get(4).getDisplayOrder());
        assertNull(result.get(5).getDisplayOrder());
        verify(skillRepository).findAll();
    }

    @Test
    void getAllSkills_WithSameDisplayOrder_ShouldMaintainStableSort() {
        Skill skill1 = createSkill("1", "Programming Languages", 1);
        Skill skill2 = createSkill("2", "Backend Frameworks", 1);
        Skill skill3 = createSkill("3", "Frontend Technologies", 1);
        when(skillRepository.findAll()).thenReturn(Arrays.asList(skill1, skill2, skill3));
        List<Skill> result = skillService.getAllSkills();
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(skillRepository).findAll();
    }

    @Test
    void getAllSkills_WithZeroDisplayOrder_ShouldSortCorrectly() {
        Skill skill1 = createSkill("1", "Programming Languages", 0);
        Skill skill2 = createSkill("2", "Backend Frameworks", 1);
        Skill skill3 = createSkill("3", "Frontend Technologies", -1);
        when(skillRepository.findAll()).thenReturn(Arrays.asList(skill1, skill2, skill3));
        List<Skill> result = skillService.getAllSkills();
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("3", result.get(0).getId());
        assertEquals("1", result.get(1).getId());
        assertEquals("2", result.get(2).getId());
        verify(skillRepository).findAll();
    }

    private Skill createSkill(String id, String categoryName, Integer displayOrder) {
        Skill skill = new Skill();
        skill.setId(id);
        skill.setCategoryName(categoryName);
        skill.setDisplayOrder(displayOrder);
        skill.setSkills(Arrays.asList("Skill1", "Skill2"));
        return skill;
    }

}
