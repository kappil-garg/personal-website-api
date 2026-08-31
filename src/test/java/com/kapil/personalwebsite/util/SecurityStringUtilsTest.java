package com.kapil.personalwebsite.util;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class SecurityStringUtilsTest {

    @Test
    void sanitizeForEmailHeader_ShouldRemoveControlCharactersAndTrim() {
        String input = "  Test\r\nSubject\t\u0000 ";
        assertEquals("TestSubject", SecurityStringUtils.sanitizeForEmailHeader(input));
    }

    @Test
    void sanitizeForEmailHeader_WhenInputIsNull_ShouldReturnEmptyString() {
        assertEquals("", SecurityStringUtils.sanitizeForEmailHeader(null));
    }

    @Test
    void sanitizeForEmailBody_ShouldNormalizeLineEndingsAndReplaceTabs() {
        String input = "line1\r\nline2\rline3\t\u0000";
        assertEquals("line1\nline2\nline3    ", SecurityStringUtils.sanitizeForEmailBody(input));
    }

    @Test
    void sanitizeForEmailBody_WhenInputIsNull_ShouldReturnEmptyString() {
        assertEquals("", SecurityStringUtils.sanitizeForEmailBody(null));
    }

    @Test
    void escapeHtml_ShouldEscapeSpecialCharacters() {
        assertEquals("&lt;tag attr=&quot;value&quot;&gt;&amp;&#x27;",
                SecurityStringUtils.escapeHtml("<tag attr=\"value\">&'"));
    }

    @Test
    void escapeHtml_WhenInputIsNull_ShouldReturnEmptyString() {
        assertEquals("", SecurityStringUtils.escapeHtml(null));
    }

    @Test
    void escapeAndSanitizeForEmailBody_ShouldSanitizeBeforeEscaping() {
        String input = "<b>Hello</b>\r\n\tWorld";

        assertEquals("&lt;b&gt;Hello&lt;/b&gt;\n    World",
                SecurityStringUtils.escapeAndSanitizeForEmailBody(input));
    }

    @Test
    void blankChecksAndConfiguredCheck_ShouldHandleNullWhitespaceAndNullLiteral() {
        assertTrue(SecurityStringUtils.isBlank(null));
        assertTrue(SecurityStringUtils.isBlank("   "));
        assertFalse(SecurityStringUtils.isBlank(" value "));
        assertFalse(SecurityStringUtils.isNotBlank(null));
        assertFalse(SecurityStringUtils.isNotBlank("   "));
        assertTrue(SecurityStringUtils.isNotBlank("value"));
        assertFalse(SecurityStringUtils.isConfigured(null));
        assertFalse(SecurityStringUtils.isConfigured(" "));
        assertFalse(SecurityStringUtils.isConfigured("null"));
        assertFalse(SecurityStringUtils.isConfigured("NULL"));
        assertTrue(SecurityStringUtils.isConfigured("configured"));
    }

    @Test
    void constantTimeEquals_ShouldRespectNullLengthAndContentChecks() {
        assertFalse(SecurityStringUtils.constantTimeEquals(null, null));
        assertFalse(SecurityStringUtils.constantTimeEquals(null, "abc"));
        assertFalse(SecurityStringUtils.constantTimeEquals("abc", null));
        assertFalse(SecurityStringUtils.constantTimeEquals("ab", "abc"));
        assertFalse(SecurityStringUtils.constantTimeEquals("abc", "abd"));
        assertTrue(SecurityStringUtils.constantTimeEquals("abc", "abc"));
    }

    @Test
    void instantiation_ShouldThrowException() throws Exception {
        Constructor<SecurityStringUtils> constructor = SecurityStringUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Exception exception = assertThrows(Exception.class, constructor::newInstance);
        assertInstanceOf(UnsupportedOperationException.class, exception.getCause());
    }

}
