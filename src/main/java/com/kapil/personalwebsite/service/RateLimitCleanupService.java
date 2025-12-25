package com.kapil.personalwebsite.service;

import com.kapil.personalwebsite.config.RateLimitFilter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Service for cleaning up expired rate limit entries.
 * Uses Spring's scheduling mechanism for proper lifecycle management.
 *
 * @author Kapil Garg
 */
@Service
@RequiredArgsConstructor
public class RateLimitCleanupService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitCleanupService.class);

    private final RateLimitFilter rateLimitFilter;

    /**
     * Cleans up expired entries from the rate limit cache.
     * Runs every 2 minutes to remove old entries and prevent memory leaks.
     */
    @Scheduled(fixedRate = 2 * 60 * 1000)
    public void cleanupExpiredEntries() {
        try {
            rateLimitFilter.cleanupExpiredEntries();
        } catch (Exception e) {
            LOGGER.error("Error during rate limit cleanup", e);
        }
    }

}
