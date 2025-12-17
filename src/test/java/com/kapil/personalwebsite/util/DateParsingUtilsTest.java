package com.kapil.personalwebsite.util;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DateParsingUtilsTest {

    @Test
    void parseDate_WithNullInput_ShouldReturnNull() {
        assertNull(DateParsingUtils.parseDate(null));
    }

    @Test
    void parseDate_WithEmptyString_ShouldReturnNull() {
        assertNull(DateParsingUtils.parseDate(""));
    }

    @Test
    void parseDate_WithValidMMYyyyFormat_ShouldReturnLocalDate() {
        LocalDate result = DateParsingUtils.parseDate("12-2023");
        assertNotNull(result);
        assertEquals(2023, result.getYear());
        assertEquals(12, result.getMonthValue());
        assertEquals(1, result.getDayOfMonth());
    }

    @Test
    void parseDate_WithValidSingleDigitMonth_ShouldReturnLocalDate() {
        LocalDate result = DateParsingUtils.parseDate("01-2024");
        assertNotNull(result);
        assertEquals(2024, result.getYear());
        assertEquals(1, result.getMonthValue());
        assertEquals(1, result.getDayOfMonth());
    }

    @Test
    void parseDate_WithInvalidFormat_ShouldReturnNull() {
        assertNull(DateParsingUtils.parseDate("2025-12"));
        assertNull(DateParsingUtils.parseDate("12/2025"));
        assertNull(DateParsingUtils.parseDate("invalid"));
        assertNull(DateParsingUtils.parseDate("13-2025"));
        assertNull(DateParsingUtils.parseDate("00-2026"));
    }

    @Test
    void parseDate_WithInvalidDate_ShouldReturnNull() {
        assertNull(DateParsingUtils.parseDate("32-2025"));
    }

    @Test
    void instantiation_ShouldThrowException() throws Exception {
        Constructor<DateParsingUtils> constructor = DateParsingUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Exception exception = assertThrows(Exception.class, constructor::newInstance);
        assertInstanceOf(UnsupportedOperationException.class, exception.getCause());
    }

}
