package com.kapil.personalwebsite.service;

import com.kapil.personalwebsite.entity.PersonalInfo;

import java.util.Optional;

/**
 * Service interface for PersonalInfo operations.
 * Provides access to portfolio information, including read and (admin-only) update operations.
 *
 * @author Kapil Garg
 */
public interface PersonalInfoService {

    /**
     * Retrieves the personal information/portfolio.
     *
     * @return an Optional containing the personal info if found, or empty if not found
     */
    Optional<PersonalInfo> getPersonalInfo();

    /**
     * Updates the personal information (admin only).
     *
     * @param personalInfo the updated personal information
     * @return the updated personal information
     */
    PersonalInfo updatePersonalInfo(PersonalInfo personalInfo);

}
