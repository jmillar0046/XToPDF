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
        doNothing().when(fileConversionService).convertFile(any(), any());

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
        doThrow(new FileConversionException("Invalid file type")).when(fileConversionService).convertFile(any(), any());

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
        doThrow(new FileConversionException("Conversion failed")).when(fileConversionService).convertFile(any(), any());

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
}