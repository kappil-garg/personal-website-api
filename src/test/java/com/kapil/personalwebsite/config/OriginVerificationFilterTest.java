package com.kapil.personalwebsite.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OriginVerificationFilter.
 *
 * @author Kapil Garg
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OriginVerificationFilterTest {

    private OriginVerificationFilter filter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() throws IOException {
        filter = new OriginVerificationFilter();
        StringWriter responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    private void initFilter(String allowedOrigins, String serverApiKey, boolean allowNoOriginForSSR) {
        ReflectionTestUtils.setField(filter, "allowedOrigins", allowedOrigins);
        ReflectionTestUtils.setField(filter, "serverApiKey", serverApiKey);
        ReflectionTestUtils.setField(filter, "allowNoOriginForSSR", allowNoOriginForSSR);
        filter.initialize();
    }

    private void initFilter(String allowedOrigins, String serverApiKey) {
        initFilter(allowedOrigins, serverApiKey, true);
    }

    @Nested
    @DisplayName("Initialization Tests")
    class InitializationTests {

        @Test
        @DisplayName("Should cache allowed origins on initialization")
        void testInitialize_WithAllowedOrigins_ShouldCacheOrigins() {
            initFilter("https://example.com,https://test.com", "test-key");
            assertNotNull(filter);
        }

        @Test
        @DisplayName("Should handle empty origins gracefully")
        void testInitialize_WithEmptyOrigins_ShouldHandleGracefully() {
            initFilter("", "test-key");
            assertNotNull(filter);
        }

    }

    @Nested
    @DisplayName("Non-Protected Endpoint Tests")
    class NonProtectedEndpointTests {

        @Test
        @DisplayName("Should allow requests to non-protected endpoints")
        void testDoFilter_NonProtectedEndpoint_ShouldAllow() throws Exception {
            initFilter("https://example.com", "test-key");
            when(request.getRequestURI()).thenReturn("/api/health");
            filter.doFilter(request, response, filterChain);
            verify(filterChain).doFilter(request, response);
            verify(response, never()).setStatus(anyInt());
        }

        @Test
        @DisplayName("Should allow OPTIONS preflight requests")
        void testDoFilter_OptionsRequest_ShouldAllow() throws Exception {
            initFilter("https://example.com", "test-key");
            when(request.getRequestURI()).thenReturn("/blogs/published");
            when(request.getMethod()).thenReturn("OPTIONS");
            filter.doFilter(request, response, filterChain);
            verify(filterChain).doFilter(request, response);
            verify(response, never()).setStatus(anyInt());
        }

    }

    @Nested
    @DisplayName("Origin Header Tests")
    class OriginHeaderTests {

        @Test
        @DisplayName("Should allow valid origin")
        void testDoFilter_WithValidOrigin_ShouldAllow() throws Exception {
            initFilter("https://example.com,https://test.com", "test-key");
            when(request.getRequestURI()).thenReturn("/blogs/published");
            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader("Origin")).thenReturn("https://example.com");
            filter.doFilter(request, response, filterChain);
            verify(filterChain).doFilter(request, response);
            verify(response, never()).setStatus(anyInt());
        }

        @Test
        @DisplayName("Should block invalid origin")
        void testDoFilter_WithInvalidOrigin_ShouldBlock() throws Exception {
            initFilter("https://example.com", "test-key");
            when(request.getRequestURI()).thenReturn("/blogs/published");
            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader("Origin")).thenReturn("https://malicious.com");
            filter.doFilter(request, response, filterChain);
            verify(filterChain, never()).doFilter(request, response);
            verify(response).setStatus(HttpStatus.FORBIDDEN.value());
            verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        }

    }

    @Nested
    @DisplayName("API Key Tests")
    class ApiKeyTests {

        @Test
        @DisplayName("Should allow valid API key")
        void testDoFilter_WithValidApiKey_ShouldAllow() throws Exception {
            initFilter("https://example.com", "test-secret-key");
            when(request.getRequestURI()).thenReturn("/blogs/published");
            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader("X-API-Key")).thenReturn("test-secret-key");
            filter.doFilter(request, response, filterChain);
            verify(filterChain).doFilter(request, response);
            verify(response, never()).setStatus(anyInt());
        }

        @Test
        @DisplayName("Should block invalid API key when SSR is disabled")
        void testDoFilter_WithInvalidApiKey_SSRDisabled_ShouldBlock() throws Exception {
            initFilter("https://example.com", "test-secret-key", false);
            when(request.getRequestURI()).thenReturn("/blogs/published");
            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader("X-API-Key")).thenReturn("wrong-key");
            filter.doFilter(request, response, filterChain);
            verify(filterChain, never()).doFilter(request, response);
            verify(response).setStatus(HttpStatus.FORBIDDEN.value());
        }

        @Test
        @DisplayName("API key should take precedence over invalid origin")
        void testDoFilter_ApiKeyPrecedence_ShouldAllow() throws Exception {
            initFilter("https://example.com", "test-secret-key");
            when(request.getRequestURI()).thenReturn("/blogs/published");
            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader("X-API-Key")).thenReturn("test-secret-key");
            when(request.getHeader("Origin")).thenReturn("https://malicious.com");
            filter.doFilter(request, response, filterChain);
            verify(filterChain).doFilter(request, response);
            verify(response, never()).setStatus(anyInt());
        }

    }

    @Nested
    @DisplayName("Referer Header Tests")
    class RefererHeaderTests {

        @Test
        @DisplayName("Should allow valid referer")
        void testDoFilter_WithValidReferer_ShouldAllow() throws Exception {
            initFilter("https://example.com", "test-key");
            when(request.getRequestURI()).thenReturn("/blogs/published");
            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader("Referer")).thenReturn("https://example.com/blog/post");
            filter.doFilter(request, response, filterChain);
            verify(filterChain).doFilter(request, response);
            verify(response, never()).setStatus(anyInt());
        }

        @Test
        @DisplayName("Should block invalid referer when SSR is disabled")
        void testDoFilter_WithInvalidReferer_SSRDisabled_ShouldBlock() throws Exception {
            initFilter("https://example.com", "test-key", false);
            when(request.getRequestURI()).thenReturn("/blogs/published");
            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader("Referer")).thenReturn("https://malicious.com/hack");
            filter.doFilter(request, response, filterChain);
            verify(filterChain, never()).doFilter(request, response);
            verify(response).setStatus(HttpStatus.FORBIDDEN.value());
        }

        @Test
        @DisplayName("Should extract origin correctly from referer with port")
        void testDoFilter_RefererWithPort_ShouldExtractCorrectly() throws Exception {
            initFilter("https://example.com:8080", "test-key");
            when(request.getRequestURI()).thenReturn("/blogs/published");
            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader("Referer")).thenReturn("https://example.com:8080/blog");
            filter.doFilter(request, response, filterChain);
            verify(filterChain).doFilter(request, response);
            verify(response, never()).setStatus(anyInt());
        }

        @Test
        @DisplayName("Origin should take precedence over referer")
        void testDoFilter_OriginPrecedence_ShouldUseOrigin() throws Exception {
            initFilter("https://example.com", "test-key");
            when(request.getRequestURI()).thenReturn("/blogs/published");
            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader("Origin")).thenReturn("https://example.com");
            when(request.getHeader("Referer")).thenReturn("https://malicious.com/hack");
            filter.doFilter(request, response, filterChain);
            verify(filterChain).doFilter(request, response);
            verify(response, never()).setStatus(anyInt());
        }

    }

    @Nested
    @DisplayName("SSR Detection Tests")
    class SSRDetectionTests {

        @Test
        @DisplayName("Should allow SSR requests (no Origin) when SSR is enabled")
        void testDoFilter_NoOrigin_SSREnabled_ShouldAllow() throws Exception {
            initFilter("https://example.com", "test-key", true);
            when(request.getRequestURI()).thenReturn("/blogs/published");
            when(request.getMethod()).thenReturn("GET");
            when(request.getRemoteAddr()).thenReturn("127.0.0.1");
            filter.doFilter(request, response, filterChain);
            verify(filterChain).doFilter(request, response);
            verify(response, never()).setStatus(anyInt());
        }

        @Test
        @DisplayName("Should block requests without Origin when SSR is disabled")
        void testDoFilter_NoOrigin_SSRDisabled_ShouldBlock() throws Exception {
            initFilter("https://example.com", "test-key", false);
            when(request.getRequestURI()).thenReturn("/blogs/published");
            when(request.getMethod()).thenReturn("GET");
            filter.doFilter(request, response, filterChain);
            verify(filterChain, never()).doFilter(request, response);
            verify(response).setStatus(HttpStatus.FORBIDDEN.value());
        }

        @Test
        @DisplayName("Browser request with invalid Origin should still be blocked even with SSR enabled")
        void testDoFilter_BrowserWithInvalidOrigin_SSREnabled_ShouldBlock() throws Exception {
            initFilter("https://example.com", "test-key", true);
            when(request.getRequestURI()).thenReturn("/blogs/published");
            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader("Origin")).thenReturn("https://malicious.com");
            filter.doFilter(request, response, filterChain);
            verify(filterChain, never()).doFilter(request, response);
            verify(response).setStatus(HttpStatus.FORBIDDEN.value());
        }

    }

}
