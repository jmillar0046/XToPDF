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

import java.io.IOException;

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

    @Test
    void convertFileTestInputIsMissing() throws Exception {
        mockMvc.perform(multipart("/api/convert"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Required parameter 'inputFile' is not present."));
    }

    @Test
    void convertFileTestOutputIsMissing() throws Exception {
        var content = "Hello, this is a test file content!";
        var file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, content.getBytes());

        mockMvc.perform(multipart("/api/convert").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Required parameter 'outputFile' is not present."));;
    }

    @Test
    void convertFileTestOutputIsEmpty() throws Exception {
        var content = "Hello, this is a test file content!";
        var file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, content.getBytes());
        doThrow(FileConversionException.class).when(fileConversionService).convertFile(any(), any());
        mockMvc.perform(multipart("/api/convert").file(file).param("outputFile", ""))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Missing parameter"));;
    }

    @Test
    void convertFileTestErrorWithConversion() throws Exception {
        var content = "Hello, this is a test file content!";
        var file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, content.getBytes());
        doThrow(FileConversionException.class).when(fileConversionService).convertFile(any(), any());
        mockMvc.perform(multipart("/api/convert").file(file).param("outputFile", "outputPath.pdf"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error with conversion"));;
    }

    @Test
    void convertFileTestAllGood() throws Exception {
        var content = "Hello, this is a test file content!";
        var file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, content.getBytes());
        doNothing().when(fileConversionService).convertFile(any(), any());
        mockMvc.perform(multipart("/api/convert").file(file).param("outputFile", "outputPath.pdf"))
                .andExpect(status().is2xxSuccessful());
    }

}