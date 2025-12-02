package com.kapil.personalwebsite.service.impl;

import com.kapil.personalwebsite.entity.PersonalInfo;
import com.kapil.personalwebsite.repository.PersonalInfoRepository;
import com.kapil.personalwebsite.service.PersonalInfoService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementation of PersonalInfoService for portfolio operations.
 * Provides read access to portfolio information for public users, and write (update) access for administrators.
 *
 * @author Kapil Garg
 */
@Service
@RequiredArgsConstructor
public class PersonalInfoServiceImpl implements PersonalInfoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersonalInfoServiceImpl.class);

    private final PersonalInfoRepository personalInfoRepository;

    /**
     * Updates the existing PersonalInfo with new values.
     *
     * @param personalInfo the new personal information
     * @param existingInfo the existing personal information
     * @return the updated PersonalInfo
     */
    private static PersonalInfo updatePersonalInfo(PersonalInfo personalInfo, PersonalInfo existingInfo) {
        existingInfo.setName(personalInfo.getName());
        existingInfo.setTagline(personalInfo.getTagline());
        existingInfo.setDescription(personalInfo.getDescription());
        existingInfo.setProfileImage(personalInfo.getProfileImage());
        existingInfo.setEmail(personalInfo.getEmail());
        existingInfo.setPhone(personalInfo.getPhone());
        existingInfo.setLocation(personalInfo.getLocation());
        existingInfo.setSocialLinks(personalInfo.getSocialLinks());
        return existingInfo;
    }

    /**
     * Retrieves the personal information/portfolio.
     *
     * @return an Optional containing the personal info if found, or empty if not found
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<PersonalInfo> getPersonalInfo() {
        LOGGER.info("Fetching personal information for public access");
        return personalInfoRepository.findFirstByOrderByIdAsc();
    }

    /**
     * Updates the personal information (admin only).
     *
     * @param personalInfo the updated personal information
     * @return the updated personal information
     */
    @Override
    @Transactional
    public PersonalInfo updatePersonalInfo(PersonalInfo personalInfo) {
        LOGGER.info("Updating personal information");
        Optional<PersonalInfo> existing = personalInfoRepository.findFirstByOrderByIdAsc();
        if (existing.isPresent()) {
            PersonalInfo existingInfo = updatePersonalInfo(personalInfo, existing.get());
            return personalInfoRepository.save(existingInfo);
        } else {
            return personalInfoRepository.save(personalInfo);
        }
    }

}
