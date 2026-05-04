package com.xtopdf.xtopdf.controllers;

import com.xtopdf.xtopdf.dto.ConversionParameters;
import com.xtopdf.xtopdf.services.FileConversionService;
import net.jqwik.api.*;
import net.jqwik.api.lifecycle.BeforeProperty;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Property-based tests for output path security validation.
 *
 * Property 6: Output Path Security Validation — For any output path string that either
 * does not start with the configured base directory after normalization or does not end
 * with ".pdf", the FileConversionController SHALL return an HTTP 400 response with the
 * message "Invalid output file path".
 *
 * **Validates: Requirements 4.3, 4.4**
 */
class OutputPathValidationPropertyTest {

    private MockMvc mockMvc;
    private FileConversionService fileConversionService;

    @BeforeProperty
    void setup() {
        fileConversionService = mock(FileConversionService.class);
        ObjectMapper objectMapper = new ObjectMapper();
        FileConversionController controller = new FileConversionController(
                fileConversionService, objectMapper);
        ReflectionTestUtils.setField(controller, "baseOutputDirectory", "/safe/output/directory");
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    /**
     * Property 6 (path traversal): For any output path containing path traversal sequences,
     * the controller SHALL return HTTP 400 with "Invalid output file path".
     *
     * **Validates: Requirements 4.3**
     */
    @Property(tries = 100)
    @Tag("Feature: repo-efficiency-improvements, Property 6: Output Path Security Validation")
    void pathTraversalAttemptsAreRejected(
            @ForAll("pathTraversalStrings") String maliciousPath) throws Exception {

        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .param("outputFile", maliciousPath))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid output file path"));
    }

    /**
     * Property 6 (non-.pdf extension): For any output path that does not end with ".pdf",
     * the controller SHALL return HTTP 400 with "Invalid output file path".
     *
     * **Validates: Requirements 4.4**
     */
    @Property(tries = 100)
    @Tag("Feature: repo-efficiency-improvements, Property 6: Output Path Security Validation")
    void nonPdfExtensionsAreRejected(
            @ForAll("nonPdfExtensions") String outputPath) throws Exception {

        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .param("outputFile", outputPath))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid output file path"));
    }

    @Provide
    Arbitrary<String> pathTraversalStrings() {
        // Generate paths that attempt directory traversal using forward slashes
        Arbitrary<String> traversalPrefixes = Arbitraries.of(
                "../", "../../", "../../../", "../../../../",
                "../../../..", "../../../../../../");
        Arbitrary<String> filenames = Arbitraries.of(
                "etc/passwd.pdf", "output.pdf", "tmp/evil.pdf",
                "windows/system32/config.pdf", "home/user/file.pdf");

        return Combinators.combine(traversalPrefixes, filenames)
                .as((prefix, filename) -> prefix + filename);
    }

    @Provide
    Arbitrary<String> nonPdfExtensions() {
        // Generate filenames with non-.pdf extensions
        Arbitrary<String> basenames = Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(20);
        Arbitrary<String> extensions = Arbitraries.of(
                ".txt", ".doc", ".docx", ".xlsx", ".csv", ".html",
                ".xml", ".json", ".exe", ".sh", ".bat", ".png", ".jpg");

        return Combinators.combine(basenames, extensions)
                .as((name, ext) -> name + ext);
    }
}
