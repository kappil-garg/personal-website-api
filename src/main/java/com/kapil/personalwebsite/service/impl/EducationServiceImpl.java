package com.kapil.personalwebsite.service.impl;

import com.kapil.personalwebsite.entity.Education;
import com.kapil.personalwebsite.repository.EducationRepository;
import com.kapil.personalwebsite.service.EducationService;
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
 * Implementation of EducationService for education operations in the personal website.
 * Provides read-only access to education information for public access.
 *
 * @author Kapil Garg
 */
@Service
@RequiredArgsConstructor
public class EducationServiceImpl implements EducationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EducationServiceImpl.class);

    private final EducationRepository educationRepository;

    /**
     * Retrieves all educations ordered by recency (most recent first).
     * For current education, uses start_date. For completed education, uses end_date.
     *
     * @return a list of all educations sorted by recency
     */
    @Override
    @Transactional(readOnly = true)
    public List<Education> getAllEducations() {
        LOGGER.info("Fetching all educations for public access");
        List<Education> educations = educationRepository.findAll();
        return educations.stream()
                .sorted(Comparator.comparing((Education e) -> {
                    String dateStr = (e.getIsCurrent() || e.getEndDate() == null)
                            ? e.getStartDate()
                            : e.getEndDate();
                    return DateParsingUtils.parseDate(dateStr);
                }, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

}
