package com.kapil.personalwebsite.config;

import com.kapil.personalwebsite.util.AppConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
 * Security configuration for the blog API using Spring Security.
 * Configures Basic Authentication for admin endpoints and allows public access to read-only endpoints.
 *
 * @author Kapil Garg
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

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
        validateAdminCredentials(adminUsername, adminPassword);
        this.corsMaxAge = corsMaxAge;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.corsAllowedOrigins = corsAllowedOrigins;
        this.corsAllowedMethods = corsAllowedMethods;
        this.corsAllowCredentials = corsAllowCredentials;
    }

    /**
     * Validates that admin credentials are provided.
     *
     * @param adminUsername the admin username
     * @param adminPassword the admin password
     */
    private static void validateAdminCredentials(String adminUsername, String adminPassword) {
        if (adminUsername == null || adminUsername.trim().isEmpty()) {
            throw new IllegalStateException(
                    "Admin username is required but not configured. " +
                            "Please set the ADMIN_USERNAME environment variable.");
        }
        if (adminPassword == null || adminPassword.trim().isEmpty()) {
            throw new IllegalStateException(
                    "Admin password is required but not configured. " +
                            "Please set the ADMIN_PASSWORD environment variable.");
        }
    }

    /**
     * Configures HTTP security with Basic Authentication for admin endpoints.
     * Public endpoints remain accessible without authentication.
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
                        .contentTypeOptions(contentTypeOptions -> {
                        })
                        .httpStrictTransportSecurity(hsts -> hsts
                                .maxAgeInSeconds(31536000)
                        )
                        .xssProtection(xss -> {
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/blogs/published/**").permitAll()
                        .requestMatchers("/blogs/*/view").permitAll()
                        .requestMatchers(HttpMethod.GET, "/portfolio").permitAll()
                        .requestMatchers(HttpMethod.GET, "/experiences").permitAll()
                        .requestMatchers(HttpMethod.GET, "/projects").permitAll()
                        .requestMatchers(HttpMethod.POST, "/contact").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/blogs").authenticated()
                        .requestMatchers("/blogs/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/portfolio").authenticated()
                        .anyRequest().permitAll()
                )
                .httpBasic(httpBasic -> httpBasic.realmName("Blog Admin API"))
                .userDetailsService(userDetailsService());
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // If not configured, default to localhost:4200 for development
        String originsToUse = (corsAllowedOrigins == null || corsAllowedOrigins.trim().isEmpty() || corsAllowedOrigins.equals("null"))
                ? "http://localhost:4200"
                : corsAllowedOrigins;
        String[] origins = originsToUse.split(",");
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
        configuration.addAllowedHeader(AppConstants.USER_AGENT_HEADER);
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
        UserDetails admin = User.builder()
                .username(adminUsername)
                .password(passwordEncoder().encode(adminPassword))
                .roles(AppConstants.ADMIN_ROLE)
                .build();
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
