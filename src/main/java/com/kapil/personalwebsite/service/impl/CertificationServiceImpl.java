package com.kapil.personalwebsite.service.impl;

import com.kapil.personalwebsite.entity.Certification;
import com.kapil.personalwebsite.repository.CertificationRepository;
import com.kapil.personalwebsite.service.CertificationService;
import com.kapil.personalwebsite.util.DateParsingUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of CertificationService for certification operations in the personal website.
 * Provides read-only access to certification information for public access.
 *
 * @author Kapil Garg
 */
@Service
@RequiredArgsConstructor
public class CertificationServiceImpl implements CertificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CertificationServiceImpl.class);

    private final CertificationRepository certificationRepository;

    /**
     * Retrieves all certifications ordered by recency (most recent first).
     * Sorts by issue_date, with certifications without dates appearing last.
     *
     * @return a list of all certifications sorted by recency
     */
    @Override
    @Transactional(readOnly = true)
    public List<Certification> getAllCertifications() {
        LOGGER.info("Fetching all certifications for public access");
        List<Certification> certifications = certificationRepository.findAll();
        return certifications.stream()
                .sorted(Comparator.comparing(
                        (Certification c) -> DateParsingUtils.parseDate(c.getIssueDate()),
                        Comparator.nullsLast(Comparator.reverseOrder()))
                )
                .collect(Collectors.toList());
    }

}
