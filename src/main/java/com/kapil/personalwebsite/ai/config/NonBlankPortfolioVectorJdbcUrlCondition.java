package com.kapil.personalwebsite.ai.config;

import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

/**
 * Matches when {@code app.vector-store.pgvector.jdbc-url} is present and non-blank.
 * Used to conditionally enable PostgreSQL vector store configuration when a valid JDBC URL is provided.
 *
 * @author Kapil Garg
 */
public class NonBlankPortfolioVectorJdbcUrlCondition implements Condition {

    private static final String PROPERTY = "app.vector-store.pgvector.jdbc-url";

    @Override
    public boolean matches(ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
        return StringUtils.hasText(context.getEnvironment().getProperty(PROPERTY));
    }

}
