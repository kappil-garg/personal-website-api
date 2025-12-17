package com.kapil.personalwebsite.repository;

import com.kapil.personalwebsite.entity.Education;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Education entity operations in the database.
 *
 * @author Kapil Garg
 */
@Repository
public interface EducationRepository extends MongoRepository<Education, String> {

}
