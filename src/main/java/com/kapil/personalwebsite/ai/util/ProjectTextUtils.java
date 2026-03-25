package com.kapil.personalwebsite.ai.util;

import com.kapil.personalwebsite.entity.Project;
import com.kapil.personalwebsite.util.AppConstants;

import java.util.List;

import static com.kapil.personalwebsite.ai.util.AiTextUtils.nullSafe;

/**
 * Shared text fragments for Project entities.
 *
 * @author Kapil Garg
 */
public final class ProjectTextUtils {

    private ProjectTextUtils() {
        throw new UnsupportedOperationException(AppConstants.UTILITY_CLASS_INSTANTIATION_MSG);
    }

    /**
     * Appends optional project URL, GitHub links, and date range.
     *
     * @param sb      the StringBuilder to append to
     * @param project the Project entity containing the data to append
     */
    public static void appendProjectUrlsAndTimeline(StringBuilder sb, Project project) {
        if (project.getProjectUrl() != null && !project.getProjectUrl().isBlank()) {
            sb.append("Project URL: ").append(project.getProjectUrl()).append(". ");
        }
        appendGithubLinks(sb, project.getGithubLinks());
        if (project.getStartDate() != null || project.getEndDate() != null) {
            sb.append("Timeline: ")
                    .append(nullSafe(project.getStartDate()))
                    .append(" - ")
                    .append(nullSafe(project.getEndDate()))
                    .append(". ");
        }
    }

    /**
     * Appends labeled GitHub (or custom-label) URLs for a project's links.
     *
     * @param sb    the StringBuilder to append to
     * @param links the list of GitHubLink objects to process and append
     */
    public static void appendGithubLinks(StringBuilder sb, List<Project.GithubLink> links) {
        if (links == null || links.isEmpty()) {
            return;
        }
        for (Project.GithubLink link : links) {
            if (link == null || link.getUrl() == null || link.getUrl().isBlank()) {
                continue;
            }
            String label = (link.getLabel() == null || link.getLabel().isBlank())
                    ? "GitHub"
                    : link.getLabel();
            sb.append("GitHub (").append(label).append("): ").append(link.getUrl()).append(". ");
        }
    }

}
