package com.kapil.personalwebsite.ai.portfolio;

import com.kapil.personalwebsite.ai.util.AiTextUtils;
import com.kapil.personalwebsite.ai.util.PortfolioAiConstants;
import com.kapil.personalwebsite.entity.*;
import com.kapil.personalwebsite.service.*;
import com.kapil.personalwebsite.service.blog.BlogPublicService;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Default implementation of PortfolioRagService that reads portfolio data from existing services.
 * Constructs a text context for AI chat and a list of Documents for VectorStore-based RAG retrieval.
 *
 * @author Kapil Garg
 */
@Service
public class PortfolioRagServiceImpl implements PortfolioRagService {

    private static final int MAX_CONTEXT_LENGTH = 12_000;
    private static final int MAX_BLOG_SECTION_LENGTH = 2_500;

    /**
     * Approximate length of the BLOGS section header so we reserve space when capping section size.
     */
    private static final int BLOG_SECTION_HEADER_APPROX_LENGTH = 120;

    private final PersonalInfoService personalInfoService;
    private final ExperienceService experienceService;
    private final ProjectService projectService;
    private final EducationService educationService;
    private final CertificationService certificationService;
    private final SkillService skillService;
    private final BlogPublicService blogPublicService;

    public PortfolioRagServiceImpl(PersonalInfoService personalInfoService, ExperienceService experienceService,
                                   ProjectService projectService, EducationService educationService,
                                   CertificationService certificationService, SkillService skillService,
                                   BlogPublicService blogPublicService) {
        this.personalInfoService = personalInfoService;
        this.experienceService = experienceService;
        this.projectService = projectService;
        this.educationService = educationService;
        this.certificationService = certificationService;
        this.skillService = skillService;
        this.blogPublicService = blogPublicService;
    }

    /**
     * Builds a single-line summary for a published blog (title, slug, category, optional excerpt).
     *
     * @param blog the published blog to summarize
     * @return a single-line summary suitable for appending to context or document content
     */
    private static @NonNull String buildBlogSummary(Blog blog) {
        String line = AiTextUtils.nullSafe(blog.getTitle())
                + " (slug: " + AiTextUtils.nullSafe(blog.getSlug())
                + (blog.getCategory() != null ? ", category: " + blog.getCategory().name() : "")
                + ")";
        String excerpt = blog.getExcerpt();
        if (excerpt != null && !excerpt.isBlank()) {
            String shortExcerpt = excerpt.length() > 80 ? excerpt.substring(0, 80).trim() + "..." : excerpt.trim();
            line += ". " + shortExcerpt;
        }
        return line;
    }

    @Override
    public String buildPortfolioContext() {
        StringBuilder sb = new StringBuilder();
        buildPersonalInfoSection(sb);
        buildExperienceSection(sb);
        buildProjectsSection(sb);
        buildEducationSection(sb);
        buildCertificationsSection(sb);
        buildSkillsSection(sb);
        buildBlogsSection(sb);
        String context = sb.toString().replaceAll("\\s+", " ").trim();
        if (context.length() > MAX_CONTEXT_LENGTH) {
            context = context.substring(0, MAX_CONTEXT_LENGTH) + "...";
        }
        return context;
    }

    @Override
    public List<Document> buildPortfolioDocuments() {
        return Stream.of(
                        personalInfoService.getPersonalInfo().stream().map(this::buildPersonalInfoDocument),
                        experienceService.getAllExperiences().stream().map(this::buildExperienceDocument),
                        projectService.getAllProjects().stream().map(this::buildProjectDocument),
                        educationService.getAllEducations().stream().map(this::buildEducationDocument),
                        certificationService.getAllCertifications().stream().map(this::buildCertificationDocument),
                        skillService.getAllSkills().stream().map(this::buildSkillDocument),
                        blogPublicService.getPublishedBlogs().stream().map(this::buildBlogDocument)
                )
                .flatMap(java.util.function.Function.identity())
                .toList();
    }

