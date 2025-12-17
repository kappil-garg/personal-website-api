package com.kapil.personalwebsite.service;

import com.kapil.personalwebsite.entity.Education;

import java.util.List;

/**
 * Service interface for Education operations.
 * Provides read-only access to education information.
 *
 * @author Kapil Garg
 */
public interface EducationService {

    /**
     * Retrieves all educations.
     *
     * @return a list of all educations
     */
    List<Education> getAllEducations();

}
