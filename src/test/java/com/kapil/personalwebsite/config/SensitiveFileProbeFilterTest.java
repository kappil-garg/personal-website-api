package com.kapil.personalwebsite.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SensitiveFileProbeFilterTest {

    private SensitiveFileProbeFilter filter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private StringWriter responseWriter;

    @BeforeEach
    void setUp() {
        responseWriter = new StringWriter();
    }

    private void stubResponseWriter() throws IOException {
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    private void initFilter(String blockedPaths) {
        filter = new SensitiveFileProbeFilter(blockedPaths);
    }

    @Nested
    @DisplayName("Path matching")
    class PathMatchingTests {

        @Test
        @DisplayName("Blocks default sensitive paths (case-insensitive)")
        void shouldBlockDefaultSensitivePath() throws Exception {
            initFilter("");
            stubResponseWriter();
            when(request.getRequestURI()).thenReturn("/CONFIG.PHP");
            when(request.getRemoteAddr()).thenReturn("192.168.0.10");
            filter.doFilter(request, response, filterChain);
            verify(filterChain, never()).doFilter(request, response);
            verify(request).getRemoteAddr();
            verify(response).setStatus(HttpStatus.NOT_FOUND.value());
            verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
            assertThat(responseWriter.toString()).contains("\"status\": 404");
        }

        @Test
        @DisplayName("Allows non-sensitive paths to proceed")
        void shouldAllowNonSensitivePath() throws Exception {
            initFilter("");
            when(request.getRequestURI()).thenReturn("/api/health");
            filter.doFilter(request, response, filterChain);
            verify(filterChain).doFilter(request, response);
            verify(response, never()).setStatus(anyInt());
        }

    }

    @Nested
    @DisplayName("Custom blocked paths")
    class CustomBlockedPathTests {

        @Test
        @DisplayName("Uses custom blocked list instead of defaults")
        void shouldRespectCustomBlockedPaths() throws Exception {
            initFilter("/private/secret");
            when(request.getRequestURI()).thenReturn("/.env");
            filter.doFilter(request, response, filterChain);
            verify(filterChain).doFilter(request, response);
            verify(response, never()).setStatus(anyInt());
        }

        @Test
        @DisplayName("Blocks custom configured path")
        void shouldBlockCustomPath() throws Exception {
            initFilter("/private/secret");
            stubResponseWriter();
            when(request.getRequestURI()).thenReturn("/private/secret/data");
            filter.doFilter(request, response, filterChain);
            verify(filterChain, never()).doFilter(request, response);
            verify(response).setStatus(HttpStatus.NOT_FOUND.value());
            assertThat(responseWriter.toString()).contains("\"Not Found\"");
        }

    }

    @Nested
    @DisplayName("404 response payload")
    class NotFoundResponseTests {

        @Test
        @DisplayName("Writes JSON 404 response for probes")
        void shouldReturnJsonNotFound() throws Exception {
            initFilter("");
            stubResponseWriter();
            when(request.getRequestURI()).thenReturn("/.env");
            filter.doFilter(request, response, filterChain);
            verify(response).setStatus(HttpStatus.NOT_FOUND.value());
            verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
            assertThat(responseWriter.toString()).isEqualTo("""
                    {
                        "error": "Not Found",
                        "status": 404,
                        "message": "The requested resource was not found"
                    }
                    """);
        }

    }

    @Nested
    @DisplayName("Exception handling")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("Propagates IOException via ExceptionUtils when path is null")
        void shouldPropagateIOExceptionOnNullPath() throws Exception {
            initFilter("");
            when(request.getRequestURI()).thenReturn(null);
            IOException ioException = new IOException("boom");
            doThrow(ioException).when(filterChain).doFilter(request, response);
            IOException thrown = assertThrows(IOException.class,
                    () -> filter.doFilter(request, response, filterChain));
            assertThat(thrown).isSameAs(ioException);
        }

        @Test
        @DisplayName("Propagates ServletException for non-sensitive paths")
        void shouldPropagateServletException() throws Exception {
            initFilter("");
            when(request.getRequestURI()).thenReturn("/api/data");
            ServletException servletException = new ServletException("error");
            doThrow(servletException).when(filterChain).doFilter(request, response);
            ServletException thrown = assertThrows(ServletException.class,
                    () -> filter.doFilter(request, response, filterChain));
            assertThat(thrown).isSameAs(servletException);
        }

    }

}
