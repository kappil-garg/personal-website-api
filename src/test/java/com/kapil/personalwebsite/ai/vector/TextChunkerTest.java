package com.kapil.personalwebsite.ai.vector;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TextChunkerTest {

    @Test
    void chunk_WhenInputIsNullOrBlank_ShouldReturnEmptyList() {
        assertTrue(TextChunker.chunk(null, 10, 0).isEmpty());
        assertTrue(TextChunker.chunk("   \n\t  ", 10, 0).isEmpty());
    }

    @Test
    void chunk_ShouldNormalizeWhitespaceBeforeChunking() {
        List<String> result = TextChunker.chunk("  alpha \n beta   gamma  ", 32, 0);
        assertEquals(List.of("alpha beta gamma"), result);
    }

    @Test
    void chunk_ShouldCreateOverlappingChunks() {
        List<String> result = TextChunker.chunk("abcdefghij", 4, 1);
        assertEquals(List.of("abcd", "defg", "ghij"), result);
    }

    @Test
    void chunk_WhenLastChunkIsShorterThanMaxChars_ShouldIncludeRemainder() {
        List<String> result = TextChunker.chunk("abcde", 4, 1);
        assertEquals(List.of("abcd", "de"), result);
    }

    @Test
    void chunk_WhenOverlapIsZero_ShouldSplitWithoutOverlap() {
        List<String> result = TextChunker.chunk("abcdefgh", 4, 0);
        assertEquals(List.of("abcd", "efgh"), result);
    }

    @Test
    void chunk_WhenMaxCharsIsNotPositive_ShouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> TextChunker.chunk("text", 0, 0));
        assertEquals("maxChars must be positive", exception.getMessage());
    }

    @Test
    void chunk_WhenOverlapIsOutsideValidRange_ShouldThrowException() {
        IllegalArgumentException negativeOverlap = assertThrows(IllegalArgumentException.class,
                () -> TextChunker.chunk("text", 4, -1));
        IllegalArgumentException tooLargeOverlap = assertThrows(IllegalArgumentException.class,
                () -> TextChunker.chunk("text", 4, 4));
        assertEquals("overlap must be in [0, maxChars)", negativeOverlap.getMessage());
        assertEquals("overlap must be in [0, maxChars)", tooLargeOverlap.getMessage());
    }

    @Test
    void instantiation_ShouldThrowException() throws Exception {
        Constructor<TextChunker> constructor = TextChunker.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Exception exception = assertThrows(Exception.class, constructor::newInstance);
        assertInstanceOf(UnsupportedOperationException.class, exception.getCause());
    }

}
