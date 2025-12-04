package com.kapil.personalwebsite.repository;

import com.kapil.personalwebsite.entity.Experience;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Experience entity operations in the database.
 *
 * @author Kapil Garg
 */
@Repository
public interface ExperienceRepository extends MongoRepository<Experience, String> {

    /**
     * Finds all experiences ordered by display order descending (most recent first).
     *
     * @return a list of experiences sorted by display order
     */
    List<Experience> findAllByOrderByDisplayOrderDesc();

}
