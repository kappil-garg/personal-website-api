package com.kapil.personalwebsite.ai.portfolio;

import com.kapil.personalwebsite.entity.*;
import com.kapil.personalwebsite.service.*;
import com.kapil.personalwebsite.service.blog.BlogPublicService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Default implementation of PortfolioDataProvider that delegates to the individual domain services.
 *
 * @author Kapil Garg
 */
@Component
public class PortfolioDataProviderImpl implements PortfolioDataProvider {

    private final PersonalInfoService personalInfoService;
    private final ExperienceService experienceService;
    private final ProjectService projectService;
    private final EducationService educationService;
    private final CertificationService certificationService;
    private final SkillService skillService;
    private final BlogPublicService blogPublicService;

    public PortfolioDataProviderImpl(PersonalInfoService personalInfoService,
                                     ExperienceService experienceService,
                                     ProjectService projectService,
                                     EducationService educationService,
                                     CertificationService certificationService,
                                     SkillService skillService,
                                     BlogPublicService blogPublicService) {
        this.personalInfoService = personalInfoService;
        this.experienceService = experienceService;
        this.projectService = projectService;
        this.educationService = educationService;
        this.certificationService = certificationService;
        this.skillService = skillService;
        this.blogPublicService = blogPublicService;
    }

    @Override
    public Optional<PersonalInfo> getPersonalInfo() {
        return personalInfoService.getPersonalInfo();
    }

    @Override
    public List<Experience> getAllExperiences() {
        return experienceService.getAllExperiences();
    }

    @Override
    public List<Project> getAllProjects() {
        return projectService.getAllProjects();
    }

    @Override
    public List<Education> getAllEducations() {
        return educationService.getAllEducations();
    }

    @Override
    public List<Certification> getAllCertifications() {
        return certificationService.getAllCertifications();
    }

    @Override
    public List<Skill> getAllSkills() {
        return skillService.getAllSkills();
    }

    @Override
    public List<Blog> getPublishedBlogs() {
        return blogPublicService.getPublishedBlogs();
    }

}
