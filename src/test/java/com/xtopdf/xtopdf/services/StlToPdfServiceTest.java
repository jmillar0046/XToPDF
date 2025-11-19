package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class StlToPdfServiceTest {

    private StlToPdfService stlToPdfService;
    
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        stlToPdfService = new StlToPdfService();
    }

    @Test
    void testConvertStlToPdf_AsciiFormat() throws Exception {
        String content = "solid test\nfacet normal 0 0 1\nouter loop\nvertex 0 0 0\nvertex 1 0 0\nvertex 0 1 0\nendloop\nendfacet\nendsolid";
        
        MockMultipartFile stlFile = new MockMultipartFile(
                "file", "test.stl", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());

        File pdfFile = tempDir.resolve("testStlOutput.pdf").toFile();

        stlToPdfService.convertStlToPdf(stlFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertStlToPdf_EmptyFile() throws Exception {
        MockMultipartFile stlFile = new MockMultipartFile(
                "file", "test.stl", MediaType.APPLICATION_OCTET_STREAM_VALUE, "".getBytes());

        File pdfFile = tempDir.resolve("testEmptyStlOutput.pdf").toFile();

        // Empty file should still create a PDF (with 0 triangles)
        stlToPdfService.convertStlToPdf(stlFile, pdfFile);
        
        assertTrue(pdfFile.exists());
    }
}
