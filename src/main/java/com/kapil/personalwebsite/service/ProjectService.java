package com.kapil.personalwebsite.service;

import com.kapil.personalwebsite.entity.Project;

import java.util.List;

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

}
