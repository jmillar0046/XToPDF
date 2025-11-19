package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class WrlToPdfServiceTest {

    private WrlToPdfService wrlToPdfService;
    
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        wrlToPdfService = new WrlToPdfService();
    }

    @Test
    void testConvertWrlToPdf_ValidVrml2File() throws Exception {
        String content = "#VRML V2.0 utf8\n" +
                        "Shape {\n" +
                        "  geometry Box { size 2 2 2 }\n" +
                        "}\n" +
                        "Transform {\n" +
                        "  children [\n" +
                        "    Shape {\n" +
                        "      appearance Material { diffuseColor 1 0 0 }\n" +
                        "      geometry Sphere { radius 1 }\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}";
        
        MockMultipartFile wrlFile = new MockMultipartFile(
                "file", "test.wrl", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());

        File pdfFile = tempDir.resolve("testWrlOutput.pdf").toFile();

        wrlToPdfService.convertWrlToPdf(wrlFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertWrlToPdf_Vrml1File() throws Exception {
        String content = "#VRML V1.0 ascii\n" +
                        "Separator {\n" +
                        "  Material { diffuseColor 0 1 0 }\n" +
                        "  Cube { }\n" +
                        "}";
        
        MockMultipartFile wrlFile = new MockMultipartFile(
                "file", "test.wrl", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());

        File pdfFile = tempDir.resolve("testWrl1Output.pdf").toFile();

        wrlToPdfService.convertWrlToPdf(wrlFile, pdfFile);

        assertTrue(pdfFile.exists());
    }

    @Test
    void testConvertWrlToPdf_EmptyFile() throws Exception {
        MockMultipartFile wrlFile = new MockMultipartFile(
                "file", "test.wrl", MediaType.APPLICATION_OCTET_STREAM_VALUE, "".getBytes());

        File pdfFile = tempDir.resolve("testEmptyWrlOutput.pdf").toFile();

        wrlToPdfService.convertWrlToPdf(wrlFile, pdfFile);

        assertTrue(pdfFile.exists());
    }

    @Test
    void testConvertWrlToPdf_WithComments() throws Exception {
        String content = "#VRML V2.0 utf8\n" +
                        "# This is a comment\n" +
                        "Shape {\n" +
                        "  # Another comment\n" +
                        "  geometry Sphere { }\n" +
                        "}";
        
        MockMultipartFile wrlFile = new MockMultipartFile(
                "file", "test.wrl", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());

        File pdfFile = tempDir.resolve("testWrlCommentsOutput.pdf").toFile();

        wrlToPdfService.convertWrlToPdf(wrlFile, pdfFile);

        assertTrue(pdfFile.exists());
    }
}
