package com.xtopdf.xtopdf.controllers;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test verifying that the OpenAPI spec is generated and accessible.
 *
 * **Validates: Requirements 25.2, 25.3, 25.4**
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "xtopdf.rate-limit.enabled=false",
        "container.orchestration.enabled=false"
})
@Tag("Feature: advanced-improvements, Property 53: OpenAPI Spec Completeness")
class OpenApiSpecTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void openApiSpecIsGeneratedAtExpectedPath() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.info").exists())
                .andExpect(jsonPath("$.paths").exists());
    }

    @Test
    void openApiSpecContainsConversionEndpoints() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/convert']").exists())
                .andExpect(jsonPath("$.paths['/api/convert/batch']").exists());
    }

    @Test
    void openApiSpecContainsPdfOperationsEndpoints() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/pdf/merge']").exists())
                .andExpect(jsonPath("$.paths['/api/pdf/add-page-numbers']").exists())
                .andExpect(jsonPath("$.paths['/api/pdf/add-watermark']").exists());
    }

    @Test
    void openApiSpecContainsTags() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags").isArray());
    }
}
