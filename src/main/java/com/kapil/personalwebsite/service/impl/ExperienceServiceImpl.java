package com.kapil.personalwebsite.service.impl;

import com.kapil.personalwebsite.entity.Experience;
import com.kapil.personalwebsite.repository.ExperienceRepository;
import com.kapil.personalwebsite.service.ExperienceService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of ExperienceService for work experience operations in the personal website.
 * Provides read-only access to experience information for public access.
 *
 * @author Kapil Garg
 */
@Service
@RequiredArgsConstructor
public class ExperienceServiceImpl implements ExperienceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExperienceServiceImpl.class);

    private final ExperienceRepository experienceRepository;

    /**
     * Retrieves all experiences ordered by display order (most recent first).
     *
     * @return a list of all experiences
     */
    @Override
    @Transactional(readOnly = true)
    public List<Experience> getAllExperiences() {
        LOGGER.info("Fetching all experiences for public access");
        return experienceRepository.findAllByOrderByDisplayOrderDesc();
    }

}
