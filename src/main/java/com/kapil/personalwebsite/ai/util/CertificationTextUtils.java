package com.kapil.personalwebsite.ai.util;

import com.kapil.personalwebsite.entity.Certification;
import com.kapil.personalwebsite.util.AppConstants;

/**
 * Shared certification text for RAG documents and vector chunks.
 *
 * @author Kapil Garg
 */
public final class CertificationTextUtils {

    private CertificationTextUtils() {
        throw new UnsupportedOperationException(AppConstants.UTILITY_CLASS_INSTANTIATION_MSG);
    }

    /**
     * Appends certification name, issuing organization, and optional description to the StringBuilder in a consistent format.
     *
     * @param sb            the StringBuilder to append to
     * @param certification the Certification entity containing the data to append
     */
    public static void appendNarrative(StringBuilder sb, Certification certification) {
        sb.append("Certification: ")
                .append(AiTextUtils.nullSafe(certification.getCertificationName()))
                .append(" from ")
                .append(AiTextUtils.nullSafe(certification.getIssuingOrganization()))
                .append(". ");
        if (certification.getDescription() != null && !certification.getDescription().isBlank()) {
            sb.append("Details: ").append(certification.getDescription()).append(" ");
        }
    }

}
