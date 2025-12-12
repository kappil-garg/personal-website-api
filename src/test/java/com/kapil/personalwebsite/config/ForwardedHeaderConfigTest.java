package com.kapil.personalwebsite.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.Ordered;
import org.springframework.web.filter.ForwardedHeaderFilter;

import static org.assertj.core.api.Assertions.assertThat;

class ForwardedHeaderConfigTest {

    private final ForwardedHeaderConfig config = new ForwardedHeaderConfig();

    @Test
    @DisplayName("Registers ForwardedHeaderFilter bean")
    void shouldRegisterForwardedHeaderFilter() {
        FilterRegistrationBean<ForwardedHeaderFilter> registration = config.forwardedHeaderFilter();
        assertThat(registration).isNotNull();
        assertThat(registration.getFilter()).isInstanceOf(ForwardedHeaderFilter.class);
    }

    @Test
    @DisplayName("Sets highest precedence order")
    void shouldSetHighestPrecedenceOrder() {
        FilterRegistrationBean<ForwardedHeaderFilter> registration = config.forwardedHeaderFilter();
        assertThat(registration.getOrder()).isEqualTo(Ordered.HIGHEST_PRECEDENCE);
    }

    @Test
    @DisplayName("Applies filter to all URL patterns")
    void shouldApplyToAllUrlPatterns() {
        FilterRegistrationBean<ForwardedHeaderFilter> registration = config.forwardedHeaderFilter();
        assertThat(registration.getUrlPatterns())
                .containsExactly("/*");
    }

}
