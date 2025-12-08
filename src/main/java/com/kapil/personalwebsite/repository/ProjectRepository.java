package com.kapil.personalwebsite.repository;

import com.kapil.personalwebsite.entity.Project;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Project entity operations in the database.
 *
 * @author Kapil Garg
 */
@Repository
public interface ProjectRepository extends MongoRepository<Project, String> {

    /**
     * Finds all active projects ordered by display order (highest display order first).
     *
     * @return a list of active projects sorted by display order (descending)
     */
    List<Project> findByIsActiveTrueOrderByDisplayOrderDesc();

}
