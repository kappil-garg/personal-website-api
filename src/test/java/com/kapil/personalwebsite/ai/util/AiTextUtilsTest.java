package com.kapil.personalwebsite.ai.util;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class AiTextUtilsTest {

    @Test
    void stripHtmlTags_WhenInputIsNullOrBlank_ShouldReturnEmptyString() {
        assertEquals("", AiTextUtils.stripHtmlTags(null));
        assertEquals("", AiTextUtils.stripHtmlTags("   "));
    }

    @Test
    void stripHtmlTags_ShouldRemoveTagsAndNormalizeWhitespace() {
        String html = "<div>Hello <strong>world</strong></div><p>Line&nbsp;<em>two</em></p>";
        assertEquals("Hello world Line&nbsp; two", AiTextUtils.stripHtmlTags(html));
    }

    @Test
    void stripHtmlTags_WhenInputHasNoTags_ShouldTrimAndCollapseWhitespace() {
        assertEquals("plain text", AiTextUtils.stripHtmlTags("  plain   text  "));
    }

    @Test
    void nullSafe_ShouldReturnOriginalValueOrEmptyString() {
        assertEquals("", AiTextUtils.nullSafe(null));
    }

    @Test
    void instantiation_ShouldThrowException() throws Exception {
        Constructor<AiTextUtils> constructor = AiTextUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Exception exception = assertThrows(Exception.class, constructor::newInstance);
        assertInstanceOf(UnsupportedOperationException.class, exception.getCause());
    }

}
