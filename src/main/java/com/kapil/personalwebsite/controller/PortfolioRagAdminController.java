package com.kapil.personalwebsite.controller;

import com.kapil.personalwebsite.ai.vector.PortfolioVectorIndexService;
import com.kapil.personalwebsite.ai.vector.ReindexJob;
import com.kapil.personalwebsite.ai.vector.ReindexJobService;
import com.kapil.personalwebsite.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Admin endpoint to rebuild the portfolio vector index after content changes.
 * Returns 202 Accepted immediately with a jobId; use the status endpoint to poll completion.
 *
 * @author Kapil Garg
 */
@RestController
@RequestMapping("/admin/ai")
@RequiredArgsConstructor
@ConditionalOnBean(PortfolioVectorIndexService.class)
public class PortfolioRagAdminController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PortfolioRagAdminController.class);

    private final PortfolioVectorIndexService portfolioVectorIndexService;
    private final ReindexJobService reindexJobService;

    /**
     * Starts an async portfolio vector index rebuild and returns a jobId immediately.
     *
     * @return 202 Accepted with the jobId to poll status
     */
    @PostMapping("/reindex-portfolio")
    public ResponseEntity<ApiResponse<String>> reindexPortfolio() {
        ReindexJob job = reindexJobService.createJob();
        LOGGER.info("POST /admin/ai/reindex-portfolio - starting async rebuild, jobId={}", job.getJobId());
        portfolioVectorIndexService.rebuildIndexAsync(job);
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success(job.getJobId(), "Portfolio reindex started. Poll /status/" + job.getJobId() + " for progress."));
    }

    /**
     * Returns the current status and progress of a reindex job.
     *
     * @param jobId the job ID returned by the reindex endpoint
     * @return job status, or 404 if the jobId is unknown
     */
    @GetMapping("/reindex-portfolio/status/{jobId}")
    public ResponseEntity<ApiResponse<ReindexJob>> getReindexStatus(@PathVariable String jobId) {
        return reindexJobService.getJob(jobId)
                .map(job -> ResponseEntity.ok(ApiResponse.success(job, "Job status retrieved")))
                .orElseGet(() -> ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Job not found: " + jobId, HttpStatus.NOT_FOUND.value())));
    }

}
