package com.kapil.personalwebsite.ai.config;

import com.kapil.personalwebsite.ai.portfolio.JevPortfolioChunkRelevanceService;
import com.kapil.personalwebsite.ai.portfolio.PortfolioChunkRelevanceService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuration for optional TypeSafe Jev portfolio chunk relevance checks.
 * This configuration is activated only when the JEV feature is enabled in the application properties.
 *
 * @author Kapil Garg
 */
@Configuration
@ConditionalOnProperty(prefix = "app.features.jev", name = "enabled", havingValue = "true")
public class PortfolioJevConfig {

    @Bean(name = "jevTaskExecutor")
    public TaskExecutor jevTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(32);
        executor.setThreadNamePrefix("jev-decision-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(5);
        executor.initialize();
        return executor;
    }

    @Bean
    public PortfolioChunkRelevanceService portfolioChunkRelevanceService(
            RestTemplateBuilder builder,
            @Value("${app.features.jev.api-url}") String apiUrl,
            @Value("${app.features.jev.api-key}") String apiKey,
            @Value("${app.features.jev.model}") String model,
            @Value("${app.features.jev.relevance-threshold}") double relevanceThreshold,
            @Value("${app.features.jev.log-decisions:false}") boolean logDecisions,
            @Qualifier("jevTaskExecutor") TaskExecutor taskExecutor) {
        RestTemplate jevRestTemplate = builder
                .connectTimeout(Duration.ofSeconds(2))
                .readTimeout(Duration.ofSeconds(5))
                .build();
        return new JevPortfolioChunkRelevanceService(
                jevRestTemplate, apiUrl, apiKey, model, relevanceThreshold, logDecisions, taskExecutor);
    }

}
