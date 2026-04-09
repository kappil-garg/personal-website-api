package com.kapil.personalwebsite.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Type-safe binding for all rate-limit configuration under the {rate.limit} prefix.
 *
 * @author Kapil Garg
 */
@ConfigurationProperties(prefix = "rate.limit")
public record RateLimitProperties(
        EndpointLimitConfig contact,
        EndpointLimitConfig contactPolish,
        EndpointLimitConfig blog,
        EndpointLimitConfig blogAsk,
        EndpointLimitConfig portfolioChat,
        boolean trustProxyHeaders
) {

    /**
     * Per-endpoint rate-limit settings split into two independent buckets.
     */
    public record EndpointLimitConfig(BucketConfig fingerprint, BucketConfig ip) {

        /**
         * Sliding-window thresholds for a single rate-limit bucket.
         *
         * @param maxRequests   maximum requests allowed within the window
         * @param windowMinutes duration of the sliding window in minutes
         */
        public record BucketConfig(int maxRequests, int windowMinutes) {
        }

    }

}