    /**
     * Appends a PERSONAL INFO section to the context with name, tagline, summary, and location.
     *
     * @param sb the StringBuilder to append the section to
     */
    private void buildPersonalInfoSection(StringBuilder sb) {
        Optional<PersonalInfo> infoOpt = personalInfoService.getPersonalInfo();
        if (infoOpt.isEmpty()) {
            return;
        }
        PersonalInfo info = infoOpt.get();
        sb.append(PortfolioAiConstants.SECTION_PERSONAL_INFO).append(":\n");
        appendPersonalInfoLines(sb, info);
        sb.append("\n");
    }

    private void buildExperienceSection(StringBuilder sb) {
        AiTextUtils.appendSection(sb, PortfolioAiConstants.SECTION_EXPERIENCE, experienceService.getAllExperiences(), this::appendExperienceLine);
    }

    private void buildProjectsSection(StringBuilder sb) {
        AiTextUtils.appendSection(sb, PortfolioAiConstants.SECTION_PROJECTS, projectService.getAllProjects(), this::appendProjectLine);
    }

    private void buildEducationSection(StringBuilder sb) {
        AiTextUtils.appendSection(sb, PortfolioAiConstants.SECTION_EDUCATION, educationService.getAllEducations(), this::appendEducationLine);
    }

    private void buildCertificationsSection(StringBuilder sb) {
        AiTextUtils.appendSection(sb, PortfolioAiConstants.SECTION_CERTIFICATIONS, certificationService.getAllCertifications(), this::appendCertificationLine);
    }

    private void buildSkillsSection(StringBuilder sb) {
        AiTextUtils.appendSection(sb, PortfolioAiConstants.SECTION_SKILLS, skillService.getAllSkills(), this::appendSkillLine);
    }

    /**
     * Appends a BLOGS section to the context: published post titles, slugs, categories, and short excerpts.
     *
     * @param sb the StringBuilder to append the section to
     */
    private void buildBlogsSection(StringBuilder sb) {
        List<Blog> blogs = blogPublicService.getPublishedBlogs();
        if (blogs == null || blogs.isEmpty()) {
            return;
        }
        sb.append(PortfolioAiConstants.SECTION_BLOGS_INTRO);
        int limit = MAX_BLOG_SECTION_LENGTH - BLOG_SECTION_HEADER_APPROX_LENGTH;
        int used = 0;
        for (Blog blog : blogs) {
            if (used >= limit) {
                sb.append(PortfolioAiConstants.BLOG_TRUNCATION_SUFFIX);
                break;
            }
            String line = "- " + buildBlogSummary(blog) + ". ";
            if (used + line.length() > limit) {
                sb.append(line, 0, limit - used).append(" ");
                used = limit;
            } else {
                sb.append(line);
                used += line.length();
            }
        }
    }

    /**
     * Builds a Document for the personal info entity for RAG retrieval.
     *
     * @param info the PersonalInfo to convert
     * @return a Document with content and metadata (type, name)
     */
    private Document buildPersonalInfoDocument(PersonalInfo info) {
        StringBuilder content = new StringBuilder();
        content.append("Personal Info - ");
        appendPersonalInfoLines(content, info);

        return new Document(content.toString(), java.util.Map.of(
                "type", "personal_info",
                "name", info.getName()
        ));
    }

    /**
     * Builds a Document for an Experience entity for RAG retrieval.
     *
     * @param experience the Experience to convert
     * @return a Document with content and metadata (type, company, position, location, dates)
     */
    private Document buildExperienceDocument(Experience experience) {
        StringBuilder content = new StringBuilder();
        content.append("Experience: ")
                .append(AiTextUtils.nullSafe(experience.getPosition()))
                .append(" at ")
                .append(AiTextUtils.nullSafe(experience.getCompanyName()))
                .append(". ");
        appendExperienceDetails(content, experience);
        return new Document(content.toString(), java.util.Map.of(
                "type", "experience",
                "company", experience.getCompanyName(),
                "position", experience.getPosition(),
                "location", experience.getLocation(),
                "startDate", experience.getStartDate(),
                "endDate", experience.getEndDate()
        ));
    }

