package com.xtopdf.xtopdf.controllers;

import com.xtopdf.xtopdf.config.PageNumberConfig;
import com.xtopdf.xtopdf.config.WatermarkConfig;
import com.xtopdf.xtopdf.services.PageNumberService;
import com.xtopdf.xtopdf.services.PdfMergeService;
import com.xtopdf.xtopdf.services.WatermarkService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PdfOperationsController.class)
class PdfOperationsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PdfMergeService pdfMergeService;

    @MockitoBean
    private PageNumberService pageNumberService;

    @MockitoBean
    private WatermarkService watermarkService;

    @Test
    void testMergePdfs_Success() throws Exception {
        MockMultipartFile pdf1 = new MockMultipartFile("pdf1", "file1.pdf", 
                MediaType.APPLICATION_PDF_VALUE, "PDF content 1".getBytes());
        MockMultipartFile pdf2 = new MockMultipartFile("pdf2", "file2.pdf", 
                MediaType.APPLICATION_PDF_VALUE, "PDF content 2".getBytes());

        doNothing().when(pdfMergeService).mergePdfs(any(File.class), any(), anyString());

        mockMvc.perform(multipart("/api/pdf/merge")
                        .file(pdf1)
                        .file(pdf2)
                        .param("position", "back"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().exists("Content-Disposition"));

        verify(pdfMergeService, times(1)).mergePdfs(any(File.class), any(), eq("back"));
    }

    @Test
    void testMergePdfs_FrontPosition_Success() throws Exception {
        MockMultipartFile pdf1 = new MockMultipartFile("pdf1", "file1.pdf", 
                MediaType.APPLICATION_PDF_VALUE, "PDF content 1".getBytes());
        MockMultipartFile pdf2 = new MockMultipartFile("pdf2", "file2.pdf", 
                MediaType.APPLICATION_PDF_VALUE, "PDF content 2".getBytes());

        doNothing().when(pdfMergeService).mergePdfs(any(File.class), any(), anyString());

        mockMvc.perform(multipart("/api/pdf/merge")
                        .file(pdf1)
                        .file(pdf2)
                        .param("position", "front"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));

        verify(pdfMergeService, times(1)).mergePdfs(any(File.class), any(), eq("front"));
    }

    @Test
    void testMergePdfs_InvalidPosition_BadRequest() throws Exception {
        MockMultipartFile pdf1 = new MockMultipartFile("pdf1", "file1.pdf", 
                MediaType.APPLICATION_PDF_VALUE, "PDF content 1".getBytes());
        MockMultipartFile pdf2 = new MockMultipartFile("pdf2", "file2.pdf", 
                MediaType.APPLICATION_PDF_VALUE, "PDF content 2".getBytes());

        mockMvc.perform(multipart("/api/pdf/merge")
                        .file(pdf1)
                        .file(pdf2)
                        .param("position", "invalid"))
                .andExpect(status().isBadRequest());

        verify(pdfMergeService, never()).mergePdfs(any(), any(), anyString());
    }

    @Test
    void testMergePdfs_MergeServiceThrowsException_InternalServerError() throws Exception {
        MockMultipartFile pdf1 = new MockMultipartFile("pdf1", "file1.pdf", 
                MediaType.APPLICATION_PDF_VALUE, "PDF content 1".getBytes());
        MockMultipartFile pdf2 = new MockMultipartFile("pdf2", "file2.pdf", 
                MediaType.APPLICATION_PDF_VALUE, "PDF content 2".getBytes());

        doThrow(new RuntimeException("Merge failed"))
                .when(pdfMergeService).mergePdfs(any(File.class), any(), anyString());

        mockMvc.perform(multipart("/api/pdf/merge")
                        .file(pdf1)
                        .file(pdf2)
                        .param("position", "back"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testAddPageNumbers_Success() throws Exception {
        MockMultipartFile pdfFile = new MockMultipartFile("pdfFile", "test.pdf", 
                MediaType.APPLICATION_PDF_VALUE, "PDF content".getBytes());

        doNothing().when(pageNumberService).addPageNumbers(any(File.class), any(PageNumberConfig.class));

        mockMvc.perform(multipart("/api/pdf/add-page-numbers")
                        .file(pdfFile)
                        .param("position", "BOTTOM")
                        .param("alignment", "CENTER")
                        .param("style", "ARABIC"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().exists("Content-Disposition"));

        verify(pageNumberService, times(1)).addPageNumbers(any(File.class), any(PageNumberConfig.class));
    }

    @Test
    void testAddPageNumbers_DefaultValues_Success() throws Exception {
        MockMultipartFile pdfFile = new MockMultipartFile("pdfFile", "test.pdf", 
                MediaType.APPLICATION_PDF_VALUE, "PDF content".getBytes());

        doNothing().when(pageNumberService).addPageNumbers(any(File.class), any(PageNumberConfig.class));

        mockMvc.perform(multipart("/api/pdf/add-page-numbers")
                        .file(pdfFile))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));

        verify(pageNumberService, times(1)).addPageNumbers(any(File.class), any(PageNumberConfig.class));
    }

    @Test
    void testAddPageNumbers_InvalidPosition_BadRequest() throws Exception {
        MockMultipartFile pdfFile = new MockMultipartFile("pdfFile", "test.pdf", 
                MediaType.APPLICATION_PDF_VALUE, "PDF content".getBytes());

        mockMvc.perform(multipart("/api/pdf/add-page-numbers")
                        .file(pdfFile)
                        .param("position", "INVALID"))
                .andExpect(status().isBadRequest());

        verify(pageNumberService, never()).addPageNumbers(any(), any());
    }

    @Test
    void testAddPageNumbers_InvalidAlignment_BadRequest() throws Exception {
        MockMultipartFile pdfFile = new MockMultipartFile("pdfFile", "test.pdf", 
                MediaType.APPLICATION_PDF_VALUE, "PDF content".getBytes());

        mockMvc.perform(multipart("/api/pdf/add-page-numbers")
                        .file(pdfFile)
                        .param("alignment", "INVALID"))
                .andExpect(status().isBadRequest());

        verify(pageNumberService, never()).addPageNumbers(any(), any());
    }

    @Test
    void testAddPageNumbers_InvalidStyle_BadRequest() throws Exception {
        MockMultipartFile pdfFile = new MockMultipartFile("pdfFile", "test.pdf", 
                MediaType.APPLICATION_PDF_VALUE, "PDF content".getBytes());

        mockMvc.perform(multipart("/api/pdf/add-page-numbers")
                        .file(pdfFile)
                        .param("style", "INVALID"))
                .andExpect(status().isBadRequest());

        verify(pageNumberService, never()).addPageNumbers(any(), any());
    }

    @Test
    void testAddWatermark_Success() throws Exception {
        MockMultipartFile pdfFile = new MockMultipartFile("pdfFile", "test.pdf", 
                MediaType.APPLICATION_PDF_VALUE, "PDF content".getBytes());

        doNothing().when(watermarkService).addWatermark(any(File.class), any(WatermarkConfig.class));

        mockMvc.perform(multipart("/api/pdf/add-watermark")
                        .file(pdfFile)
                        .param("watermarkText", "CONFIDENTIAL")
                        .param("fontSize", "48")
                        .param("layer", "FOREGROUND")
                        .param("orientation", "DIAGONAL_UP"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().exists("Content-Disposition"));

        verify(watermarkService, times(1)).addWatermark(any(File.class), any(WatermarkConfig.class));
    }

    @Test
    void testAddWatermark_DefaultValues_Success() throws Exception {
        MockMultipartFile pdfFile = new MockMultipartFile("pdfFile", "test.pdf", 
                MediaType.APPLICATION_PDF_VALUE, "PDF content".getBytes());

        doNothing().when(watermarkService).addWatermark(any(File.class), any(WatermarkConfig.class));

        mockMvc.perform(multipart("/api/pdf/add-watermark")
                        .file(pdfFile)
                        .param("watermarkText", "DRAFT"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));

        verify(watermarkService, times(1)).addWatermark(any(File.class), any(WatermarkConfig.class));
    }

    @Test
    void testAddWatermark_MissingText_BadRequest() throws Exception {
        MockMultipartFile pdfFile = new MockMultipartFile("pdfFile", "test.pdf", 
                MediaType.APPLICATION_PDF_VALUE, "PDF content".getBytes());

        mockMvc.perform(multipart("/api/pdf/add-watermark")
                        .file(pdfFile))
                .andExpect(status().isBadRequest());

        verify(watermarkService, never()).addWatermark(any(), any());
    }

    @Test
    void testAddWatermark_EmptyText_BadRequest() throws Exception {
        MockMultipartFile pdfFile = new MockMultipartFile("pdfFile", "test.pdf", 
                MediaType.APPLICATION_PDF_VALUE, "PDF content".getBytes());

        mockMvc.perform(multipart("/api/pdf/add-watermark")
                        .file(pdfFile)
                        .param("watermarkText", ""))
                .andExpect(status().isBadRequest());

        verify(watermarkService, never()).addWatermark(any(), any());
    }

    @Test
    void testAddWatermark_FontSizeTooSmall_BadRequest() throws Exception {
        MockMultipartFile pdfFile = new MockMultipartFile("pdfFile", "test.pdf", 
                MediaType.APPLICATION_PDF_VALUE, "PDF content".getBytes());

        mockMvc.perform(multipart("/api/pdf/add-watermark")
                        .file(pdfFile)
                        .param("watermarkText", "TEST")
                        .param("fontSize", "0"))
                .andExpect(status().isBadRequest());

        verify(watermarkService, never()).addWatermark(any(), any());
    }

    @Test
    void testAddWatermark_FontSizeTooLarge_BadRequest() throws Exception {
        MockMultipartFile pdfFile = new MockMultipartFile("pdfFile", "test.pdf", 
                MediaType.APPLICATION_PDF_VALUE, "PDF content".getBytes());

        mockMvc.perform(multipart("/api/pdf/add-watermark")
                        .file(pdfFile)
                        .param("watermarkText", "TEST")
                        .param("fontSize", "201"))
                .andExpect(status().isBadRequest());

        verify(watermarkService, never()).addWatermark(any(), any());
    }

    @Test
    void testAddWatermark_InvalidLayer_BadRequest() throws Exception {
        MockMultipartFile pdfFile = new MockMultipartFile("pdfFile", "test.pdf", 
                MediaType.APPLICATION_PDF_VALUE, "PDF content".getBytes());

        mockMvc.perform(multipart("/api/pdf/add-watermark")
                        .file(pdfFile)
                        .param("watermarkText", "TEST")
                        .param("layer", "INVALID"))
                .andExpect(status().isBadRequest());

        verify(watermarkService, never()).addWatermark(any(), any());
    }

    @Test
    void testAddWatermark_InvalidOrientation_BadRequest() throws Exception {
        MockMultipartFile pdfFile = new MockMultipartFile("pdfFile", "test.pdf", 
                MediaType.APPLICATION_PDF_VALUE, "PDF content".getBytes());

        mockMvc.perform(multipart("/api/pdf/add-watermark")
                        .file(pdfFile)
                        .param("watermarkText", "TEST")
                        .param("orientation", "INVALID"))
                .andExpect(status().isBadRequest());

        verify(watermarkService, never()).addWatermark(any(), any());
    }
}
