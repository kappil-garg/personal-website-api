package com.kapil.personalwebsite.service.impl;

import com.kapil.personalwebsite.entity.Project;
import com.kapil.personalwebsite.repository.ProjectRepository;
import com.kapil.personalwebsite.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of ProjectService for project operations in the personal website.
 * Provides read-only access to project information for public access.
 *
 * @author Kapil Garg
 */
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectServiceImpl.class);

    private final ProjectRepository projectRepository;

    /**
     * Retrieves all active projects ordered by display order (highest display order first).
     *
     * @return a list of all active projects
     */
    @Override
    @Transactional(readOnly = true)
    public List<Project> getAllProjects() {
        LOGGER.info("Fetching all active projects for public access");
        return projectRepository.findByIsActiveTrueOrderByDisplayOrderDesc();
    }

}