    /**
     * Builds a Document for a Project entity, including title, description, technologies, and timeline.
     *
     * @param project the Project entity to convert into a Document
     * @return a Document containing the project's information and metadata for RAG retrieval
     */
    private Document buildProjectDocument(Project project) {
        StringBuilder content = new StringBuilder();
        content.append("Project: ").append(AiTextUtils.nullSafe(project.getTitle())).append(". ");
        content.append(AiTextUtils.nullSafe(project.getShortDescription())).append(" ");
        appendProjectNarrativeDetails(content, project);
        return new Document(content.toString(), java.util.Map.of(
                "type", "project",
                "title", project.getTitle(),
                "startDate", project.getStartDate(),
                "endDate", project.getEndDate(),
                "isActive", project.getIsActive()
        ));
    }

    /**
     * Builds a Document for an Education entity, including degree, field of study, institution, and timeline.
     *
     * @param education the Education entity to convert into a Document
     * @return a Document containing the education's information and metadata for RAG retrieval
     */
    private Document buildEducationDocument(Education education) {
        StringBuilder content = new StringBuilder();
        content.append("Education: ")
                .append(AiTextUtils.nullSafe(education.getDegree()))
                .append(" in ")
                .append(AiTextUtils.nullSafe(education.getFieldOfStudy()))
                .append(" at ")
                .append(AiTextUtils.nullSafe(education.getInstitutionName()))
                .append(". ");
        if (education.getDescription() != null && !education.getDescription().isBlank()) {
            content.append("Details: ").append(education.getDescription()).append(" ");
        }
        return new Document(content.toString(), java.util.Map.of(
                "type", "education",
                "degree", education.getDegree(),
                "fieldOfStudy", education.getFieldOfStudy(),
                "institution", education.getInstitutionName(),
                "startDate", education.getStartDate(),
                "endDate", education.getEndDate()
        ));
    }

    /**
     * Builds a Document for a Certification entity, including certification name, issuing organization, and validity.
     *
     * @param certification the Certification entity to convert into a Document
     * @return a Document containing the certification's information and metadata for RAG retrieval
     */
    private Document buildCertificationDocument(Certification certification) {
        StringBuilder content = new StringBuilder();
        content.append("Certification: ")
                .append(AiTextUtils.nullSafe(certification.getCertificationName()))
                .append(" from ")
                .append(AiTextUtils.nullSafe(certification.getIssuingOrganization()))
                .append(". ");
        if (certification.getDescription() != null && !certification.getDescription().isBlank()) {
            content.append("Details: ").append(certification.getDescription()).append(" ");
        }
        return new Document(content.toString(), java.util.Map.of(
                "type", "certification",
                "name", certification.getCertificationName(),
                "organization", certification.getIssuingOrganization(),
                "issueDate", certification.getIssueDate(),
                "expirationDate", certification.getExpirationDate(),
                "doesNotExpire", certification.getDoesNotExpire()
        ));
    }

    /**
     * Builds a Document for a Skill entity, including skill category and list of skills.
     *
     * @param skill the Skill entity to convert into a Document
     * @return a Document containing the skill's information and metadata for RAG retrieval
     */
    private Document buildSkillDocument(Skill skill) {
        StringBuilder content = new StringBuilder();
        content.append("Skill category: ").append(AiTextUtils.nullSafe(skill.getCategoryName())).append(". ");
        if (skill.getSkills() != null && !skill.getSkills().isEmpty()) {
            content.append("Skills: ").append(String.join(", ", skill.getSkills())).append(". ");
        }
        return new Document(content.toString(), java.util.Map.of(
                "type", "skill",
                "category", skill.getCategoryName()
        ));
    }

    /**
     * Builds a Document for a published Blog so that portfolio chat can retrieve blog-related context.
     *
     * @param blog the Blog entity to convert into a Document
     * @return a Document containing the blog's key metadata for RAG retrieval
     */
    private Document buildBlogDocument(Blog blog) {
        String content = "Blog: " + buildBlogSummary(blog) + ".";
        String category = blog.getCategory() != null ? blog.getCategory().name() : "";
        return new Document(content, java.util.Map.of(
                "type", "blog",
                "title", blog.getTitle(),
                "slug", blog.getSlug(),
                "category", category
        ));
    }

