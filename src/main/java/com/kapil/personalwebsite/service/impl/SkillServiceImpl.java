package com.kapil.personalwebsite.service.impl;

import com.kapil.personalwebsite.entity.Skill;
import com.kapil.personalwebsite.repository.SkillRepository;
import com.kapil.personalwebsite.service.SkillService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of SkillService for skill operations in the personal website.
 * Provides read-only access to skill information for public access.
 *
 * @author Kapil Garg
 */
@Service
@RequiredArgsConstructor
public class SkillServiceImpl implements SkillService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SkillServiceImpl.class);

    private final SkillRepository skillRepository;

    /**
     * Retrieves all skills ordered by display order.
     *
     * @return a list of all skills sorted by display order
     */
    @Override
    @Transactional(readOnly = true)
    public List<Skill> getAllSkills() {
        LOGGER.info("Fetching all skills for public access");
        List<Skill> skills = skillRepository.findAll();
        return skills.stream()
                .sorted(Comparator.comparing(Skill::getDisplayOrder, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

}
