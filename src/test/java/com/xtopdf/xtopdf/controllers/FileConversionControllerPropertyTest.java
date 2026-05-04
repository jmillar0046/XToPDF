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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Property-based tests for FileConversionController error message propagation.
 *
 * Property 5: Controller Error Message Propagation — For any FileConversionException
 * with an arbitrary message string, the FileConversionController SHALL return an HTTP 400
 * response whose body contains that exact message string.
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
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    /**
     * Property 5: Controller Error Message Propagation — For any FileConversionException
     * with an arbitrary message string, the FileConversionController SHALL return an HTTP 400
     * response whose body contains that exact message string.
     *
     * **Validates: Requirements 3.1, 3.2**
     */
    @Property(tries = 100)
    @Tag("Feature: repo-efficiency-improvements, Property 5: Controller Error Message Propagation")
    void controllerReturnsExactExceptionMessageInResponse(
            @ForAll("errorMessages") String errorMessage) throws Exception {

        doThrow(new FileConversionException(errorMessage))
                .when(fileConversionService).convertFile(any(ConversionParameters.class));

        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .param("outputFile", "test.pdf"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(errorMessage));
    }

    @Provide
    Arbitrary<String> errorMessages() {
        return Arbitraries.strings()
                .ofMinLength(1)
                .ofMaxLength(200)
                .ascii()
                .filter(s -> !s.isBlank());
    }
}
