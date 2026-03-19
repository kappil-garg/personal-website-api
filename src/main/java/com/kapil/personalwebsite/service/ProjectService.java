package com.kapil.personalwebsite.service;

import com.kapil.personalwebsite.entity.Project;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Project operations.
 * Provides read-only access to project information.
 *
 * @author Kapil Garg
 */
public interface ProjectService {

    /**
     * Retrieves all active projects ordered by display order.
     *
     * @return a list of all active projects
     */
    List<Project> getAllProjects();

    /**
     * Retrieves an active project by ID.
     *
     * @param id project ID
     * @return optional active project
     */
    Optional<Project> getProjectById(String id);

}
