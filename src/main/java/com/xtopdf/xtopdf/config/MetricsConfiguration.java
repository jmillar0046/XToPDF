package com.xtopdf.xtopdf.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for custom application metrics using Micrometer.
 * Registers custom meters for conversion operations:
 * - conversion.requests.total: Counter by format
 * - conversion.duration.seconds: Timer by format
 * - conversion.errors.total: Counter by format and error type
 * - conversion.file.size.bytes: Distribution summary for file sizes
 */
@Configuration
public class MetricsConfiguration {

    /**
     * Timer for tracking conversion duration by format.
     * Tagged with "format" to allow per-format breakdown.
     */
    @Bean
    public Timer conversionDurationTimer(MeterRegistry registry) {
        return Timer.builder("conversion.duration.seconds")
                .description("Time taken to convert files to PDF")
                .tag("format", "unknown")
                .publishPercentileHistogram()
                .register(registry);
    }

    /**
     * Distribution summary for tracking file sizes processed.
     */
    @Bean
    public DistributionSummary conversionFileSizeSummary(MeterRegistry registry) {
        return DistributionSummary.builder("conversion.file.size.bytes")
                .description("Size of files processed for conversion")
                .publishPercentileHistogram()
                .register(registry);
    }

    /**
     * Provides helper methods for recording metrics dynamically with tags.
     */
    @Bean
    public ConversionMetrics conversionMetrics(MeterRegistry registry) {
        return new ConversionMetrics(registry);
    }

    /**
     * Helper class providing methods to record conversion metrics with dynamic tags.
     */
    public static class ConversionMetrics {
        private final MeterRegistry registry;

        public ConversionMetrics(MeterRegistry registry) {
            this.registry = registry;
        }

        public MeterRegistry getRegistry() {
            return registry;
        }

        /**
         * Increments the conversion request counter for the given format.
         */
        public void incrementRequestCount(String format) {
            Counter.builder("conversion.requests.total")
                    .description("Total number of conversion requests")
                    .tag("format", format)
                    .register(registry)
                    .increment();
        }

        /**
         * Increments the conversion error counter for the given format and error type.
         */
        public void incrementErrorCount(String format, String errorType) {
            Counter.builder("conversion.errors.total")
                    .description("Total number of conversion errors")
                    .tag("format", format)
                    .tag("error_type", errorType)
                    .register(registry)
                    .increment();
        }

        /**
         * Records the duration of a conversion for the given format.
         */
        public Timer.Sample startTimer() {
            return Timer.start(registry);
        }

        /**
         * Stops the timer and records the duration for the given format.
         */
        public void stopTimer(Timer.Sample sample, String format) {
            sample.stop(Timer.builder("conversion.duration.seconds")
                    .description("Time taken to convert files to PDF")
                    .tag("format", format)
                    .publishPercentileHistogram()
                    .register(registry));
        }

        /**
         * Records the file size for the distribution summary.
         */
        public void recordFileSize(long bytes) {
            DistributionSummary.builder("conversion.file.size.bytes")
                    .description("Size of files processed for conversion")
                    .publishPercentileHistogram()
                    .register(registry)
                    .record(bytes);
        }
    }
}
