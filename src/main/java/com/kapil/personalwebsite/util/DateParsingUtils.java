package com.kapil.personalwebsite.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for date parsing and formatting.
 *
 * @author Kapil Garg
 */
public final class DateParsingUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(DateParsingUtils.class);

    private DateParsingUtils() {
        throw new UnsupportedOperationException(AppConstants.UTILITY_CLASS_INSTANTIATION_MSG);
    }

    /**
     * Parses a date string in MM-yyyy format to LocalDate for comparison.
     * Returns null if the date string is invalid.
     *
     * @param dateStr the date string in MM-yyyy format
     * @return the parsed LocalDate or null if invalid
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse("01-" + dateStr, DateTimeFormatter.ofPattern(AppConstants.DATE_FORMAT));
        } catch (Exception e) {
            LOGGER.warn("Failed to parse date: {}", dateStr, e);
            return null;
        }
    }

}
