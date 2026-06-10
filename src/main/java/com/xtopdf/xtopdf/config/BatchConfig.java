package com.xtopdf.xtopdf.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for batch file conversion processing.
 */
@Configuration
public class BatchConfig {

    @Value("${xtopdf.batch.max-size:10}")
    private int maxBatchSize;

    @Value("${xtopdf.batch.parallel-workers:4}")
    private int parallelWorkers;

    @Value("${xtopdf.batch.timeout-per-file-seconds:300}")
    private int timeoutPerFileSeconds;

    public int getMaxBatchSize() {
        return maxBatchSize;
    }

    public int getParallelWorkers() {
        return parallelWorkers;
    }

    public int getTimeoutPerFileSeconds() {
        return timeoutPerFileSeconds;
    }
}