    /**
     * Appends a formatted line for an Experience entry to the provided StringBuilder including context details.
     *
     * @param sb         the StringBuilder to append the experience line to
     * @param experience the Experience entity containing the details to format and append
     */
    private void appendExperienceLine(StringBuilder sb, Experience experience) {
        sb.append("- ")
                .append(AiTextUtils.nullSafe(experience.getPosition()))
                .append(" at ")
                .append(AiTextUtils.nullSafe(experience.getCompanyName()))
                .append(" (")
                .append(AiTextUtils.nullSafe(experience.getStartDate()))
                .append(" - ")
                .append(experience.getIsCurrent() != null && experience.getIsCurrent() ? "Present" : AiTextUtils.nullSafe(experience.getEndDate()))
                .append(")");
        if (experience.getLocation() != null && !experience.getLocation().isBlank()) {
            sb.append(" in ").append(experience.getLocation());
        }
        sb.append(". ");
        appendExperienceDetails(sb, experience);
    }

    /**
     * Appends a formatted line for a Project entry to the provided StringBuilder.
     *
     * @param sb      the StringBuilder to append the project line to
     * @param project the Project entity containing the details to format and append details
     */
    private void appendProjectLine(StringBuilder sb, Project project) {
        sb.append("- ").append(AiTextUtils.nullSafe(project.getTitle())).append(": ")
                .append(AiTextUtils.nullSafe(project.getShortDescription()))
                .append(". ");
        appendProjectNarrativeDetails(sb, project);
        if (project.getProjectUrl() != null && !project.getProjectUrl().isBlank()) {
            sb.append("Project URL: ").append(project.getProjectUrl()).append(". ");
        }
        if (project.getGithubLinks() != null && !project.getGithubLinks().isEmpty()) {
            for (Project.GithubLink link : project.getGithubLinks()) {
                if (link == null || link.getUrl() == null || link.getUrl().isBlank()) {
                    continue;
                }
                String label = (link.getLabel() == null || link.getLabel().isBlank())
                        ? "GitHub"
                        : link.getLabel();
                sb.append("GitHub (").append(label).append("): ").append(link.getUrl()).append(". ");
            }
        }
        if (project.getStartDate() != null || project.getEndDate() != null) {
            sb.append("Timeline: ")
                    .append(AiTextUtils.nullSafe(project.getStartDate()))
                    .append(" - ")
                    .append(AiTextUtils.nullSafe(project.getEndDate()))
                    .append(". ");
        }
    }

    /**
     * Appends a formatted line for an Education entry to the provided StringBuilder.
     *
     * @param sb        the StringBuilder to append the education line to
     * @param education the Education entity containing the details to format and append
     */
    private void appendEducationLine(StringBuilder sb, Education education) {
        sb.append("- ")
                .append(AiTextUtils.nullSafe(education.getDegree()))
                .append(" in ")
                .append(AiTextUtils.nullSafe(education.getFieldOfStudy()))
                .append(" at ")
                .append(AiTextUtils.nullSafe(education.getInstitutionName()))
                .append(" (")
                .append(AiTextUtils.nullSafe(education.getStartDate()))
                .append(" - ")
                .append(education.getIsCurrent() != null && education.getIsCurrent() ? "Present" : AiTextUtils.nullSafe(education.getEndDate()))
                .append("). ");
        if (education.getLocation() != null && !education.getLocation().isBlank()) {
            sb.append("Location: ").append(education.getLocation()).append(". ");
        }
        if (education.getDescription() != null && !education.getDescription().isBlank()) {
            sb.append("Details: ").append(education.getDescription()).append(" ");
        }
    }

