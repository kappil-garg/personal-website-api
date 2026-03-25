package com.kapil.personalwebsite.ai.portfolio;

import com.kapil.personalwebsite.entity.*;

import java.util.List;
import java.util.Optional;

/**
 * Aggregates all portfolio domain data into a single dependency.
 * Services that need read access to the full portfolio (RAG context building, vector indexing) use this interface.
 *
 * @author Kapil Garg
 */
public interface PortfolioDataProvider {

    Optional<PersonalInfo> getPersonalInfo();

    List<Experience> getAllExperiences();

    List<Project> getAllProjects();

    List<Education> getAllEducations();

    List<Certification> getAllCertifications();

    List<Skill> getAllSkills();

    List<Blog> getPublishedBlogs();

}
