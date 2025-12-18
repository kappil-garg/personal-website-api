package com.kapil.personalwebsite.repository;

import com.kapil.personalwebsite.entity.Skill;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Skill entity operations in the database.
 *
 * @author Kapil Garg
 */
@Repository
public interface SkillRepository extends MongoRepository<Skill, String> {

}
