package com.kapil.personalwebsite.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
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

    @Mock
    private FilterConfig filterConfig;

    @BeforeEach
    void setUp() throws ServletException, IOException {
        filter = new OriginVerificationFilter();
        StringWriter responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    private void initFilter(String allowedOrigins, String serverApiKey) throws ServletException {
        ReflectionTestUtils.setField(filter, "allowedOrigins", allowedOrigins);
        ReflectionTestUtils.setField(filter, "serverApiKey", serverApiKey);
        filter.init(filterConfig);
    }

    @Test
    void testInit_WithAllowedOrigins_ShouldCacheOrigins() throws ServletException {
        initFilter("https://example.com,https://test.com", "test-key");
        assertNotNull(filter);
    }

    @Test
    void testInit_WithEmptyOrigins_ShouldHandleGracefully() throws ServletException {
        initFilter("", "test-key");
        assertNotNull(filter);
    }

    @Test
    void testDoFilter_NonProtectedEndpoint_ShouldAllow() throws Exception {
        initFilter("https://example.com", "test-key");
        when(request.getRequestURI()).thenReturn("/api/health");
        filter.doFilter(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void testDoFilter_ProtectedEndpoint_PublishedBlogs_WithValidOrigin_ShouldAllow() throws Exception {
        initFilter("https://example.com,https://test.com", "test-key");
        when(request.getRequestURI()).thenReturn("/blogs/published");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Origin")).thenReturn("https://example.com");
        filter.doFilter(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void testDoFilter_ProtectedEndpoint_ViewPath_WithValidOrigin_ShouldAllow() throws Exception {
        initFilter("https://example.com", "test-key");
        when(request.getRequestURI()).thenReturn("/blogs/123/view");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Origin")).thenReturn("https://example.com");
        filter.doFilter(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void testDoFilter_ProtectedEndpoint_WithInvalidOrigin_ShouldBlock() throws Exception {
        initFilter("https://example.com", "test-key");
        when(request.getRequestURI()).thenReturn("/blogs/published");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Origin")).thenReturn("https://malicious.com");
        filter.doFilter(request, response, filterChain);
        verify(filterChain, never()).doFilter(request, response);
        verify(response).setStatus(HttpStatus.FORBIDDEN.value());
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    void testDoFilter_ProtectedEndpoint_WithValidApiKey_ShouldAllow() throws Exception {
        initFilter("https://example.com", "test-secret-key");
        when(request.getRequestURI()).thenReturn("/blogs/published");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("X-API-Key")).thenReturn("test-secret-key");
        filter.doFilter(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void testDoFilter_ProtectedEndpoint_WithInvalidApiKey_ShouldBlock() throws Exception {
        initFilter("https://example.com", "test-secret-key");
        when(request.getRequestURI()).thenReturn("/blogs/published");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("X-API-Key")).thenReturn("wrong-key");
        filter.doFilter(request, response, filterChain);
        verify(filterChain, never()).doFilter(request, response);
        verify(response).setStatus(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void testDoFilter_ProtectedEndpoint_WithValidReferer_ShouldAllow() throws Exception {
        initFilter("https://example.com", "test-key");
        when(request.getRequestURI()).thenReturn("/blogs/published");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Referer")).thenReturn("https://example.com/blog/post");
        filter.doFilter(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void testDoFilter_ProtectedEndpoint_WithInvalidReferer_ShouldBlock() throws Exception {
        initFilter("https://example.com", "test-key");
        when(request.getRequestURI()).thenReturn("/blogs/published");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Referer")).thenReturn("https://malicious.com/hack");
        filter.doFilter(request, response, filterChain);
        verify(filterChain, never()).doFilter(request, response);
        verify(response).setStatus(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void testDoFilter_ProtectedEndpoint_NoOriginNoRefererNoApiKey_ShouldBlock() throws Exception {
        initFilter("https://example.com", "test-key");
        when(request.getRequestURI()).thenReturn("/blogs/published");
        when(request.getMethod()).thenReturn("GET");
        filter.doFilter(request, response, filterChain);
        verify(filterChain, never()).doFilter(request, response);
        verify(response).setStatus(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void testDoFilter_OptionsRequest_ShouldAllow() throws Exception {
        initFilter("https://example.com", "test-key");
        when(request.getRequestURI()).thenReturn("/blogs/published");
        when(request.getMethod()).thenReturn("OPTIONS");
        filter.doFilter(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
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
    void testDoFilter_MalformedReferer_ShouldBlock() throws Exception {
        initFilter("https://example.com", "test-key");
        when(request.getRequestURI()).thenReturn("/blogs/published");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Referer")).thenReturn("not-a-valid-url");
        filter.doFilter(request, response, filterChain);
        verify(filterChain, never()).doFilter(request, response);
        verify(response).setStatus(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void testDoFilter_MultipleAllowedOrigins_ShouldMatchAny() throws Exception {
        initFilter("https://example.com,https://test.com,https://staging.com", "test-key");
        when(request.getRequestURI()).thenReturn("/blogs/published");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Origin")).thenReturn("https://test.com");
        filter.doFilter(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void testDoFilter_NullRequestPath_ShouldNotProtect() throws Exception {
        initFilter("https://example.com", "test-key");
        when(request.getRequestURI()).thenReturn(null);
        filter.doFilter(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void testDoFilter_ProtectedEndpoint_OriginTakesPrecedenceOverReferer() throws Exception {
        initFilter("https://example.com", "test-key");
        when(request.getRequestURI()).thenReturn("/blogs/published");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Origin")).thenReturn("https://example.com");
        when(request.getHeader("Referer")).thenReturn("https://malicious.com/hack");
        filter.doFilter(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void testDoFilter_ProtectedEndpoint_ApiKeyTakesPrecedenceOverOrigin() throws Exception {
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
