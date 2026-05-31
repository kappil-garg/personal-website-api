package com.kapil.personalwebsite.util;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityStringUtilsTest {

    @Test
    void sanitizeForEmailHeader_ShouldRemoveControlCharactersAndTrim() {
        String input = "  Test\r\nSubject\t\u0000 ";

        assertEquals("TestSubject", SecurityStringUtils.sanitizeForEmailHeader(input));
    }

    @Test
    void sanitizeForEmailBody_ShouldNormalizeLineEndingsAndReplaceTabs() {
        String input = "line1\r\nline2\rline3\t\u0000";

        assertEquals("line1\nline2\nline3    ", SecurityStringUtils.sanitizeForEmailBody(input));
    }

    @Test
    void escapeHtml_ShouldEscapeSpecialCharacters() {
        assertEquals("&lt;tag attr=&quot;value&quot;&gt;&amp;&#x27;",
                SecurityStringUtils.escapeHtml("<tag attr=\"value\">&'"));
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
