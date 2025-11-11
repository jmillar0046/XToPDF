package com.xtopdf.xtopdf.controllers;

import com.xtopdf.xtopdf.exceptions.FileConversionException;
import com.xtopdf.xtopdf.services.FileConversionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FileConversionController.class)
class FileConversionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileConversionService fileConversionService;

    @Autowired
    private FileConversionController fileConversionController;

    @Test
    void testConvertFileAllGood() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());
        String outputFile = "test.pdf";

        // Mock service method for successful conversion
        doNothing().when(fileConversionService).convertFile(any(), any(), any(), any(), any());

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .param("outputFile", outputFile))
                .andExpect(status().isOk())
                .andExpect(content().string("File converted successfully"));
    }

    @Test
    public void testInvalidFileType() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "invalidfile.txt", MediaType.TEXT_PLAIN_VALUE, "invalid content".getBytes());
        String outputFile = "test.pdf";

        // Mock service to handle invalid file type
        doThrow(new FileConversionException("Invalid file type")).when(fileConversionService).convertFile(any(), any(), any(), any(), any(), any(Boolean.class));

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .param("outputFile", outputFile))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error with conversion"));
    }

    @Test
    public void testFileConversionException() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());
        String outputFile = "test.pdf";

        // Mock service to throw FileConversionException
        doThrow(new FileConversionException("Conversion failed")).when(fileConversionService).convertFile(any(), any(), any(), any(), any(), any(Boolean.class));

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .param("outputFile", outputFile))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error with conversion"));
    }

    @Test
    public void testMissingOutputFileParameter() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx", MediaType.APPLICATION_OCTET_STREAM_VALUE, ("").getBytes());

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                    .param("outputFile", ""))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid output file path"));
    }

    @Test
    void testDirectoryTraversalAttackPrevention() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());
        String outputFile = "../../malicious.pdf";

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .param("outputFile", outputFile))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid output file path"));
    }

    @Test
    void testNonPdfOutputExtension() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());
        String outputFile = "output.txt";

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .param("outputFile", outputFile))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid output file path"));
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
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "large.docx", MediaType.APPLICATION_OCTET_STREAM_VALUE, largeContent);
        String outputFile = "large.pdf";

        doNothing().when(fileConversionService).convertFile(any(), any(), any(), any(), any());

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .param("outputFile", outputFile))
                .andExpect(status().isOk())
                .andExpect(content().string("File converted successfully"));
    }

    @Test
    void testNullOutputFileParameter() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testServiceLayerGenericException() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());
        String outputFile = "test.pdf";

        doThrow(new RuntimeException("Unexpected error")).when(fileConversionService).convertFile(any(), any(), any(), any(), any(), any(Boolean.class));

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .param("outputFile", outputFile))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testConvertFileWithExistingPdfAtBack() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());
        MockMultipartFile existingPdf = new MockMultipartFile("existingPdf", "existing.pdf", MediaType.APPLICATION_PDF_VALUE, "existing pdf content".getBytes());
        String outputFile = "test.pdf";

        doNothing().when(fileConversionService).convertFile(any(), any(), any(), any(), any());

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .file(existingPdf)
                        .param("outputFile", outputFile)
                        .param("position", "back"))
                .andExpect(status().isOk())
                .andExpect(content().string("File converted successfully"));
    }

    @Test
    void testConvertFileWithExistingPdfAtFront() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());
        MockMultipartFile existingPdf = new MockMultipartFile("existingPdf", "existing.pdf", MediaType.APPLICATION_PDF_VALUE, "existing pdf content".getBytes());
        String outputFile = "test.pdf";

        doNothing().when(fileConversionService).convertFile(any(), any(), any(), any(), any());

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .file(existingPdf)
                        .param("outputFile", outputFile)
                        .param("position", "front"))
                .andExpect(status().isOk())
                .andExpect(content().string("File converted successfully"));
    }

    @Test
    void testConvertFileWithExistingPdfDefaultPosition() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());
        MockMultipartFile existingPdf = new MockMultipartFile("existingPdf", "existing.pdf", MediaType.APPLICATION_PDF_VALUE, "existing pdf content".getBytes());
        String outputFile = "test.pdf";

        doNothing().when(fileConversionService).convertFile(any(), any(), any(), any(), any());

        // Test without position parameter - should default to "back"
        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .file(existingPdf)
                        .param("outputFile", outputFile))
                .andExpect(status().isOk())
                .andExpect(content().string("File converted successfully"));
    }

    @Test
    void testConvertFileWithInvalidPosition() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());
        MockMultipartFile existingPdf = new MockMultipartFile("existingPdf", "existing.pdf", MediaType.APPLICATION_PDF_VALUE, "existing pdf content".getBytes());
        String outputFile = "test.pdf";

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .file(existingPdf)
                        .param("outputFile", outputFile)
                        .param("position", "middle"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid position. Must be 'front' or 'back'"));
    }

    @Test
    void testConvertFileWithoutExistingPdf() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());
        String outputFile = "test.pdf";

        doNothing().when(fileConversionService).convertFile(any(), any(), any(), any(), any());

        // Test without existingPdf parameter - should work as before
        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .param("outputFile", outputFile))
                .andExpect(status().isOk())
                .andExpect(content().string("File converted successfully"));
    }

    @Test
    void testConvertFileWithExistingPdfMergeFailure() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());
        MockMultipartFile existingPdf = new MockMultipartFile("existingPdf", "existing.pdf", MediaType.APPLICATION_PDF_VALUE, "existing pdf content".getBytes());
        String outputFile = "test.pdf";

        doThrow(new FileConversionException("Conversion failed")).when(fileConversionService).convertFile(any(), any(), any(), any(), any(), any(Boolean.class));

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .file(existingPdf)
                        .param("outputFile", outputFile)
                        .param("position", "back"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error with conversion"));
    }

    @Test
    void testConvertMarkdownFile() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.md", MediaType.TEXT_MARKDOWN_VALUE, "# Test Markdown".getBytes());
        String outputFile = "test.pdf";

        doNothing().when(fileConversionService).convertFile(any(), any(), any(), any(), any());

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .param("outputFile", outputFile))
                .andExpect(status().isOk())
                .andExpect(content().string("File converted successfully"));
    }

    @Test
    void testConvertMarkdownExtensionFile() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.markdown", MediaType.TEXT_MARKDOWN_VALUE, "# Test Markdown".getBytes());
        String outputFile = "test.pdf";

        doNothing().when(fileConversionService).convertFile(any(), any(), any(), any(), any());

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .param("outputFile", outputFile))
                .andExpect(status().isOk())
                .andExpect(content().string("File converted successfully"));
    }

    @Test
    void testConvertMarkdownFileWithExistingPdf() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.md", MediaType.TEXT_MARKDOWN_VALUE, "# Test Markdown".getBytes());
        MockMultipartFile existingPdf = new MockMultipartFile("existingPdf", "existing.pdf", MediaType.APPLICATION_PDF_VALUE, "existing pdf content".getBytes());
        String outputFile = "test.pdf";

        doNothing().when(fileConversionService).convertFile(any(), any(), any(), any(), any());

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .file(existingPdf)
                        .param("outputFile", outputFile)
                        .param("position", "front"))
                .andExpect(status().isOk())
                .andExpect(content().string("File converted successfully"));
    }

    @Test
    void testConvertMarkdownFileFailed() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.md", MediaType.TEXT_MARKDOWN_VALUE, "# Test".getBytes());
        String outputFile = "test.pdf";

        doThrow(new FileConversionException("Conversion failed")).when(fileConversionService).convertFile(any(), any(), any(), any(), any(), any(Boolean.class));

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .param("outputFile", outputFile))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error with conversion"));
    }

    @Test
    void testConvertFileWithPageNumbersEnabled() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.txt", MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());
        String outputFile = "test.pdf";

        doNothing().when(fileConversionService).convertFile(any(), any(), any(), any(), any());

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .param("outputFile", outputFile)
                        .param("addPageNumbers", "true")
                        .param("pageNumberPosition", "BOTTOM")
                        .param("pageNumberAlignment", "CENTER")
                        .param("pageNumberStyle", "ARABIC"))
                .andExpect(status().isOk())
                .andExpect(content().string("File converted successfully"));
    }

    @Test
    void testConvertFileWithPageNumbersTopPosition() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.txt", MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());
        String outputFile = "test.pdf";

        doNothing().when(fileConversionService).convertFile(any(), any(), any(), any(), any());

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .param("outputFile", outputFile)
                        .param("addPageNumbers", "true")
                        .param("pageNumberPosition", "TOP")
                        .param("pageNumberAlignment", "RIGHT")
                        .param("pageNumberStyle", "ROMAN_UPPER"))
                .andExpect(status().isOk())
                .andExpect(content().string("File converted successfully"));
    }

    @Test
    void testConvertFileWithPageNumbersAlphabetic() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx", MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());
        String outputFile = "test.pdf";

        doNothing().when(fileConversionService).convertFile(any(), any(), any(), any(), any());

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .param("outputFile", outputFile)
                        .param("addPageNumbers", "true")
                        .param("pageNumberPosition", "BOTTOM")
                        .param("pageNumberAlignment", "LEFT")
                        .param("pageNumberStyle", "ALPHABETIC_LOWER"))
                .andExpect(status().isOk())
                .andExpect(content().string("File converted successfully"));
    }

    @Test
    void testConvertFileWithInvalidPageNumberPosition() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.txt", MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());
        String outputFile = "test.pdf";

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .param("outputFile", outputFile)
                        .param("addPageNumbers", "true")
                        .param("pageNumberPosition", "MIDDLE")
                        .param("pageNumberAlignment", "CENTER")
                        .param("pageNumberStyle", "ARABIC"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid page numbering parameters")));
    }

    @Test
    void testConvertFileWithInvalidPageNumberAlignment() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.txt", MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());
        String outputFile = "test.pdf";

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .param("outputFile", outputFile)
                        .param("addPageNumbers", "true")
                        .param("pageNumberPosition", "BOTTOM")
                        .param("pageNumberAlignment", "MIDDLE")
                        .param("pageNumberStyle", "ARABIC"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid page numbering parameters")));
    }

    @Test
    void testConvertFileWithInvalidPageNumberStyle() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.txt", MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());
        String outputFile = "test.pdf";

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .param("outputFile", outputFile)
                        .param("addPageNumbers", "true")
                        .param("pageNumberPosition", "BOTTOM")
                        .param("pageNumberAlignment", "CENTER")
                        .param("pageNumberStyle", "INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid page numbering parameters")));
    }

    @Test
    void testConvertFileWithPageNumbersDisabled() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.txt", MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());
        String outputFile = "test.pdf";

        doNothing().when(fileConversionService).convertFile(any(), any(), any(), any(), any());

        // When addPageNumbers is false or not provided, page numbers should not be added
        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .param("outputFile", outputFile)
                        .param("addPageNumbers", "false"))
                .andExpect(status().isOk())
                .andExpect(content().string("File converted successfully"));
    }

    @Test
    void testConvertFileWithPageNumbersAndExistingPdf() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.txt", MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());
        MockMultipartFile existingPdf = new MockMultipartFile("existingPdf", "existing.pdf", MediaType.APPLICATION_PDF_VALUE, "pdf content".getBytes());
        String outputFile = "test.pdf";

        doNothing().when(fileConversionService).convertFile(any(), any(), any(), any(), any());

        mockMvc.perform(multipart("/api/convert")
                        .file(inputFile)
                        .file(existingPdf)
                        .param("outputFile", outputFile)
                        .param("position", "back")
                        .param("addPageNumbers", "true")
                        .param("pageNumberPosition", "BOTTOM")
                        .param("pageNumberAlignment", "CENTER")
                        .param("pageNumberStyle", "ARABIC"))
                .andExpect(status().isOk())
                .andExpect(content().string("File converted successfully"));
    }
}
