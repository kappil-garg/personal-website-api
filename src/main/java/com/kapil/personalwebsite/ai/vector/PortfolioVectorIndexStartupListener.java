package com.kapil.personalwebsite.ai.vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Populates the vector store after the application is ready (and when embeddings are enabled).
 *
 * @author Kapil Garg
 */
@Component
@ConditionalOnProperty(prefix = "app.features", name = "embeddings.enabled", havingValue = "true")
@ConditionalOnBean(PortfolioVectorIndexService.class)
public class PortfolioVectorIndexStartupListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(PortfolioVectorIndexStartupListener.class);

    private final PortfolioVectorIndexService indexService;

    public PortfolioVectorIndexStartupListener(PortfolioVectorIndexService indexService) {
        this.indexService = indexService;
    }

    @Order(100)
    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        try {
            indexService.rebuildIndex();
        } catch (Exception ex) {
            LOGGER.warn("Initial portfolio vector index build failed; chat may fall back until reindex succeeds", ex);
        }
    }

}
