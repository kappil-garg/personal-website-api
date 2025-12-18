package com.kapil.personalwebsite.service;

import com.kapil.personalwebsite.entity.Skill;

import java.util.List;

/**
 * Service interface for Skill operations.
 * Provides read-only access to skill information.
 *
 * @author Kapil Garg
 */
public interface SkillService {

    /**
     * Retrieves all skills ordered by display order.
     *
     * @return a list of all skills sorted by display order
     */
    List<Skill> getAllSkills();

}