    /**
     * Appends a formatted line for a Certification entry to the provided StringBuilder.
     *
     * @param sb            the StringBuilder to append to
     * @param certification the Certification entity to format
     */
    private void appendCertificationLine(StringBuilder sb, Certification certification) {
        sb.append("- ")
                .append(AiTextUtils.nullSafe(certification.getCertificationName()))
                .append(" from ")
                .append(AiTextUtils.nullSafe(certification.getIssuingOrganization()))
                .append(". ");
        if (certification.getIssueDate() != null || certification.getExpirationDate() != null) {
            sb.append("Validity: ")
                    .append(AiTextUtils.nullSafe(certification.getIssueDate()))
                    .append(" - ");
            if (certification.getDoesNotExpire() != null && certification.getDoesNotExpire()) {
                sb.append("No expiry");
            } else {
                sb.append(AiTextUtils.nullSafe(certification.getExpirationDate()));
            }
            sb.append(". ");
        }
        if (certification.getDescription() != null && !certification.getDescription().isBlank()) {
            sb.append("Details: ").append(certification.getDescription()).append(" ");
        }
        if (certification.getCredentialUrl() != null && !certification.getCredentialUrl().isBlank()) {
            sb.append("Credential URL: ").append(certification.getCredentialUrl()).append(". ");
        }
    }

    /**
     * Appends a formatted line for a Skill category and its associated skills to the provided StringBuilder.
     *
     * @param sb    the StringBuilder to append to
     * @param skill the Skill entity containing the category and list of skills to format and append
     */
    private void appendSkillLine(StringBuilder sb, Skill skill) {
        sb.append("- ").append(AiTextUtils.nullSafe(skill.getCategoryName())).append(": ");
        if (skill.getSkills() != null && !skill.getSkills().isEmpty()) {
            sb.append(String.join(", ", skill.getSkills()));
        }
    }

    /**
     * Appends formatted personal info lines (name, tagline, summary, location) to the StringBuilder.
     *
     * @param sb   the StringBuilder to append to
     * @param info the PersonalInfo to format
     */
    private void appendPersonalInfoLines(StringBuilder sb, PersonalInfo info) {
        sb.append("Name: ").append(AiTextUtils.nullSafe(info.getName())).append("\n");
        sb.append("Tagline: ").append(AiTextUtils.nullSafe(info.getTagline())).append("\n");
        if (info.getDescription() != null && !info.getDescription().isEmpty()) {
            sb.append("Summary: ")
                    .append(String.join(" ", info.getDescription()))
                    .append("\n");
        }
        sb.append("Location: ").append(AiTextUtils.nullSafe(info.getLocation())).append("\n");
    }

    /**
     * Appends experience details (summary, impact, highlights) to the StringBuilder.
     *
     * @param sb         the StringBuilder to append to
     * @param experience the Experience entity
     */
    private void appendExperienceDetails(StringBuilder sb, Experience experience) {
        appendListSection(sb, "Summary", experience.getSummary(), " ", false);
        appendListSection(sb, "Impact", experience.getImpact(), " ", false);
        appendListSection(sb, "Highlights", experience.getHighlights(), ", ", true);
    }

    /**
     * Appends project narrative sections used by both context and document builders.
     *
     * @param sb      the StringBuilder to append to
     * @param project the project entity containing narrative sections
     */
    private void appendProjectNarrativeDetails(StringBuilder sb, Project project) {
        appendListSection(sb, "Overview", project.getOverview(), " ", false);
        appendListSection(sb, "Key features", project.getKeyFeatures(), " ", false);
        appendListSection(sb, "Engineering", project.getEngineering(), " ", false);
        appendListSection(sb, "Decisions", project.getDecisions(), " ", false);
        appendListSection(sb, "Impact", project.getImpact(), " ", false);
        appendListSection(sb, "Highlights", project.getHighlights(), ", ", true);
    }

    /**
     * Appends a labeled list section to a builder when the list has content.
     *
     * @param sb           builder to append to
     * @param label        section label
     * @param values       values to join
     * @param delimiter    delimiter used when joining values
     * @param appendPeriod whether to append a trailing period before whitespace
     */
    private void appendListSection(StringBuilder sb, String label, List<String> values, String delimiter, boolean appendPeriod) {
        if (values == null || values.isEmpty()) {
            return;
        }
        List<String> sanitizedValues = values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .toList();
        if (sanitizedValues.isEmpty()) {
            return;
        }
        sb.append(label).append(": ").append(String.join(delimiter, sanitizedValues));
        if (appendPeriod) {
            sb.append(".");
        }
        sb.append(" ");
    }

}
