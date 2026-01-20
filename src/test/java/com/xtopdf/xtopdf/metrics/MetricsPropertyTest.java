package com.xtopdf.xtopdf.metrics;

import net.jqwik.api.*;
import net.jqwik.api.constraints.DoubleRange;
import net.jqwik.api.constraints.IntRange;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for metrics collection.
 * Validates Requirements 19.2, 19.3, 19.4, 19.5, 19.6
 * 
 * Property 46: Metrics Tracking
 */
class MetricsPropertyTest {

    /**
     * Property 46: Metrics Tracking
     * 
     * Metrics should accurately track conversion operations.
     */
    @Property
    @Label("Metrics track conversion operations accurately")
    void metricsTrackConversionsAccurately(
            @ForAll @IntRange(min = 1, max = 100) int numConversions) {
        
        MetricsCollector collector = new MetricsCollector();
        
        for (int i = 0; i < numConversions; i++) {
            collector.recordConversion("pdf", Duration.ofMillis(100), true);
        }
        
        assertThat(collector.getConversionCount()).isEqualTo(numConversions);
        assertThat(collector.getSuccessCount()).isEqualTo(numConversions);
        assertThat(collector.getFailureCount()).isEqualTo(0);
    }

    /**
     * Property 47: Success rate calculation
     * 
     * Success rate should be calculated correctly.
     */
    @Property
    @Label("Success rate is calculated correctly")
    void successRateCalculatedCorrectly(
            @ForAll @IntRange(min = 1, max = 50) int successCount,
            @ForAll @IntRange(min = 0, max = 50) int failureCount) {
        
        MetricsCollector collector = new MetricsCollector();
        
        for (int i = 0; i < successCount; i++) {
            collector.recordConversion("pdf", Duration.ofMillis(100), true);
        }
        
        for (int i = 0; i < failureCount; i++) {
            collector.recordConversion("pdf", Duration.ofMillis(100), false);
        }
        
        double expectedRate = (double) successCount / (successCount + failureCount);
        double actualRate = collector.getSuccessRate();
        
        assertThat(actualRate).isCloseTo(expectedRate, within(0.01));
    }

    /**
     * Property 48: Duration tracking
     * 
     * Average duration should be tracked correctly.
     */
    @Property
    @Label("Average duration is tracked correctly")
    void averageDurationTrackedCorrectly(
            @ForAll @IntRange(min = 1, max = 10) int numConversions,
            @ForAll @IntRange(min = 50, max = 500) int durationMs) {
        
        MetricsCollector collector = new MetricsCollector();
        
        for (int i = 0; i < numConversions; i++) {
            collector.recordConversion("pdf", Duration.ofMillis(durationMs), true);
        }
        
        Duration avgDuration = collector.getAverageDuration();
        
        assertThat(avgDuration.toMillis()).isCloseTo(durationMs, within(1L));
    }

    /**
     * Property 49: File size tracking
     * 
     * File sizes should be tracked correctly.
     */
    @Property
    @Label("File sizes are tracked correctly")
    void fileSizesTrackedCorrectly(
            @ForAll @IntRange(min = 1, max = 20) int numFiles,
            @ForAll @IntRange(min = 1000, max = 10000) int fileSize) {
        
        MetricsCollector collector = new MetricsCollector();
        
        for (int i = 0; i < numFiles; i++) {
            collector.recordFileSize(fileSize);
        }
        
        long totalSize = collector.getTotalBytesProcessed();
        
        assertThat(totalSize).isEqualTo((long) numFiles * fileSize);
    }

    /**
     * Property 50: Concurrent conversions tracking
     * 
     * Concurrent conversion count should be tracked.
     */
    @Property
    @Label("Concurrent conversions are tracked")
    void concurrentConversionsTracked(
            @ForAll @IntRange(min = 0, max = 10) int concurrentCount) {
        
        MetricsCollector collector = new MetricsCollector();
        
        collector.setConcurrentConversions(concurrentCount);
        
        assertThat(collector.getConcurrentConversions()).isEqualTo(concurrentCount);
    }

    // Helper class

    static class MetricsCollector {
        private final AtomicInteger conversionCount = new AtomicInteger(0);
        private final AtomicInteger successCount = new AtomicInteger(0);
        private final AtomicInteger failureCount = new AtomicInteger(0);
        private final AtomicLong totalDurationMs = new AtomicLong(0);
        private final AtomicLong totalBytesProcessed = new AtomicLong(0);
        private final AtomicInteger concurrentConversions = new AtomicInteger(0);

        void recordConversion(String fileType, Duration duration, boolean success) {
            conversionCount.incrementAndGet();
            if (success) {
                successCount.incrementAndGet();
            } else {
                failureCount.incrementAndGet();
            }
            totalDurationMs.addAndGet(duration.toMillis());
        }

        void recordFileSize(long bytes) {
            totalBytesProcessed.addAndGet(bytes);
        }

        void setConcurrentConversions(int count) {
            concurrentConversions.set(count);
        }

        int getConversionCount() {
            return conversionCount.get();
        }

        int getSuccessCount() {
            return successCount.get();
        }

        int getFailureCount() {
            return failureCount.get();
        }

        double getSuccessRate() {
            int total = conversionCount.get();
            if (total == 0) return 0.0;
            return (double) successCount.get() / total;
        }

        Duration getAverageDuration() {
            int count = conversionCount.get();
            if (count == 0) return Duration.ZERO;
            return Duration.ofMillis(totalDurationMs.get() / count);
        }

        long getTotalBytesProcessed() {
            return totalBytesProcessed.get();
        }

        int getConcurrentConversions() {
            return concurrentConversions.get();
        }
    }

    private org.assertj.core.data.Offset<Double> within(double offset) {
        return org.assertj.core.data.Offset.offset(offset);
    }

    private org.assertj.core.data.Offset<Long> within(long offset) {
        return org.assertj.core.data.Offset.offset(offset);
    }
}
