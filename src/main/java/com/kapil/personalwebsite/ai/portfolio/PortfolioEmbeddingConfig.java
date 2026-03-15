package com.kapil.personalwebsite.ai.portfolio;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers portfolio embedding retrieval only when an EmbeddingModel is available.
 *
 * @author Kapil Garg
 */
@Configuration
public class PortfolioEmbeddingConfig {

    @Bean
    @ConditionalOnBean(EmbeddingModel.class)
    public PortfolioEmbeddingRetrievalService portfolioEmbeddingRetrievalServiceImpl(
            EmbeddingModel embeddingModel,
            PortfolioRagService portfolioRagService) {
        return new PortfolioEmbeddingRetrievalServiceImpl(embeddingModel, portfolioRagService);
    }

}
