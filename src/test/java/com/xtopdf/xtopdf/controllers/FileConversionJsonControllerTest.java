package com.xtopdf.xtopdf.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtopdf.xtopdf.dto.ConversionRequest;
import com.xtopdf.xtopdf.dto.MergeRequest;
import com.xtopdf.xtopdf.dto.PageNumberRequest;
import com.xtopdf.xtopdf.dto.WatermarkRequest;
import com.xtopdf.xtopdf.enums.PageNumberAlignment;
import com.xtopdf.xtopdf.enums.PageNumberPosition;
import com.xtopdf.xtopdf.enums.PageNumberStyle;
import com.xtopdf.xtopdf.enums.WatermarkLayer;
import com.xtopdf.xtopdf.enums.WatermarkOrientation;
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

@WebMvcTest(controllers = FileConversionJsonController.class)
class FileConversionJsonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileConversionService fileConversionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testConvertFileWithJsonRequest_Success() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx", 
                MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());
        
        ConversionRequest request = ConversionRequest.builder()
                .outputFile("output.pdf")
                .build();
        
        MockMultipartFile requestPart = new MockMultipartFile("request", "", 
                MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(request));

        doNothing().when(fileConversionService).convertFile(any(), any(), any(), any(), any(), any(), any(Boolean.class));

        mockMvc.perform(multipart("/api/convert-json")
                        .file(inputFile)
                        .file(requestPart))
                .andExpect(status().isOk())
                .andExpect(content().string("File converted successfully"));

        verify(fileConversionService, times(1)).convertFile(any(), any(), any(), any(), any(), any(), any(Boolean.class));
    }

    @Test
    void testConvertFileWithJsonRequest_WithPageNumbers() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx", 
                MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());
        
        ConversionRequest request = ConversionRequest.builder()
                .outputFile("output.pdf")
                .pageNumbers(PageNumberRequest.builder()
                        .position(PageNumberPosition.BOTTOM)
                        .alignment(PageNumberAlignment.CENTER)
                        .style(PageNumberStyle.ARABIC)
                        .build())
                .build();
        
        MockMultipartFile requestPart = new MockMultipartFile("request", "", 
                MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(request));

        doNothing().when(fileConversionService).convertFile(any(), any(), any(), any(), any(), any(), any(Boolean.class));

        mockMvc.perform(multipart("/api/convert-json")
                        .file(inputFile)
                        .file(requestPart))
                .andExpect(status().isOk())
                .andExpect(content().string("File converted successfully"));
    }

    @Test
    void testConvertFileWithJsonRequest_WithWatermark() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx", 
                MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());
        
        ConversionRequest request = ConversionRequest.builder()
                .outputFile("output.pdf")
                .watermark(WatermarkRequest.builder()
                        .text("CONFIDENTIAL")
                        .fontSize(48f)
                        .layer(WatermarkLayer.FOREGROUND)
                        .orientation(WatermarkOrientation.DIAGONAL_UP)
                        .build())
                .build();
        
        MockMultipartFile requestPart = new MockMultipartFile("request", "", 
                MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(request));

        doNothing().when(fileConversionService).convertFile(any(), any(), any(), any(), any(), any(), any(Boolean.class));

        mockMvc.perform(multipart("/api/convert-json")
                        .file(inputFile)
                        .file(requestPart))
                .andExpect(status().isOk())
                .andExpect(content().string("File converted successfully"));
    }

    @Test
    void testConvertFileWithJsonRequest_WithMerge() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx", 
                MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());
        MockMultipartFile existingPdf = new MockMultipartFile("existingPdf", "existing.pdf", 
                MediaType.APPLICATION_PDF_VALUE, "existing PDF".getBytes());
        
        ConversionRequest request = ConversionRequest.builder()
                .outputFile("output.pdf")
                .merge(MergeRequest.builder()
                        .position("back")
                        .build())
                .build();
        
        MockMultipartFile requestPart = new MockMultipartFile("request", "", 
                MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(request));

        doNothing().when(fileConversionService).convertFile(any(), any(), any(), any(), any(), any(), any(Boolean.class));

        mockMvc.perform(multipart("/api/convert-json")
                        .file(inputFile)
                        .file(existingPdf)
                        .file(requestPart))
                .andExpect(status().isOk())
                .andExpect(content().string("File converted successfully"));
    }

    @Test
    void testConvertFileWithJsonRequest_WithAllFeatures() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx", 
                MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());
        MockMultipartFile existingPdf = new MockMultipartFile("existingPdf", "existing.pdf", 
                MediaType.APPLICATION_PDF_VALUE, "existing PDF".getBytes());
        
        ConversionRequest request = ConversionRequest.builder()
                .outputFile("output.pdf")
                .executeMacros(true)
                .pageNumbers(PageNumberRequest.builder()
                        .position(PageNumberPosition.BOTTOM)
                        .alignment(PageNumberAlignment.CENTER)
                        .style(PageNumberStyle.ARABIC)
                        .build())
                .watermark(WatermarkRequest.builder()
                        .text("CONFIDENTIAL")
                        .fontSize(48f)
                        .layer(WatermarkLayer.FOREGROUND)
                        .orientation(WatermarkOrientation.DIAGONAL_UP)
                        .build())
                .merge(MergeRequest.builder()
                        .position("front")
                        .build())
                .build();
        
        MockMultipartFile requestPart = new MockMultipartFile("request", "", 
                MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(request));

        doNothing().when(fileConversionService).convertFile(any(), any(), any(), any(), any(), any(), any(Boolean.class));

        mockMvc.perform(multipart("/api/convert-json")
                        .file(inputFile)
                        .file(existingPdf)
                        .file(requestPart))
                .andExpect(status().isOk())
                .andExpect(content().string("File converted successfully"));
    }

    @Test
    void testConvertFileWithJsonRequest_InvalidOutputPath() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx", 
                MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());
        
        ConversionRequest request = ConversionRequest.builder()
                .outputFile("../../../etc/passwd")
                .build();
        
        MockMultipartFile requestPart = new MockMultipartFile("request", "", 
                MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart("/api/convert-json")
                        .file(inputFile)
                        .file(requestPart))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid output file path"));

        verify(fileConversionService, never()).convertFile(any(), any(), any(), any(), any(), any(), any(Boolean.class));
    }

    @Test
    void testConvertFileWithJsonRequest_InvalidMergePosition() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx", 
                MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());
        
        ConversionRequest request = ConversionRequest.builder()
                .outputFile("output.pdf")
                .merge(MergeRequest.builder()
                        .position("invalid")
                        .build())
                .build();
        
        MockMultipartFile requestPart = new MockMultipartFile("request", "", 
                MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart("/api/convert-json")
                        .file(inputFile)
                        .file(requestPart))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid position. Must be 'front' or 'back'"));

        verify(fileConversionService, never()).convertFile(any(), any(), any(), any(), any(), any(), any(Boolean.class));
    }

    @Test
    void testConvertFileWithJsonRequest_MissingWatermarkText() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx", 
                MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());
        
        ConversionRequest request = ConversionRequest.builder()
                .outputFile("output.pdf")
                .watermark(WatermarkRequest.builder()
                        .fontSize(48f)
                        .build())
                .build();
        
        MockMultipartFile requestPart = new MockMultipartFile("request", "", 
                MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart("/api/convert-json")
                        .file(inputFile)
                        .file(requestPart))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Watermark text must be provided when addWatermark is true"));

        verify(fileConversionService, never()).convertFile(any(), any(), any(), any(), any(), any(), any(Boolean.class));
    }

    @Test
    void testConvertFileWithJsonRequest_InvalidWatermarkFontSize() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("inputFile", "test.docx", 
                MediaType.APPLICATION_OCTET_STREAM_VALUE, "test content".getBytes());
        
        ConversionRequest request = ConversionRequest.builder()
                .outputFile("output.pdf")
                .watermark(WatermarkRequest.builder()
                        .text("TEST")
                        .fontSize(300f)
                        .build())
                .build();
        
        MockMultipartFile requestPart = new MockMultipartFile("request", "", 
                MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart("/api/convert-json")
                        .file(inputFile)
                        .file(requestPart))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Watermark font size must be greater than 0 and up to 200"));

        verify(fileConversionService, never()).convertFile(any(), any(), any(), any(), any(), any(), any(Boolean.class));
    }
}
