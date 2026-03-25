package com.kapil.personalwebsite.ai.vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Full rebuild of the portfolio vector index (delete namespace, re-embed all chunks).
 *
 * @author Kapil Garg
 */
@Service
@ConditionalOnProperty(prefix = "app.features", name = "embeddings.enabled", havingValue = "true")
@ConditionalOnBean(VectorStore.class)
public class PortfolioVectorIndexService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PortfolioVectorIndexService.class);

    private final VectorStore vectorStore;
    private final PortfolioChunkDocumentService chunkDocumentService;

    @Value("${app.features.embeddings.index-batch-size}")
    private int indexBatchSize;

    @Value("${app.features.embeddings.index-batch-delay-ms}")
    private long indexBatchDelayMs;

    public PortfolioVectorIndexService(VectorStore vectorStore,
                                       PortfolioChunkDocumentService chunkDocumentService) {
        this.vectorStore = vectorStore;
        this.chunkDocumentService = chunkDocumentService;
    }

    /**
     * Fires a portfolio vector index rebuild on a background thread.
     * Exceptions are caught and logged; failure is non-fatal for the caller.
     */
    @Async
    public void rebuildIndexAsync() {
        try {
            rebuildIndex();
        } catch (Exception ex) {
            LOGGER.warn("Async portfolio vector reindex failed: {}", ex.getMessage());
        }
    }

    /**
     * Fires a tracked portfolio vector index rebuild on a background thread.
     * Progress and outcome are written back to {@code job} as the rebuild proceeds.
     */
    @Async
    public void rebuildIndexAsync(ReindexJob job) {
        try {
            rebuildIndexTracked(job);
            job.succeed();
        } catch (Exception ex) {
            LOGGER.warn("Tracked async portfolio vector reindex failed: {}", ex.getMessage());
            job.fail(ex.getMessage());
        }
    }

    /**
     * Removes all portfolio chunks from the store and re-adds them from current Mongo data.
     * Also evicts the portfolio summary cache so the next chat request picks up fresh data.
     */
    @CacheEvict(value = "portfolioSummary", allEntries = true)
    public void rebuildIndex() {
        rebuildIndexTracked(null);
    }

    /**
     * Core rebuild logic. If {@code job} is non-null, progress is written back to it as embedding proceeds.
     *
     * @param job the ReindexJob to track progress, or null for untracked rebuild
     */
    private void rebuildIndexTracked(ReindexJob job) {
        try {
            var filter = new FilterExpressionBuilder()
                    .eq(PortfolioVectorMetadataKeys.NAMESPACE, PortfolioVectorMetadataKeys.NAMESPACE_PORTFOLIO)
                    .build();
            vectorStore.delete(filter);
        } catch (Exception ex) {
            LOGGER.debug("Vector store delete (clear namespace) skipped or failed: {}", ex.getMessage());
        }
        List<Document> chunks = chunkDocumentService.buildAllChunkDocuments();
        if (chunks.isEmpty()) {
            LOGGER.warn("Portfolio vector index rebuild: no documents produced (empty portfolio data?)");
            return;
        }
        int total = chunks.size();
        if (job != null) {
            job.setTotalChunks(total);
        }
        LOGGER.info("Portfolio vector index rebuild: embedding {} chunk(s) in batches of {}", total, indexBatchSize);
        try {
            for (int i = 0; i < total; i += indexBatchSize) {
                List<Document> batch = chunks.subList(i, Math.min(i + indexBatchSize, total));
                vectorStore.add(batch);
                if (job != null) {
                    job.addChunksEmbedded(batch.size());
                }
                int added = Math.min(i + indexBatchSize, total);
                LOGGER.debug("Portfolio vector index: added {}/{} chunk(s)", added, total);
                if (added < total && indexBatchDelayMs > 0) {
                    Thread.sleep(indexBatchDelayMs);
                }
            }
            LOGGER.info("Portfolio vector index rebuilt with {} chunk(s)", total);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            LOGGER.warn("Portfolio vector index rebuild interrupted");
        } catch (Exception ex) {
            LOGGER.error("Portfolio vector index rebuild failed during add", ex);
            throw ex;
        }
    }

}
