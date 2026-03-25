package com.kapil.personalwebsite.ai.portfolio;

import com.kapil.personalwebsite.ai.util.AiTextUtils;
import com.kapil.personalwebsite.entity.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Default implementation of PortfolioRagService which builds the RAG context summary and documents.
 *
 * @author Kapil Garg
 */
@Service
public class PortfolioRagServiceImpl implements PortfolioRagService {

    private static final int MAX_SUMMARY_CONTEXT_LENGTH = 2_200;

    private final PortfolioDataProvider dataProvider;

    public PortfolioRagServiceImpl(PortfolioDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    @Cacheable("portfolioSummary")
    public String buildPortfolioContextSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Summary (use retrieved snippets for specifics): ");
        appendSummaryPersonal(sb);
        appendSummaryProjects(sb);
        appendSummaryExperience(sb);
        appendSummaryEducation(sb);
        appendSummaryCertifications(sb);
        appendSummarySkills(sb);
        appendSummaryBlogs(sb);
        String context = sb.toString().replaceAll("\\s+", " ").trim();
        if (context.length() > MAX_SUMMARY_CONTEXT_LENGTH) {
            context = context.substring(0, MAX_SUMMARY_CONTEXT_LENGTH) + "...";
        }
        return context;
    }

    /**
     * Appends the personal info to the summary.
     *
     * @param sb the summary builder
     */
    private void appendSummaryPersonal(StringBuilder sb) {
        dataProvider.getPersonalInfo().ifPresent(info -> sb
                .append("Name: ").append(AiTextUtils.nullSafe(info.getName()))
                .append(". Tagline: ").append(AiTextUtils.nullSafe(info.getTagline()))
                .append(". Location: ").append(AiTextUtils.nullSafe(info.getLocation()))
                .append(". "));
    }

    /**
     * Appends the projects to the summary.
     *
     * @param sb the summary builder
     */
    private void appendSummaryProjects(StringBuilder sb) {
        List<Project> projects = dataProvider.getAllProjects();
        if (projects.isEmpty()) {
            return;
        }
        String joined = projects.stream()
                .map(p -> AiTextUtils.nullSafe(p.getTitle()))
                .filter(t -> !t.isEmpty())
                .collect(Collectors.joining("; "));
        sb.append("Projects: ").append(joined).append(". ");
    }

    /**
     * Appends the experience to the summary.
     *
     * @param sb the summary builder
     */
    private void appendSummaryExperience(StringBuilder sb) {
        List<Experience> list = dataProvider.getAllExperiences();
        if (list.isEmpty()) {
            return;
        }
        String joined = list.stream()
                .map(e -> AiTextUtils.nullSafe(e.getPosition()) + " at " + AiTextUtils.nullSafe(e.getCompanyName()))
                .collect(Collectors.joining("; "));
        sb.append("Roles: ").append(joined).append("; ");
    }

    /**
     * Appends the education to the summary.
     *
     * @param sb the summary builder
     */
    private void appendSummaryEducation(StringBuilder sb) {
        List<Education> list = dataProvider.getAllEducations();
        if (list.isEmpty()) {
            return;
        }
        String joined = list.stream()
                .map(ed -> AiTextUtils.nullSafe(ed.getDegree()) + " at " + AiTextUtils.nullSafe(ed.getInstitutionName()))
                .collect(Collectors.joining("; "));
        sb.append("Education: ").append(joined).append("; ");
    }

    /**
     * Appends the certifications to the summary.
     *
     * @param sb the summary builder
     */
    private void appendSummaryCertifications(StringBuilder sb) {
        List<Certification> list = dataProvider.getAllCertifications();
        if (list.isEmpty()) {
            return;
        }
        String joined = list.stream()
                .map(c -> AiTextUtils.nullSafe(c.getCertificationName()))
                .collect(Collectors.joining("; "));
        sb.append("Certifications: ").append(joined).append("; ");
    }

    /**
     * Appends the skills to the summary.
     *
     * @param sb the summary builder
     */
    private void appendSummarySkills(StringBuilder sb) {
        List<Skill> skills = dataProvider.getAllSkills();
        if (skills.isEmpty()) {
            return;
        }
        String joined = skills.stream()
                .map(s -> {
                    String skillList = s.getSkills() == null ? "" : s.getSkills().stream()
                                                                    .filter(x -> x != null && !x.isBlank())
                                                                    .map(String::trim)
                                                                    .collect(Collectors.joining(", "));
                    return AiTextUtils.nullSafe(s.getCategoryName()) + ": " + skillList;
                })
                .collect(Collectors.joining("; "));
        sb.append("Skills: ").append(joined).append("; ");
    }

    /**
     * Appends the blogs to the summary.
     *
     * @param sb the summary builder
     */
    private void appendSummaryBlogs(StringBuilder sb) {
        List<Blog> blogs = dataProvider.getPublishedBlogs();
        if (blogs.isEmpty()) {
            return;
        }
        String joined = blogs.stream()
                .map(blog -> AiTextUtils.nullSafe(blog.getTitle()) + " (slug " + AiTextUtils.nullSafe(blog.getSlug()) + ")")
                .collect(Collectors.joining("; "));
        sb.append("Blog posts: ").append(joined).append("; ");
    }

}
