package com.kapil.personalwebsite.ai.vector;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * In-memory store for portfolio reindex job status without persistence.
 * Allows the background thread to update the job status and the HTTP endpoint to read it without needing a database.
 *
 * @author Kapil Garg
 */
@Service
@ConditionalOnBean(PortfolioVectorIndexService.class)
public class ReindexJobService {

    private static final int MAX_JOBS = 10;

    private final Map<String, ReindexJob> jobs = new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, ReindexJob> eldest) {
            return size() > MAX_JOBS;
        }
    };

    /**
     * Creates a new job record and returns it. The background thread will update the same object.
     */
    public synchronized ReindexJob createJob() {
        String jobId = UUID.randomUUID().toString();
        ReindexJob job = new ReindexJob(jobId);
        jobs.put(jobId, job);
        return job;
    }

    public synchronized Optional<ReindexJob> getJob(String jobId) {
        return Optional.ofNullable(jobs.get(jobId));
    }

}
