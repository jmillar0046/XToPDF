package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class X3dToPdfServiceTest {

    private X3dToPdfService x3dToPdfService;
    private com.xtopdf.xtopdf.pdf.PdfBackendProvider pdfBackend;
    
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        pdfBackend = new com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend();
        x3dToPdfService = new X3dToPdfService(pdfBackend);
    }

    @Test
    void testConvertX3dToPdf_ValidX3dFile() throws Exception {
        String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<X3D version=\"3.0\">\n" +
                        "  <Scene>\n" +
                        "    <Shape>\n" +
                        "      <Box size=\"2 2 2\"/>\n" +
                        "      <Appearance>\n" +
                        "        <Material diffuseColor=\"1 0 0\"/>\n" +
                        "      </Appearance>\n" +
                        "    </Shape>\n" +
                        "    <Transform translation=\"0 3 0\">\n" +
                        "      <Shape>\n" +
                        "        <Sphere radius=\"1\"/>\n" +
                        "      </Shape>\n" +
                        "    </Transform>\n" +
                        "  </Scene>\n" +
                        "</X3D>";
        
        MockMultipartFile x3dFile = new MockMultipartFile(
                "file", "test.x3d", MediaType.APPLICATION_XML_VALUE, content.getBytes());

        File pdfFile = tempDir.resolve("testX3dOutput.pdf").toFile();

        x3dToPdfService.convertX3dToPdf(x3dFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertX3dToPdf_WithMultipleGeometries() throws Exception {
        String content = "<?xml version=\"1.0\"?>\n" +
                        "<X3D version=\"4.0\">\n" +
                        "  <Scene>\n" +
                        "    <Shape>\n" +
                        "      <Box/>\n" +
                        "    </Shape>\n" +
                        "    <Shape>\n" +
                        "      <Sphere/>\n" +
                        "    </Shape>\n" +
                        "    <Shape>\n" +
                        "      <Cone/>\n" +
                        "    </Shape>\n" +
                        "    <Shape>\n" +
                        "      <Cylinder/>\n" +
                        "    </Shape>\n" +
                        "    <Shape>\n" +
                        "      <IndexedFaceSet coordIndex=\"0 1 2 -1\"/>\n" +
                        "    </Shape>\n" +
                        "  </Scene>\n" +
                        "</X3D>";
        
        MockMultipartFile x3dFile = new MockMultipartFile(
                "file", "test.x3d", MediaType.APPLICATION_XML_VALUE, content.getBytes());

        File pdfFile = tempDir.resolve("testX3dGeometriesOutput.pdf").toFile();

        x3dToPdfService.convertX3dToPdf(x3dFile, pdfFile);

        assertTrue(pdfFile.exists());
    }

    @Test
    void testConvertX3dToPdf_EmptyScene() throws Exception {
        String content = "<?xml version=\"1.0\"?>\n" +
                        "<X3D version=\"3.0\">\n" +
                        "  <Scene/>\n" +
                        "</X3D>";
        
        MockMultipartFile x3dFile = new MockMultipartFile(
                "file", "test.x3d", MediaType.APPLICATION_XML_VALUE, content.getBytes());

        File pdfFile = tempDir.resolve("testX3dEmptyOutput.pdf").toFile();

        x3dToPdfService.convertX3dToPdf(x3dFile, pdfFile);

        assertTrue(pdfFile.exists());
    }

    @Test
    void testConvertX3dToPdf_ComplexScene() throws Exception {
        String content = "<?xml version=\"1.0\"?>\n" +
                        "<X3D version=\"3.3\">\n" +
                        "  <Scene>\n" +
                        "    <Transform>\n" +
                        "      <Transform>\n" +
                        "        <Shape>\n" +
                        "          <Material/>\n" +
                        "          <Box/>\n" +
                        "        </Shape>\n" +
                        "      </Transform>\n" +
                        "    </Transform>\n" +
                        "  </Scene>\n" +
                        "</X3D>";
        
        MockMultipartFile x3dFile = new MockMultipartFile(
                "file", "test.x3d", MediaType.APPLICATION_XML_VALUE, content.getBytes());

        File pdfFile = tempDir.resolve("testX3dComplexOutput.pdf").toFile();

        x3dToPdfService.convertX3dToPdf(x3dFile, pdfFile);

        assertTrue(pdfFile.exists());
    }
}
