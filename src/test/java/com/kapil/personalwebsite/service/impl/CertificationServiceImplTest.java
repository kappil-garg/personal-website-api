package com.kapil.personalwebsite.service.impl;

import com.kapil.personalwebsite.entity.Certification;
import com.kapil.personalwebsite.repository.CertificationRepository;
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
class CertificationServiceImplTest {

    @Mock
    private CertificationRepository certificationRepository;

    @InjectMocks
    private CertificationServiceImpl certificationService;

    @Test
    void getAllCertifications_ShouldReturnSortedByDateDescending() {
        Certification cert1 = createCertification("1", "06-2023");
        Certification cert2 = createCertification("2", "11-2024");
        Certification cert3 = createCertification("3", "03-2022");
        Certification cert4 = createCertification("4", null);
        when(certificationRepository.findAll()).thenReturn(Arrays.asList(cert1, cert2, cert3, cert4));
        List<Certification> result = certificationService.getAllCertifications();
        assertNotNull(result);
        assertEquals(4, result.size());
        assertEquals("2", result.get(0).getId());
        assertEquals("1", result.get(1).getId());
        assertEquals("3", result.get(2).getId());
        assertEquals("4", result.get(3).getId());
        verify(certificationRepository).findAll();
    }

    @Test
    void getAllCertifications_WithEmptyList_ShouldReturnEmptyList() {
        when(certificationRepository.findAll()).thenReturn(List.of());
        List<Certification> result = certificationService.getAllCertifications();
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(certificationRepository).findAll();
    }

    @Test
    void getAllCertifications_WithNullDates_ShouldPutNullsLast() {
        Certification cert1 = createCertification("1", "12-2023");
        Certification cert2 = createCertification("2", null);
        Certification cert3 = createCertification("3", "06-2025");
        when(certificationRepository.findAll()).thenReturn(Arrays.asList(cert1, cert2, cert3));
        List<Certification> result = certificationService.getAllCertifications();
        assertEquals("3", result.get(0).getId());
        assertEquals("1", result.get(1).getId());
        assertEquals("2", result.get(2).getId());
        verify(certificationRepository).findAll();
    }

    private Certification createCertification(String id, String issueDate) {
        Certification cert = new Certification();
        cert.setId(id);
        cert.setCertificationName("Test Certification");
        cert.setIssuingOrganization("Test Org");
        cert.setIssueDate(issueDate);
        return cert;
    }

}
