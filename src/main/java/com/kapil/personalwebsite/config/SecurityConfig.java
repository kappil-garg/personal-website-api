package com.kapil.personalwebsite.config;

import com.kapil.personalwebsite.util.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Security configuration for the application, including CORS settings and admin authentication.
 *
 * @author Kapil Garg
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);

    private final long corsMaxAge;
    private final String adminUsername;
    private final String adminPassword;
    private final String corsAllowedOrigins;
    private final String corsAllowedMethods;
    private final boolean corsAllowCredentials;

    public SecurityConfig(@Value("${cors.max-age}") long corsMaxAge,
                          @Value("${admin.username}") String adminUsername,
                          @Value("${admin.password}") String adminPassword,
                          @Value("${cors.allowed-origins}") String corsAllowedOrigins,
                          @Value("${cors.allowed-methods}") String corsAllowedMethods,
                          @Value("${cors.allow-credentials}") boolean corsAllowCredentials) {
        this.corsMaxAge = corsMaxAge;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.corsAllowedOrigins = corsAllowedOrigins;
        this.corsAllowedMethods = corsAllowedMethods;
        this.corsAllowCredentials = corsAllowCredentials;
        validateAdminCredentials(adminUsername, adminPassword);
    }

    /**
     * Validates that admin credentials are properly configured.
     * Since admin endpoints require authentication, both username and password must be provided.
     *
     * @param adminUsername the admin username
     * @param adminPassword the admin password
     */
    private static void validateAdminCredentials(String adminUsername, String adminPassword) {
        boolean hasUsername = adminUsername != null && !adminUsername.trim().isEmpty();
        boolean hasPassword = adminPassword != null && !adminPassword.trim().isEmpty();
        if (hasUsername && !hasPassword) {
            throw new IllegalStateException(
                    "Admin password is required when admin username is configured. " +
                            "Please set the ADMIN_PASSWORD environment variable.");
        }
        if (!hasUsername && hasPassword) {
            throw new IllegalStateException(
                    "Admin username is required when admin password is configured. " +
                            "Please set the ADMIN_USERNAME environment variable.");
        }
    }

    /**
     * Configures HTTP security with CORS-based origin filtering for all endpoints.
     *
     * @return the security filter chain
     * @throws Exception in case of configuration errors
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                        // Explicitly apply default X-Content-Type-Options: nosniff
                        .contentTypeOptions(Customizer.withDefaults())
                        .httpStrictTransportSecurity(hsts -> hsts
                                .maxAgeInSeconds(31536000)
                        )
                        .xssProtection(Customizer.withDefaults())
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/blogs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/portfolio").permitAll()
                        .requestMatchers(HttpMethod.GET, "/experiences").permitAll()
                        .requestMatchers(HttpMethod.GET, "/projects").permitAll()
                        .requestMatchers(HttpMethod.GET, "/educations").permitAll()
                        .requestMatchers(HttpMethod.GET, "/certifications").permitAll()
                        .requestMatchers(HttpMethod.GET, "/skills").permitAll()
                        .requestMatchers(HttpMethod.POST, "/contact").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/actuator/info").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/portfolio").authenticated()
                        .anyRequest().authenticated()
                )
                .httpBasic(httpBasic -> httpBasic.realmName("Blog Admin API"))
                .userDetailsService(userDetailsService());
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        if (corsAllowedOrigins == null || corsAllowedOrigins.trim().isEmpty() || "null".equals(corsAllowedOrigins)) {
            LOGGER.warn("CORS allowed origins not configured. Browser-based frontend requests will be blocked. " +
                    "Set CORS_ALLOWED_ORIGINS environment variable to enable CORS for frontend access.");
            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", configuration);
            return source;
        }
        String[] origins = corsAllowedOrigins.split(",");
        for (String origin : origins) {
            String trimmedOrigin = origin.trim();
            if (!trimmedOrigin.isEmpty()) {
                configuration.addAllowedOrigin(trimmedOrigin);
            }
        }
        String[] methods = corsAllowedMethods.split(",");
        for (String method : methods) {
            String trimmedMethod = method.trim();
            if (!trimmedMethod.isEmpty()) {
                configuration.addAllowedMethod(trimmedMethod);
            }
        }
        configuration.addAllowedHeader(AppConstants.CONTENT_TYPE_HEADER);
        configuration.addAllowedHeader(AppConstants.AUTHORIZATION_HEADER);
        configuration.addAllowedHeader(AppConstants.API_KEY_HEADER);
        configuration.addAllowedHeader(AppConstants.ORIGIN_HEADER);
        configuration.addAllowedHeader(AppConstants.REFERER_HEADER);
        configuration.addAllowedHeader(AppConstants.ACCEPT_HEADER);
        configuration.setAllowCredentials(corsAllowCredentials);
        configuration.setMaxAge(corsMaxAge);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Creates in-memory user details for admin authentication.
     *
     * @return the user details service
     */
    @Bean
    public UserDetailsService userDetailsService() {
        if (adminUsername == null || adminUsername.trim().isEmpty()) {
            LOGGER.warn("Admin credentials not configured. Admin endpoints requiring authentication will be inaccessible. " +
                    "Set ADMIN_USERNAME and ADMIN_PASSWORD environment variables to enable admin access.");
            return new InMemoryUserDetailsManager();
        }
        UserDetails admin = User.builder()
                .username(adminUsername)
                .password(passwordEncoder().encode(adminPassword))
                .roles(AppConstants.ADMIN_ROLE)
                .build();
        LOGGER.info("Admin user '{}' configured successfully.", adminUsername);
        return new InMemoryUserDetailsManager(admin);
    }

    /**
     * Password encoder for secure password storage.
     *
     * @return the password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
