package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ObjToPdfServiceTest {

    private ObjToPdfService objToPdfService;
    private com.xtopdf.xtopdf.pdf.PdfBackendProvider pdfBackend;
    
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        pdfBackend = new com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend();
        objToPdfService = new ObjToPdfService(pdfBackend);
    }

    @Test
    void testConvertObjToPdf_Success() throws Exception {
        String content = "# Test OBJ\nv 0 0 0\nv 1 0 0\nv 0 1 0\nf 1 2 3\n";
        
        MockMultipartFile objFile = new MockMultipartFile(
                "file", "test.obj", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());

        File pdfFile = tempDir.resolve("testObjOutput.pdf").toFile();

        objToPdfService.convertObjToPdf(objFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertObjToPdf_WithGroups() throws Exception {
        String content = "v 0 0 0\ng group1\nv 1 0 0\nf 1 2 3\n";
        
        MockMultipartFile objFile = new MockMultipartFile(
                "file", "test.obj", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());

        File pdfFile = tempDir.resolve("testObjGroupsOutput.pdf").toFile();

        objToPdfService.convertObjToPdf(objFile, pdfFile);

        assertTrue(pdfFile.exists());
    }
}
