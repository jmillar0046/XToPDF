package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class DwfToPdfServiceTest {

    private DwfToPdfService dwfToPdfService;
    
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        dwfToPdfService = new DwfToPdfService();
    }

    @Test
    void testConvertDwfToPdf_ValidFile() throws Exception {
        // Create a minimal DWF file (ZIP with some content)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry1 = new ZipEntry("descriptor.xml");
            zos.putNextEntry(entry1);
            zos.write("<?xml version=\"1.0\"?><descriptor/>".getBytes());
            zos.closeEntry();
            
            ZipEntry entry2 = new ZipEntry("data/section1.w2d");
            zos.putNextEntry(entry2);
            zos.write("binary data".getBytes());
            zos.closeEntry();
        }
        
        MockMultipartFile dwfFile = new MockMultipartFile(
                "file", "test.dwf", "model/vnd.dwf", baos.toByteArray());

        File pdfFile = tempDir.resolve("testDwfOutput.pdf").toFile();

        dwfToPdfService.convertDwfToPdf(dwfFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertDwfToPdf_WithMultipleSections() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry1 = new ZipEntry("section1/model.w2d");
            zos.putNextEntry(entry1);
            zos.write("data1".getBytes());
            zos.closeEntry();
            
            ZipEntry entry2 = new ZipEntry("section2/model.w2d");
            zos.putNextEntry(entry2);
            zos.write("data2".getBytes());
            zos.closeEntry();
            
            ZipEntry entry3 = new ZipEntry("metadata.xml");
            zos.putNextEntry(entry3);
            zos.write("<metadata/>".getBytes());
            zos.closeEntry();
        }
        
        MockMultipartFile dwfFile = new MockMultipartFile(
                "file", "test.dwf", "model/vnd.dwf", baos.toByteArray());

        File pdfFile = tempDir.resolve("testDwfMultiOutput.pdf").toFile();

        dwfToPdfService.convertDwfToPdf(dwfFile, pdfFile);

        assertTrue(pdfFile.exists());
    }

    @Test
    void testConvertDwfToPdf_EmptyZip() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            // Empty ZIP
        }
        
        MockMultipartFile dwfFile = new MockMultipartFile(
                "file", "test.dwf", "model/vnd.dwf", baos.toByteArray());

        File pdfFile = tempDir.resolve("testDwfEmptyOutput.pdf").toFile();

        dwfToPdfService.convertDwfToPdf(dwfFile, pdfFile);

        assertTrue(pdfFile.exists());
    }
}
