package com.kapil.personalwebsite.ai.util;

import com.kapil.personalwebsite.entity.*;
import com.kapil.personalwebsite.util.AppConstants;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Canonical text representations for each portfolio entity type.
 * Used by both the RAG context builder and the vector chunk document service to ensure the text stored is identical.
 *
 * @author Kapil Garg
 */
public final class PortfolioEntityTextBuilder {

    private PortfolioEntityTextBuilder() {
        throw new UnsupportedOperationException(AppConstants.UTILITY_CLASS_INSTANTIATION_MSG);
    }

    /**
     * Builds the full narrative text for a {@link PersonalInfo} entity.
     *
     * @param info the personal info
     * @return the full narrative text
     */
    public static String buildPersonalInfoText(PersonalInfo info) {
        StringBuilder sb = new StringBuilder();
        sb.append("Personal Info. Name: ").append(AiTextUtils.nullSafe(info.getName())).append(". ");
        sb.append("Tagline: ").append(AiTextUtils.nullSafe(info.getTagline())).append(". ");
        if (info.getDescription() != null && !info.getDescription().isEmpty()) {
            sb.append("Summary: ").append(String.join(" ", info.getDescription())).append(". ");
        }
        sb.append("Location: ").append(AiTextUtils.nullSafe(info.getLocation())).append(". ");
        return sb.toString();
    }

    /**
     * Builds the full narrative text for an {@link Experience} entity.
     *
     * @param e the experience
     * @return the full narrative text
     */
    public static String buildExperienceText(Experience e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Experience: ")
                .append(AiTextUtils.nullSafe(e.getPosition()))
                .append(" at ")
                .append(AiTextUtils.nullSafe(e.getCompanyName()))
                .append(". ");
        if (e.getLocation() != null && !e.getLocation().isBlank()) {
            sb.append("Location: ").append(e.getLocation()).append(". ");
        }
        sb.append("Timeline: ")
                .append(AiTextUtils.nullSafe(e.getStartDate()))
                .append(" - ")
                .append(Boolean.TRUE.equals(e.getIsCurrent()) ? "Present" : AiTextUtils.nullSafe(e.getEndDate()))
                .append(". ");
        appendJoined(sb, "Summary", e.getSummary(), " ", false);
        appendJoined(sb, "Impact", e.getImpact(), " ", false);
        appendJoined(sb, "Highlights", e.getHighlights(), ", ", true);
        return sb.toString();
    }

    /**
     * Builds the full narrative text for a {@link Project} entity.
     *
     * @param p the project
     * @return the full narrative text
     */
    public static String buildProjectText(Project p) {
        StringBuilder sb = new StringBuilder();
        sb.append("Project: ").append(AiTextUtils.nullSafe(p.getTitle())).append(". ");
        sb.append(AiTextUtils.nullSafe(p.getShortDescription())).append(" ");
        appendJoined(sb, "Overview", p.getOverview(), " ", false);
        appendJoined(sb, "Key features", p.getKeyFeatures(), " ", false);
        appendJoined(sb, "Engineering", p.getEngineering(), " ", false);
        appendJoined(sb, "Decisions", p.getDecisions(), " ", false);
        appendJoined(sb, "Impact", p.getImpact(), " ", false);
        appendJoined(sb, "Highlights", p.getHighlights(), ", ", true);
        ProjectTextUtils.appendProjectUrlsAndTimeline(sb, p);
        return sb.toString();
    }

    /**
     * Builds the full narrative text for an {@link Education} entity.
     *
     * @param ed the education
     * @return the full narrative text
     */
    public static String buildEducationText(Education ed) {
        StringBuilder sb = new StringBuilder();
        sb.append("Education: ")
                .append(AiTextUtils.nullSafe(ed.getDegree()))
                .append(" in ")
                .append(AiTextUtils.nullSafe(ed.getFieldOfStudy()))
                .append(" at ")
                .append(AiTextUtils.nullSafe(ed.getInstitutionName()))
                .append(". ");
        sb.append("Timeline: ")
                .append(AiTextUtils.nullSafe(ed.getStartDate()))
                .append(" - ")
                .append(Boolean.TRUE.equals(ed.getIsCurrent()) ? "Present" : AiTextUtils.nullSafe(ed.getEndDate()))
                .append(". ");
        if (ed.getLocation() != null && !ed.getLocation().isBlank()) {
            sb.append("Location: ").append(ed.getLocation()).append(". ");
        }
        if (ed.getDescription() != null && !ed.getDescription().isBlank()) {
            sb.append("Details: ").append(ed.getDescription()).append(" ");
        }
        return sb.toString();
    }

    /**
     * Builds the full narrative text for a {@link Certification} entity.
     *
     * @param c the certification
     * @return the full narrative text
     */
    public static String buildCertificationText(Certification c) {
        StringBuilder sb = new StringBuilder();
        CertificationTextUtils.appendNarrative(sb, c);
        if (c.getCredentialUrl() != null && !c.getCredentialUrl().isBlank()) {
            sb.append("Credential URL: ").append(c.getCredentialUrl()).append(". ");
        }
        return sb.toString();
    }

    /**
     * Builds the full narrative text for a {@link Skill} entity.
     *
     * @param s the skill
     * @return the full narrative text
     */
    public static String buildSkillText(Skill s) {
        StringBuilder sb = new StringBuilder();
        sb.append("Skill category: ").append(AiTextUtils.nullSafe(s.getCategoryName())).append(". ");
        if (s.getSkills() != null && !s.getSkills().isEmpty()) {
            String joined = s.getSkills().stream()
                    .filter(x -> x != null && !x.isBlank())
                    .map(String::trim)
                    .collect(Collectors.joining(", "));
            if (!joined.isEmpty()) {
                sb.append("Skills: ").append(joined).append(". ");
            }
        }
        return sb.toString();
    }

    /**
     * Builds the full narrative text for a published {@link Blog} entity.
     *
     * @param blog the blog
     * @return the full narrative text
     */
    public static String buildBlogText(Blog blog) {
        StringBuilder sb = new StringBuilder();
        sb.append("Blog: ").append(AiTextUtils.nullSafe(blog.getTitle())).append(". ");
        sb.append("Slug: ").append(AiTextUtils.nullSafe(blog.getSlug())).append(". ");
        if (blog.getCategory() != null) {
            sb.append("Category: ").append(blog.getCategory().name()).append(". ");
        }
        if (blog.getExcerpt() != null && !blog.getExcerpt().isBlank()) {
            sb.append("Excerpt: ").append(AiTextUtils.stripHtmlTags(blog.getExcerpt())).append(". ");
        }
        String body = AiTextUtils.stripHtmlTags(blog.getContent() != null ? blog.getContent() : "");
        if (!body.isBlank()) {
            sb.append("Content: ").append(body);
        }
        return sb.toString();
    }

    /**
     * Appends a labeled, joined list of non-blank strings to the builder.
     *
     * @param sb           the builder to append to
     * @param label        section label prefix
     * @param values       values to filter, trim, and join
     * @param delimiter    join delimiter between values
     * @param appendPeriod whether to append a trailing period after the joined content
     */
    public static void appendJoined(StringBuilder sb, String label, List<String> values,
                                    String delimiter, boolean appendPeriod) {
        if (values == null || values.isEmpty()) {
            return;
        }
        String joined = values.stream()
                .filter(v -> v != null && !v.isBlank())
                .map(String::trim)
                .collect(Collectors.joining(delimiter));
        if (joined.isEmpty()) {
            return;
        }
        sb.append(label).append(": ").append(joined);
        if (appendPeriod) {
            sb.append(".");
        }
        sb.append(" ");
    }

}
