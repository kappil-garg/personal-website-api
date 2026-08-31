package com.kapil.personalwebsite.service.impl;

import com.kapil.personalwebsite.entity.PersonalInfo;
import com.kapil.personalwebsite.repository.PersonalInfoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonalInfoServiceImplTest {

    @Mock
    private PersonalInfoRepository personalInfoRepository;

    @InjectMocks
    private PersonalInfoServiceImpl personalInfoService;

    @Test
    void getPersonalInfo_ShouldReturnRepositoryValue() {
        PersonalInfo personalInfo = createPersonalInfo("existing-id", "Existing Name");
        when(personalInfoRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(personalInfo));
        Optional<PersonalInfo> result = personalInfoService.getPersonalInfo();
        assertTrue(result.isPresent());
        assertSame(personalInfo, result.get());
        verify(personalInfoRepository).findFirstByOrderByIdAsc();
    }

    @Test
    void getPersonalInfo_WhenNoRecordExists_ShouldReturnEmpty() {
        when(personalInfoRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());
        Optional<PersonalInfo> result = personalInfoService.getPersonalInfo();
        assertTrue(result.isEmpty());
        verify(personalInfoRepository).findFirstByOrderByIdAsc();
    }

    @Test
    void updatePersonalInfo_WhenRecordExists_ShouldUpdateExistingEntityAndSaveIt() {
        PersonalInfo existing = createPersonalInfo("existing-id", "Existing Name");
        PersonalInfo incoming = createPersonalInfo("new-id", "Updated Name");
        incoming.setTagline("Updated tagline");
        incoming.setEmail("updated@example.com");
        when(personalInfoRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(existing));
        when(personalInfoRepository.save(existing)).thenReturn(existing);
        PersonalInfo result = personalInfoService.updatePersonalInfo(incoming);
        ArgumentCaptor<PersonalInfo> captor = ArgumentCaptor.forClass(PersonalInfo.class);
        verify(personalInfoRepository).save(captor.capture());
        PersonalInfo saved = captor.getValue();
        assertSame(existing, result);
        assertSame(existing, saved);
        assertEquals("existing-id", saved.getId());
        assertEquals("Updated Name", saved.getName());
        assertEquals("Updated tagline", saved.getTagline());
        assertEquals(incoming.getDescription(), saved.getDescription());
        assertEquals(incoming.getProfileImage(), saved.getProfileImage());
        assertEquals("updated@example.com", saved.getEmail());
        assertEquals(incoming.getPhone(), saved.getPhone());
        assertEquals(incoming.getLocation(), saved.getLocation());
        assertEquals(incoming.getSocialLinks(), saved.getSocialLinks());
    }

    @Test
    void updatePersonalInfo_WhenNoRecordExists_ShouldSaveIncomingEntity() {
        PersonalInfo incoming = createPersonalInfo("new-id", "New Name");
        when(personalInfoRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());
        when(personalInfoRepository.save(incoming)).thenReturn(incoming);
        PersonalInfo result = personalInfoService.updatePersonalInfo(incoming);
        assertSame(incoming, result);
        verify(personalInfoRepository).save(incoming);
    }

    private PersonalInfo createPersonalInfo(String id, String name) {
        PersonalInfo.SocialLinks socialLinks = new PersonalInfo.SocialLinks(
                "https://github.com/test",
                "https://linkedin.com/in/test",
                "https://twitter.com/test",
                "https://example.com"
        );
        return new PersonalInfo(
                id,
                name,
                "Tagline",
                List.of("Line 1", "Line 2"),
                "profile.png",
                "test@example.com",
                "1234567890",
                "Earth",
                socialLinks,
                null
        );
    }

}
