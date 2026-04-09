package com.kapil.personalwebsite.config;

import com.kapil.personalwebsite.util.AppConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RateLimitFilter.
 *
 * @author Kapil Garg
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RateLimitFilterTest {

    private RateLimitFilter filter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() throws Exception {
        StringWriter responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        filter = buildFilter(5, 60);
    }

    private RateLimitFilter buildFilter(int maxRequests, int windowMinutes) {
        return buildFilter(maxRequests, windowMinutes, maxRequests * 10, windowMinutes);
    }

    private RateLimitFilter buildFilter(int fpMax, int fpWindow, int ipMax, int ipWindow) {
        var fp = new RateLimitProperties.EndpointLimitConfig.BucketConfig(fpMax, fpWindow);
        var ip = new RateLimitProperties.EndpointLimitConfig.BucketConfig(ipMax, ipWindow);
        var cfg = new RateLimitProperties.EndpointLimitConfig(fp, ip);
        RateLimitProperties properties = new RateLimitProperties(cfg, cfg, cfg, cfg, cfg, false);
        return new RateLimitFilter(properties);
    }

    private void stubContactPost(String userAgent) {
        when(request.getMethod()).thenReturn("POST");
        when(request.getServletPath()).thenReturn("/contact");
        when(request.getRemoteAddr()).thenReturn("1.2.3.4");
        when(request.getHeader(AppConstants.USER_AGENT_HEADER)).thenReturn(userAgent);
        when(request.getHeader(AppConstants.ACCEPT_LANGUAGE_HEADER)).thenReturn("en-US");
        when(request.getRequestURI()).thenReturn("/api/contact");
    }

    @Nested
    @DisplayName("buildClientFingerprint")
    class BuildClientFingerprintTests {

        @Test
        @DisplayName("same IP + same UA + same lang → same fingerprint")
        void sameInputs_sameFingerprintEveryTime() {
            when(request.getHeader(AppConstants.USER_AGENT_HEADER)).thenReturn("Mozilla/5.0");
            when(request.getHeader(AppConstants.ACCEPT_LANGUAGE_HEADER)).thenReturn("en-US");
            String fp1 = filter.buildClientFingerprint("1.2.3.4", request);
            String fp2 = filter.buildClientFingerprint("1.2.3.4", request);
            assertEquals(fp1, fp2);
        }

        @Test
        @DisplayName("same IP + different UA → different fingerprints")
        void sameIpDifferentUserAgent_differentFingerprint() {
            HttpServletRequest req1 = mock(HttpServletRequest.class);
            HttpServletRequest req2 = mock(HttpServletRequest.class);
            when(req1.getHeader(AppConstants.USER_AGENT_HEADER)).thenReturn("Chrome/120");
            when(req1.getHeader(AppConstants.ACCEPT_LANGUAGE_HEADER)).thenReturn("en-US");
            when(req2.getHeader(AppConstants.USER_AGENT_HEADER)).thenReturn("Firefox/121");
            when(req2.getHeader(AppConstants.ACCEPT_LANGUAGE_HEADER)).thenReturn("en-US");
            String fp1 = filter.buildClientFingerprint("1.2.3.4", req1);
            String fp2 = filter.buildClientFingerprint("1.2.3.4", req2);
            assertNotEquals(fp1, fp2, "Different User-Agents on same IP must yield different fingerprints");
        }

        @Test
        @DisplayName("different IPs + same UA → different fingerprints")
        void differentIpSameUserAgent_differentFingerprint() {
            when(request.getHeader(AppConstants.USER_AGENT_HEADER)).thenReturn("Chrome/120");
            when(request.getHeader(AppConstants.ACCEPT_LANGUAGE_HEADER)).thenReturn("en-US");
            String fp1 = filter.buildClientFingerprint("1.2.3.4", request);
            String fp2 = filter.buildClientFingerprint("5.6.7.8", request);
            assertNotEquals(fp1, fp2);
        }

        @Test
        @DisplayName("fingerprint is exactly 24 hex characters")
        void fingerprint_hasExpectedLength() {
            when(request.getHeader(AppConstants.USER_AGENT_HEADER)).thenReturn("TestAgent");
            when(request.getHeader(AppConstants.ACCEPT_LANGUAGE_HEADER)).thenReturn("en");
            String fp = filter.buildClientFingerprint("127.0.0.1", request);
            assertThat(fp).hasSize(24).matches("[0-9a-f]+");
        }

        @Test
        @DisplayName("null UA and null Accept-Language are handled without NPE")
        void nullHeaders_noException() {
            when(request.getHeader(AppConstants.USER_AGENT_HEADER)).thenReturn(null);
            when(request.getHeader(AppConstants.ACCEPT_LANGUAGE_HEADER)).thenReturn(null);
            assertDoesNotThrow(() -> filter.buildClientFingerprint("1.2.3.4", request));
        }

    }

    @Nested
    @DisplayName("Rate limit enforcement")
    class RateLimitEnforcementTests {

        @Test
        @DisplayName("requests within limit are allowed through to filter chain")
        void withinLimit_requestAllowed() throws Exception {
            stubContactPost("Chrome/120");
            filter.doFilter(request, response, filterChain);
            verify(filterChain).doFilter(request, response);
            verify(response, never()).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        }

        @Test
        @DisplayName("request exactly at limit is blocked with 429")
        void atLimit_requestBlocked() throws Exception {
            filter = buildFilter(2, 60);
            stubContactPost("Chrome/120");
            // fill the window (2 allowed)
            filter.doFilter(request, response, filterChain);
            filter.doFilter(request, response, filterChain);
            // 3rd must be rejected
            filter.doFilter(request, response, filterChain);
            ArgumentCaptor<Integer> statusCaptor = ArgumentCaptor.forClass(Integer.class);
            verify(response, atLeastOnce()).setStatus(statusCaptor.capture());
            assertThat(statusCaptor.getAllValues()).contains(HttpStatus.TOO_MANY_REQUESTS.value());
        }

        @Test
        @DisplayName("two browsers on the same IP have independent buckets")
        void sameIpDifferentUA_independentBuckets() throws Exception {
            filter = buildFilter(1, 60);
            // Browser A
            stubContactPost("Chrome/120");
            filter.doFilter(request, response, filterChain);
            // Browser B — same IP, different UA
            reset(request);
            stubContactPost("Firefox/121");
            filter.doFilter(request, response, filterChain);
            // Both should have reached the filter chain
            verify(filterChain, times(2)).doFilter(any(), any());
            verify(response, never()).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        }

        @Test
        @DisplayName("OPTIONS requests bypass rate limiting")
        void optionsRequest_bypassed() throws Exception {
            when(request.getMethod()).thenReturn("OPTIONS");
            filter.doFilter(request, response, filterChain);
            verify(filterChain).doFilter(request, response);
            verifyNoInteractions(response);
        }

        @Test
        @DisplayName("IP aggregate bucket blocks header-rotating abuser even when each fingerprint bucket is fresh")
        void ipAggregateBucket_blocksHeaderRotation() throws Exception {
            // fp=1/60min per fingerprint bucket, ip=2/60min shared across all fingerprints from this IP
            filter = buildFilter(1, 60, 2, 60);
            // Request 1 — UA-A fills fp_A (1/1) and consumes IP slot 1/2
            stubContactPost("Chrome/120");
            filter.doFilter(request, response, filterChain);
            // Request 2 — UA-B fills fp_B (1/1) and consumes IP slot 2/2
            reset(request);
            stubContactPost("Firefox/121");
            filter.doFilter(request, response, filterChain);
            verify(filterChain, times(2)).doFilter(any(), any());
            // Request 3 — UA-C has a fresh fingerprint bucket but the IP aggregate is exhausted → 429
            reset(request);
            reset(filterChain);
            stubContactPost("Safari/17");
            filter.doFilter(request, response, filterChain);
            verify(filterChain, never()).doFilter(any(), any());
            verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        }

        @Test
        @DisplayName("X-RateLimit-* headers reflect fingerprint bucket limits when IP aggregate blocks")
        void headersReflectFingerprintBucket_whenIpAggregateBlocks() throws Exception {
            // fp=5/60min, ip=2/60min — IP aggregate exhausted after 2 requests
            filter = buildFilter(5, 60, 2, 60);
            stubContactPost("Chrome/120");
            filter.doFilter(request, response, filterChain);
            reset(request);
            stubContactPost("Firefox/121");
            filter.doFilter(request, response, filterChain);
            // 3rd request from a new UA — fp bucket is fresh but IP is exhausted
            reset(request);
            reset(response);
            when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
            stubContactPost("Safari/17");
            filter.doFilter(request, response, filterChain);
            verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            // X-RateLimit-Limit must reflect the fingerprint config (5), not the IP config (2)
            verify(response).setHeader(eq(AppConstants.X_RATE_LIMIT_LIMIT), eq("5"));
        }

    }

    @Nested
    @DisplayName("X-RateLimit-* response headers")
    class RateLimitHeaderTests {

        @Test
        @DisplayName("allowed response includes X-RateLimit-Limit, -Remaining, -Reset")
        void allowedRequest_hasRateLimitHeaders() throws Exception {
            filter = buildFilter(5, 60);
            stubContactPost("Chrome/120");
            filter.doFilter(request, response, filterChain);
            verify(response).setHeader(eq(AppConstants.X_RATE_LIMIT_LIMIT), eq("5"));
            verify(response).setHeader(eq(AppConstants.X_RATE_LIMIT_REMAINING), anyString());
            verify(response).setHeader(eq(AppConstants.X_RATE_LIMIT_RESET), anyString());
        }

        @Test
        @DisplayName("X-RateLimit-Remaining decrements with each request")
        void remaining_decrementsWithEachRequest() throws Exception {
            filter = buildFilter(3, 60);
            stubContactPost("Chrome/120");
            ArgumentCaptor<String> remainingCaptor = ArgumentCaptor.forClass(String.class);
            filter.doFilter(request, response, filterChain); // 1st — remaining = 2
            filter.doFilter(request, response, filterChain); // 2nd — remaining = 1
            filter.doFilter(request, response, filterChain); // 3rd — remaining = 0
            verify(response, atLeast(3)).setHeader(eq(AppConstants.X_RATE_LIMIT_REMAINING), remainingCaptor.capture());
            assertThat(remainingCaptor.getAllValues()).contains("2", "1", "0");
        }

        @Test
        @DisplayName("429 response still includes X-RateLimit-* headers with Remaining=0")
        void blockedRequest_hasRateLimitHeadersWithZeroRemaining() throws Exception {
            filter = buildFilter(1, 60);
            stubContactPost("Chrome/120");
            filter.doFilter(request, response, filterChain); // allowed
            reset(filterChain);
            filter.doFilter(request, response, filterChain); // blocked with 429
            verify(response, atLeastOnce()).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            verify(response, atLeast(2)).setHeader(eq(AppConstants.X_RATE_LIMIT_REMAINING), anyString());
            // Last call to setHeader for REMAINING should be "0"
            ArgumentCaptor<String> remainingCaptor = ArgumentCaptor.forClass(String.class);
            verify(response, atLeast(2)).setHeader(eq(AppConstants.X_RATE_LIMIT_REMAINING), remainingCaptor.capture());
            assertThat(remainingCaptor.getAllValues()).contains("0");
        }

        @Test
        @DisplayName("X-RateLimit-Reset is a positive epoch second value")
        void reset_isPositiveEpochSecond() throws Exception {
            filter = buildFilter(5, 60);
            stubContactPost("Chrome/120");
            ArgumentCaptor<String> resetCaptor = ArgumentCaptor.forClass(String.class);
            filter.doFilter(request, response, filterChain);
            verify(response).setHeader(eq(AppConstants.X_RATE_LIMIT_RESET), resetCaptor.capture());
            long resetValue = Long.parseLong(resetCaptor.getValue());
            assertThat(resetValue).isGreaterThan(System.currentTimeMillis() / 1000);
        }

        @Test
        @DisplayName("non-matched endpoints do not receive X-RateLimit-* headers")
        void nonMatchedEndpoint_noRateLimitHeaders() throws Exception {
            when(request.getMethod()).thenReturn("GET");
            when(request.getServletPath()).thenReturn("/portfolio");
            when(request.getRemoteAddr()).thenReturn("1.2.3.4");
            filter.doFilter(request, response, filterChain);
            verify(response, never()).setHeader(eq(AppConstants.X_RATE_LIMIT_LIMIT), anyString());
        }

    }

    @Nested
    @DisplayName("Retry-After header on 429")
    class RetryAfterHeaderTests {

        @Test
        @DisplayName("blocked response includes Retry-After header in seconds")
        void blockedRequest_hasRetryAfterHeader() throws Exception {
            filter = buildFilter(1, 2); // 2-minute window
            stubContactPost("Chrome/120");
            filter.doFilter(request, response, filterChain); // allowed
            reset(filterChain);
            filter.doFilter(request, response, filterChain); // blocked
            verify(response).setHeader("Retry-After", "120");
        }

    }

}
