package com.kapil.personalwebsite.ai.vector;

import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.AccessLevel;
import lombok.Getter;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tracks the status and progress of a single portfolio vector index rebuild job.
 * Fields are volatile so the HTTP status endpoint always reads the latest written state.
 *
 * @author Kapil Garg
 */
@Getter
public class ReindexJob {

    private final String jobId;
    private final Instant startedAt;

    @Getter(AccessLevel.NONE)
    private final AtomicInteger chunksEmbedded = new AtomicInteger(0);

    private volatile Status status = Status.RUNNING;
    private volatile int totalChunks = 0;
    private volatile Instant completedAt;
    private volatile String errorMessage;

    ReindexJob(String jobId) {
        this.jobId = jobId;
        this.startedAt = Instant.now();
    }

    void setTotalChunks(int total) {
        this.totalChunks = total;
    }

    void addChunksEmbedded(int count) {
        chunksEmbedded.addAndGet(count);
    }

    /**
     * Progress field for the reindex status JSON response invoked by the frontend polling endpoint.
     */
    @JsonGetter("chunksEmbedded")
    public int getChunksEmbedded() {
        return chunksEmbedded.get();
    }

    void succeed() {
        this.status = Status.SUCCEEDED;
        this.completedAt = Instant.now();
    }

    void fail(String message) {
        this.status = Status.FAILED;
        this.errorMessage = message;
        this.completedAt = Instant.now();
    }

    public enum Status {RUNNING, SUCCEEDED, FAILED}

}
