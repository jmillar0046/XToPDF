package com.xtopdf.xtopdf.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test verifying metrics configuration and Prometheus endpoint.
 * Validates: Requirements 19.1, 19.7
 */
@SpringBootTest
@AutoConfigureMockMvc
class MetricsConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private MetricsConfiguration.ConversionMetrics conversionMetrics;

    @Test
    @DisplayName("MetricsConfiguration bean is loaded in context")
    void metricsConfigurationIsLoaded() {
        assertThat(applicationContext.getBean(MetricsConfiguration.class)).isNotNull();
    }

    @Test
    @DisplayName("ConversionMetrics bean is available")
    void conversionMetricsBeanIsAvailable() {
        assertThat(conversionMetrics).isNotNull();
        assertThat(conversionMetrics.getRegistry()).isNotNull();
    }

    @Test
    @DisplayName("MeterRegistry is configured and available")
    void meterRegistryIsConfigured() {
        assertThat(meterRegistry).isNotNull();
    }

    @Test
    @DisplayName("Prometheus endpoint returns metrics at /actuator/prometheus")
    void prometheusEndpointReturnsMetrics() throws Exception {
        // Record some metrics so there's data to export
        conversionMetrics.incrementRequestCount("txt");
        conversionMetrics.recordFileSize(1024);

        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("conversion_requests_total")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("conversion_file_size_bytes")));
    }

    @Test
    @DisplayName("Prometheus endpoint includes conversion duration metrics after recording")
    void prometheusEndpointIncludesDurationMetrics() throws Exception {
        // Record a timer to ensure it appears in the output
        var sample = conversionMetrics.startTimer();
        conversionMetrics.stopTimer(sample, "pdf");

        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("conversion_duration_seconds")));
    }

    @Test
    @DisplayName("Prometheus endpoint includes error metrics after recording")
    void prometheusEndpointIncludesErrorMetrics() throws Exception {
        conversionMetrics.incrementErrorCount("docx", "timeout");

        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("conversion_errors_total")));
    }

    @Test
    @DisplayName("ConversionMetrics records request count correctly")
    void conversionMetricsRecordsRequestCount() {
        double before = meterRegistry.find("conversion.requests.total")
                .tag("format", "html")
                .counter() != null ?
                meterRegistry.find("conversion.requests.total")
                        .tag("format", "html")
                        .counter().count() : 0.0;

        conversionMetrics.incrementRequestCount("html");

        double after = meterRegistry.find("conversion.requests.total")
                .tag("format", "html")
                .counter().count();

        assertThat(after).isEqualTo(before + 1.0);
    }
}
