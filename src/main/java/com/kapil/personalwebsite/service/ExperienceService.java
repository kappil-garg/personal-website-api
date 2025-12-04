package com.kapil.personalwebsite.service;

import com.kapil.personalwebsite.entity.Experience;

import java.util.List;

/**
 * Service interface for Experience operations.
 * Provides read-only access to work experience information.
 *
 * @author Kapil Garg
 */
public interface ExperienceService {

    /**
     * Retrieves all experiences ordered by display order.
     *
     * @return a list of all experiences
     */
    List<Experience> getAllExperiences();

}
