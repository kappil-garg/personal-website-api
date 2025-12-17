package com.kapil.personalwebsite.service;

import com.kapil.personalwebsite.entity.Certification;

import java.util.List;

/**
 * Service interface for Certification operations.
 * Provides read-only access to certification information.
 *
 * @author Kapil Garg
 */
public interface CertificationService {

    /**
     * Retrieves all certifications ordered by display order.
     *
     * @return a list of all certifications
     */
    List<Certification> getAllCertifications();

}
