package com.kapil.personalwebsite.controller;

import com.kapil.personalwebsite.dto.ApiResponse;
import com.kapil.personalwebsite.entity.PersonalInfo;
import com.kapil.personalwebsite.service.PersonalInfoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonalInfoControllerTest {

    @Mock
    private PersonalInfoService personalInfoService;

    @InjectMocks
    private PersonalInfoController personalInfoController;

    private PersonalInfo createSamplePersonalInfo() {
        PersonalInfo.SocialLinks socialLinks = new PersonalInfo.SocialLinks(
                "https://github.com/professor-mosby",
                "https://linkedin.com/in/ted-mosby",
                "https://twitter.com/ted_mosby",
                "https://architecturebyted.com"
        );
        PersonalInfo personalInfo = new PersonalInfo();
        personalInfo.setId("ted-mosby-id");
        personalInfo.setName("Ted Mosby");
        personalInfo.setTagline("Architect. Professor. Hopeless romantic.");
        personalInfo.setDescription(List.of(
                "Professor of Architecture at Columbia University.",
                "Former architect with a passion for designing buildings that tell stories.",
                "On a lifelong journey to find 'the one' in New York City."
        ));
        personalInfo.setProfileImage("ted-mosby-profile.jpg");
        personalInfo.setEmail("ted.mosby@example.com");
        personalInfo.setPhone("+1-212-555-0133");
        personalInfo.setLocation("New York City");
        personalInfo.setSocialLinks(socialLinks);
        personalInfo.setUpdatedAt(LocalDateTime.now());
        return personalInfo;
    }

    @Test
    void getPersonalInfo_WhenInfoExists_ShouldReturnWrappedPersonalInfo() {
        PersonalInfo personalInfo = createSamplePersonalInfo();
        when(personalInfoService.getPersonalInfo()).thenReturn(Optional.of(personalInfo));
        var response = personalInfoController.getPersonalInfo();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<PersonalInfo> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
        assertEquals("Personal information retrieved successfully", body.getMessage());
        assertNotNull(body.getData());
        assertEquals("Ted Mosby", body.getData().getName());
    }

    @Test
    void getPersonalInfo_WhenInfoDoesNotExist_ShouldReturnNotFound() {
        when(personalInfoService.getPersonalInfo()).thenReturn(Optional.empty());
        var response = personalInfoController.getPersonalInfo();
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ApiResponse<PersonalInfo> body = response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals("Personal information not found", body.getMessage());
        assertNull(body.getData());
    }

    @Test
    void updatePersonalInfo_ShouldUpdateAndReturnWrappedPersonalInfo() {
        PersonalInfo requestInfo = createSamplePersonalInfo();
        PersonalInfo updatedInfo = createSamplePersonalInfo();
        updatedInfo.setName("Updated User");
        when(personalInfoService.updatePersonalInfo(requestInfo)).thenReturn(updatedInfo);
        var response = personalInfoController.updatePersonalInfo(requestInfo);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<PersonalInfo> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
        assertEquals("Personal information updated successfully", body.getMessage());
        assertNotNull(body.getData());
        assertEquals("Updated User", body.getData().getName());
    }

}
