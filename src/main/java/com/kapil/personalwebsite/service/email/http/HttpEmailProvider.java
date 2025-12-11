package com.kapil.personalwebsite.service.email.http;

import com.kapil.personalwebsite.util.AppConstants;

/**
 * Constants for supported HTTP email providers.
 *
 * @author Kapil Garg
 */
public final class HttpEmailProvider {

    public static final String BREVO = "brevo";

    private HttpEmailProvider() {
        throw new IllegalStateException(AppConstants.UTILITY_CLASS_INSTANTIATION_MSG);
    }

    /**
     * Normalizes provider key for map lookup.
     *
     * @param provider the provider key
     * @return normalized provider key
     */
    public static String normalize(String provider) {
        return provider == null ? "" : provider.trim().toLowerCase();
    }

}
