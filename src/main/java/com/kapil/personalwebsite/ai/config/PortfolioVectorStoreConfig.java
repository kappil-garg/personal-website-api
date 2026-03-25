package com.kapil.personalwebsite.ai.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Configures a VectorStore for portfolio RAG use cases.
 * If pgvector configuration is provided, it sets up a PgVectorStore with a DataSource and JdbcTemplate.
 *
 * @author Kapil Garg
 */
@Configuration
@ConditionalOnProperty(prefix = "app.features", name = "embeddings.enabled", havingValue = "true")
public class PortfolioVectorStoreConfig {

    @Bean(name = "portfolioVectorStoreDataSource")
    @Conditional(NonBlankPortfolioVectorJdbcUrlCondition.class)
    public DataSource portfolioVectorStoreDataSource(
            @Value("${app.vector-store.pgvector.jdbc-url}") String jdbcUrl,
            @Value("${app.vector-store.pgvector.username}") String username,
            @Value("${app.vector-store.pgvector.password}") String password,
            @Value("${app.vector-store.pgvector.maximum-pool-size}") int maxPoolSize) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(jdbcUrl);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setMaximumPoolSize(maxPoolSize);
        ds.setPoolName("portfolio-pgvector-pool");
        return ds;
    }

    @Bean(name = "portfolioVectorStoreJdbcTemplate")
    @ConditionalOnBean(name = "portfolioVectorStoreDataSource")
    public JdbcTemplate portfolioVectorStoreJdbcTemplate(
            @Qualifier("portfolioVectorStoreDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    @ConditionalOnBean(name = "portfolioVectorStoreJdbcTemplate")
    public VectorStore pgVectorStore(
            @Qualifier("portfolioVectorStoreJdbcTemplate") JdbcTemplate jdbcTemplate,
            EmbeddingModel embeddingModel) {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .vectorTableName("portfolio_vector_store")
                .initializeSchema(true)
                .dimensions(3072)                          // gemini-embedding-001 output size
                .indexType(PgVectorStore.PgIndexType.NONE) // HNSW max is 2000 dims
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(VectorStore.class)
    public VectorStore simplePortfolioVectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

}
