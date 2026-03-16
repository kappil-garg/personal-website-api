package com.kapil.personalwebsite.ai.config;

import com.kapil.personalwebsite.ai.portfolio.PortfolioEmbeddingRetrievalService;
import com.kapil.personalwebsite.ai.portfolio.PortfolioEmbeddingRetrievalServiceImpl;
import com.kapil.personalwebsite.ai.portfolio.PortfolioRagService;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Central configuration for portfolio semantic retrieval using embeddings.
 *
 * @author Kapil Garg
 */
@Configuration
@ConditionalOnProperty(prefix = "app.features", name = "embeddings.enabled", havingValue = "true")
public class PortfolioEmbeddingConfig {

    @Bean
    @ConditionalOnBean(EmbeddingModel.class)
    public PortfolioEmbeddingRetrievalService portfolioEmbeddingRetrievalService(
            EmbeddingModel embeddingModel,
            PortfolioRagService portfolioRagService) {
        return new PortfolioEmbeddingRetrievalServiceImpl(embeddingModel, portfolioRagService);
    }

}
