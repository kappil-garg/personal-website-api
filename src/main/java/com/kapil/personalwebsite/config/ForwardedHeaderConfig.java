package com.kapil.personalwebsite.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.ForwardedHeaderFilter;

/**
 * Configuration for handling forwarded headers to capture real client IP addresses.
 *
 * @author Kapil Garg
 */
@Configuration
public class ForwardedHeaderConfig {

    /**
     * Registers ForwardedHeaderFilter to process X-Forwarded-* headers.
     *
     * @return FilterRegistrationBean for ForwardedHeaderFilter
     */
    @Bean
    public FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter() {
        FilterRegistrationBean<ForwardedHeaderFilter> registrationBean = new FilterRegistrationBean<>();
        ForwardedHeaderFilter filter = new ForwardedHeaderFilter();
        registrationBean.setFilter(filter);
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }

}
