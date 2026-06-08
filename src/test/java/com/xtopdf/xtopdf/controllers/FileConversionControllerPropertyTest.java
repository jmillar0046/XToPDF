package com.xtopdf.xtopdf.controllers;

import com.xtopdf.xtopdf.dto.ConversionParameters;
import com.xtopdf.xtopdf.exceptions.FileConversionException;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Property-based tests for FileConversionController error handling.
 *
 * Property 5: Controller Error Handling — For any FileConversionException regardless
 * of its internal message, the FileConversionController SHALL return an HTTP 400
 * response with a safe generic message that does not expose internal details.
 *
 * **Validates: Requirements 3.1, 3.2**
 */
class FileConversionControllerPropertyTest {

    private MockMvc mockMvc;
    private FileConversionService fileConversionService;

    @BeforeProperty
    void setup() {
        fileConversionService = mock(FileConversionService.class);
        ObjectMapper objectMapper = new ObjectMapper();
        FileConversionController controller = new FileConversionController(
                fileConversionService, objectMapper);
        ReflectionTestUtils.setField(controller, "baseOutputDirectory", "/safe/output/directory");
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    /**
     * Property 5: For any FileConversionException with any internal message,
     * the controller SHALL return HTTP 400 with a safe generic message
     * that does not leak the internal exception details.
     *
     * **Validates: Requirements 3.1, 3.2**
     */
    @Property(tries = 25)
    @Tag("Feature: repo-efficiency-improvements, Property 5: Controller Error Handling")
    void controllerReturnsGenericErrorMessageForAnyException(
            @ForAll("errorMessages") String errorMessage) throws Exception {

        doThrow(new FileConversionException(errorMessage))
                .when(fileConversionService).convertFile(any(ConversionParameters.class));

        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        String responseBody = mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .param("outputFile", "test.pdf"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("CONVERSION_ERROR"))
                .andExpect(jsonPath("$.message").value("File conversion failed"))
                .andExpect(jsonPath("$.correlationId").exists())
                .andReturn().getResponse().getContentAsString();

        // Verify the internal error message is NOT exposed to the user
        org.assertj.core.api.Assertions.assertThat(responseBody)
                .doesNotContain(errorMessage);
    }

    @Provide
    Arbitrary<String> errorMessages() {
        // Generate messages that could contain sensitive info (paths, class names, etc.)
        return Arbitraries.oneOf(
                Arbitraries.of(
                        "/usr/local/app/data/secret.pdf",
                        "java.lang.NullPointerException at com.xtopdf.internal.Service",
                        "Connection refused: localhost:5432",
                        "Access denied for user 'admin'@'192.168.1.1'",
                        "File not found: /etc/shadow"
                ),
                Arbitraries.strings()
                        .ofMinLength(1)
                        .ofMaxLength(200)
                        .ascii()
                        .filter(s -> !s.isBlank() && !s.equals("File conversion failed"))
        );
    }
}
