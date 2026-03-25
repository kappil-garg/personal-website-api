package com.kapil.personalwebsite.ai.config;

import com.kapil.personalwebsite.ai.portfolio.PortfolioEmbeddingRetrievalService;
import com.kapil.personalwebsite.ai.portfolio.PortfolioEmbeddingRetrievalServiceImpl;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Central configuration for portfolio semantic retrieval using embeddings.
 * The class-level @ConditionalOnProperty ensures this only activates when embeddings are enabled.
 *
 * @author Kapil Garg
 */
@Configuration
@ConditionalOnProperty(prefix = "app.features", name = "embeddings.enabled", havingValue = "true")
@Import(PortfolioVectorStoreConfig.class)
public class PortfolioEmbeddingConfig {

    @Bean
    public PortfolioEmbeddingRetrievalService portfolioEmbeddingRetrievalService(VectorStore vectorStore) {
        return new PortfolioEmbeddingRetrievalServiceImpl(vectorStore);
    }

}
