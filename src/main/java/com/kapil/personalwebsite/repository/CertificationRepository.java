package com.kapil.personalwebsite.repository;

import com.kapil.personalwebsite.entity.Certification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Certification entity operations in the database.
 *
 * @author Kapil Garg
 */
@Repository
public interface CertificationRepository extends MongoRepository<Certification, String> {

}
