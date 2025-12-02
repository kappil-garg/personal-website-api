package com.kapil.personalwebsite.repository;

import com.kapil.personalwebsite.entity.PersonalInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for PersonalInfo entity operations in the database.
 *
 * @author Kapil Garg
 */
@Repository
public interface PersonalInfoRepository extends MongoRepository<PersonalInfo, String> {

    /**
     * Finds the first personal info record ordered by ID in ascending order.
     *
     * @return an Optional containing the personal info if found
     */
    Optional<PersonalInfo> findFirstByOrderByIdAsc();

}
