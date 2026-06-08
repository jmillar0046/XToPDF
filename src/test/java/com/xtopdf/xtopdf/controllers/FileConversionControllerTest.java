package com.xtopdf.xtopdf.controllers;

import tools.jackson.databind.ObjectMapper;
import com.xtopdf.xtopdf.dto.ConversionParameters;
import com.xtopdf.xtopdf.dto.ConversionRequest;
import com.xtopdf.xtopdf.dto.MergeRequest;
import com.xtopdf.xtopdf.dto.PageNumberRequest;
import com.xtopdf.xtopdf.dto.WatermarkRequest;
import com.xtopdf.xtopdf.enums.PageNumberAlignment;
import com.xtopdf.xtopdf.enums.PageNumberPosition;
import com.xtopdf.xtopdf.enums.PageNumberStyle;
import com.xtopdf.xtopdf.enums.WatermarkLayer;
import com.xtopdf.xtopdf.enums.WatermarkOrientation;
import com.xtopdf.xtopdf.exceptions.FileConversionException;
import com.xtopdf.xtopdf.services.FileConversionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FileConversionController.class)
class FileConversionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileConversionService fileConversionService;

    @Autowired
    private FileConversionController fileConversionController;

    @Autowired
    private ObjectMapper objectMapper;

    // --- FileConversionException handling via GlobalExceptionHandler ---

    @Test
    void testFileConversionExceptionReturnsGenericMessage() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());
        String outputFile = "test.pdf";

        doThrow(new FileConversionException("Unsupported file format: .xyz"))
                .when(fileConversionService).convertFile(any(ConversionParameters.class));

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .param("outputFile", outputFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("CONVERSION_ERROR"))
                .andExpect(jsonPath("$.message").value("File conversion failed"))
                .andExpect(jsonPath("$.correlationId").exists());
    }

    @Test
    void testFileConversionExceptionReturnedForJsonEndpoint() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        ConversionRequest request = ConversionRequest.builder()
                .outputFile("test.pdf")
                .build();
        MockMultipartFile requestPart = new MockMultipartFile("request", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request));

        doThrow(new FileConversionException("Conversion failed: corrupt file"))
                .when(fileConversionService).convertFile(any(ConversionParameters.class));

        mockMvc.perform(multipart("/api/convert/json")
                        .file(inputFile)
                        .file(requestPart))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("CONVERSION_ERROR"))
                .andExpect(jsonPath("$.message").value("File conversion failed"))
                .andExpect(jsonPath("$.correlationId").exists());
    }

    // --- IllegalArgumentException handling via GlobalExceptionHandler ---

    @Test
    void testIllegalArgumentExceptionReturnsGenericMessage() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());
        MockMultipartFile existingPdf = new MockMultipartFile("existingPdf", "existing.pdf",
                MediaType.APPLICATION_PDF_VALUE, "existing pdf content".getBytes());
        String outputFile = "test.pdf";

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .file(existingPdf)
                        .param("outputFile", outputFile)
                        .param("position", "invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_ARGUMENT"))
                .andExpect(jsonPath("$.message").value("Invalid request parameters"))
                .andExpect(jsonPath("$.correlationId").exists());
    }

    // --- Path validation ---

    @Test
    void testDirectoryTraversalAttackPrevention() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());
        String maliciousPath = "../../../etc/passwd";

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .param("outputFile", maliciousPath))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_ARGUMENT"))
                .andExpect(jsonPath("$.message").value("Invalid request parameters"));
    }

    @Test
    void testDirectoryTraversalWithPdfExtension() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());
        String maliciousPath = "../../etc/output.pdf";

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .param("outputFile", maliciousPath))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_ARGUMENT"))
                .andExpect(jsonPath("$.message").value("Invalid request parameters"));
    }

    @Test
    void testNonPdfOutputExtension() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());
        String outputFile = "test.txt";

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .param("outputFile", outputFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_ARGUMENT"))
                .andExpect(jsonPath("$.message").value("Invalid request parameters"));
    }

    // --- Success cases (SuccessResponse JSON) ---

    @Test
    void testConvertFileAllGood() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());
        String outputFile = "test.pdf";

        doNothing().when(fileConversionService).convertFile(any(ConversionParameters.class));

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .param("outputFile", outputFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("File converted successfully"));
    }

    @Test
    void testDefaultOutputDirectoryIsUsedWhenPropertyNotSet() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());
        String outputFile = "test.pdf";

        doNothing().when(fileConversionService).convertFile(any(ConversionParameters.class));

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .param("outputFile", outputFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("File converted successfully"));
    }

    @Test
    public void testInvalidFileType() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "invalidfile.txt",
                MediaType.TEXT_PLAIN_VALUE, "invalid content".getBytes());
        String outputFile = "test.pdf";

        doThrow(new FileConversionException("Invalid file type"))
                .when(fileConversionService).convertFile(any(ConversionParameters.class));

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .param("outputFile", outputFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("CONVERSION_ERROR"))
                .andExpect(jsonPath("$.message").value("File conversion failed"));
    }

    @Test
    public void testFileConversionException() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());
        String outputFile = "test.pdf";

        doThrow(new FileConversionException("Conversion failed"))
                .when(fileConversionService).convertFile(any(ConversionParameters.class));

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .param("outputFile", outputFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("CONVERSION_ERROR"))
                .andExpect(jsonPath("$.message").value("File conversion failed"));
    }

    @Test
    public void testMissingOutputFileParameter() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE, ("").getBytes());

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testMissingInputFile() throws Exception {
        mockMvc.perform(multipart("/api/convert")
                        .param("outputFile", "test.pdf"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLargeFileUpload() throws Exception {
        byte[] largeContent = new byte[1024 * 1024 * 5]; // 5MB
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "large.docx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE, largeContent);
        String outputFile = "test.pdf";

        doNothing().when(fileConversionService).convertFile(any(ConversionParameters.class));

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .param("outputFile", outputFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("File converted successfully"));
    }

    @Test
    void testNullOutputFileParameter() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testServiceLayerGenericException() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());
        String outputFile = "test.pdf";

        doThrow(new RuntimeException("Unexpected error"))
                .when(fileConversionService).convertFile(any(ConversionParameters.class));

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .param("outputFile", outputFile))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    @Test
    void testConvertFileWithExistingPdfAtBack() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());
        MockMultipartFile existingPdf = new MockMultipartFile("existingPdf", "existing.pdf",
                MediaType.APPLICATION_PDF_VALUE, "existing pdf content".getBytes());
        String outputFile = "test.pdf";

        doNothing().when(fileConversionService).convertFile(any(ConversionParameters.class));

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .file(existingPdf)
                        .param("outputFile", outputFile)
                        .param("position", "back"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("File converted successfully"));
    }

    @Test
    void testConvertFileWithExistingPdfAtFront() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());
        MockMultipartFile existingPdf = new MockMultipartFile("existingPdf", "existing.pdf",
                MediaType.APPLICATION_PDF_VALUE, "existing pdf content".getBytes());
        String outputFile = "test.pdf";

        doNothing().when(fileConversionService).convertFile(any(ConversionParameters.class));

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .file(existingPdf)
                        .param("outputFile", outputFile)
                        .param("position", "front"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("File converted successfully"));
    }

    @Test
    void testConvertFileWithExistingPdfDefaultPosition() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());
        MockMultipartFile existingPdf = new MockMultipartFile("existingPdf", "existing.pdf",
                MediaType.APPLICATION_PDF_VALUE, "existing pdf content".getBytes());
        String outputFile = "test.pdf";

        doNothing().when(fileConversionService).convertFile(any(ConversionParameters.class));

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .file(existingPdf)
                        .param("outputFile", outputFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("File converted successfully"));
    }

    @Test
    void testConvertFileWithInvalidPosition() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());
        MockMultipartFile existingPdf = new MockMultipartFile("existingPdf", "existing.pdf",
                MediaType.APPLICATION_PDF_VALUE, "existing pdf content".getBytes());
        String outputFile = "test.pdf";

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .file(existingPdf)
                        .param("outputFile", outputFile)
                        .param("position", "invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_ARGUMENT"))
                .andExpect(jsonPath("$.message").value("Invalid request parameters"));
    }

    @Test
    void testConvertFileWithoutExistingPdf() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());
        String outputFile = "test.pdf";

        doNothing().when(fileConversionService).convertFile(any(ConversionParameters.class));

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .param("outputFile", outputFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("File converted successfully"));
    }

    @Test
    void testConvertFileWithExistingPdfMergeFailure() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());
        MockMultipartFile existingPdf = new MockMultipartFile("existingPdf", "existing.pdf",
                MediaType.APPLICATION_PDF_VALUE, "existing pdf content".getBytes());
        String outputFile = "test.pdf";

        doThrow(new FileConversionException("Merge failed"))
                .when(fileConversionService).convertFile(any(ConversionParameters.class));

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .file(existingPdf)
                        .param("outputFile", outputFile)
                        .param("position", "back"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("CONVERSION_ERROR"))
                .andExpect(jsonPath("$.message").value("File conversion failed"));
    }

    @Test
    void testConvertFileWithPageNumbersEnabled() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.txt",
                MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());
        String outputFile = "test.pdf";

        doNothing().when(fileConversionService).convertFile(any(ConversionParameters.class));

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .param("outputFile", outputFile)
                        .param("addPageNumbers", "true")
                        .param("pageNumberPosition", "BOTTOM")
                        .param("pageNumberAlignment", "CENTER")
                        .param("pageNumberStyle", "ARABIC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("File converted successfully"));
    }

    @Test
    void testConvertFileWithWatermark_Success() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());
        String outputFile = "test.pdf";

        doNothing().when(fileConversionService).convertFile(any(ConversionParameters.class));

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .param("outputFile", outputFile)
                        .param("addWatermark", "true")
                        .param("watermarkText", "CONFIDENTIAL")
                        .param("watermarkFontSize", "48")
                        .param("watermarkLayer", "FOREGROUND")
                        .param("watermarkOrientation", "DIAGONAL_UP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("File converted successfully"));
    }

    // --- JSON endpoint tests ---

    @Test
    void testJsonEndpointConvertFileSuccess() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        ConversionRequest request = ConversionRequest.builder()
                .outputFile("test.pdf")
                .build();
        MockMultipartFile requestPart = new MockMultipartFile("request", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request));

        doNothing().when(fileConversionService).convertFile(any(ConversionParameters.class));

        mockMvc.perform(multipart("/api/convert/json")
                        .file(inputFile)
                        .file(requestPart))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("File converted successfully"));
    }

    @Test
    void testJsonEndpointPathTraversalPrevention() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        ConversionRequest request = ConversionRequest.builder()
                .outputFile("../../../etc/passwd.pdf")
                .build();
        MockMultipartFile requestPart = new MockMultipartFile("request", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart("/api/convert/json")
                        .file(inputFile)
                        .file(requestPart))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_ARGUMENT"))
                .andExpect(jsonPath("$.message").value("Invalid request parameters"));
    }

    @Test
    void testJsonEndpointNonPdfOutputPath() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        ConversionRequest request = ConversionRequest.builder()
                .outputFile("output.txt")
                .build();
        MockMultipartFile requestPart = new MockMultipartFile("request", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart("/api/convert/json")
                        .file(inputFile)
                        .file(requestPart))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_ARGUMENT"))
                .andExpect(jsonPath("$.message").value("Invalid request parameters"));
    }
}
