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

class ThreeMfToPdfServiceTest {

    private ThreeMfToPdfService threeMfToPdfService;
    private com.xtopdf.xtopdf.pdf.PdfBackendProvider pdfBackend;
    
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        pdfBackend = new com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend();
        threeMfToPdfService = new ThreeMfToPdfService(pdfBackend);
    }

    @Test
    void testConvert3mfToPdf_ValidFile() throws Exception {
        // Create a minimal 3MF file (ZIP with XML model)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            // Add 3D/3dmodel.model file
            ZipEntry entry = new ZipEntry("3D/3dmodel.model");
            zos.putNextEntry(entry);
            
            String modelXml = "<?xml version=\"1.0\"?>\n" +
                            "<model xmlns=\"http://schemas.microsoft.com/3dmanufacturing/core/2015/02\">\n" +
                            "  <resources>\n" +
                            "    <object id=\"1\" type=\"model\">\n" +
                            "      <mesh>\n" +
                            "        <vertices>\n" +
                            "          <vertex x=\"0\" y=\"0\" z=\"0\"/>\n" +
                            "          <vertex x=\"1\" y=\"0\" z=\"0\"/>\n" +
                            "          <vertex x=\"0\" y=\"1\" z=\"0\"/>\n" +
                            "        </vertices>\n" +
                            "        <triangles>\n" +
                            "          <triangle v1=\"0\" v2=\"1\" v3=\"2\"/>\n" +
                            "        </triangles>\n" +
                            "      </mesh>\n" +
                            "    </object>\n" +
                            "  </resources>\n" +
                            "  <build>\n" +
                            "    <item objectid=\"1\"/>\n" +
                            "  </build>\n" +
                            "</model>";
            
            zos.write(modelXml.getBytes());
            zos.closeEntry();
        }
        
        MockMultipartFile mfFile = new MockMultipartFile(
                "file", "test.3mf", "application/vnd.ms-package.3dmanufacturing-3dmodel+xml", baos.toByteArray());

        File pdfFile = tempDir.resolve("test3mfOutput.pdf").toFile();

        threeMfToPdfService.convert3mfToPdf(mfFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvert3mfToPdf_MultipleObjects() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry = new ZipEntry("3D/3dmodel.model");
            zos.putNextEntry(entry);
            
            String modelXml = "<?xml version=\"1.0\"?>\n" +
                            "<model xmlns=\"http://schemas.microsoft.com/3dmanufacturing/core/2015/02\">\n" +
                            "  <resources>\n" +
                            "    <object id=\"1\" type=\"model\">\n" +
                            "      <mesh>\n" +
                            "        <vertices>\n" +
                            "          <vertex x=\"0\" y=\"0\" z=\"0\"/>\n" +
                            "          <vertex x=\"1\" y=\"0\" z=\"0\"/>\n" +
                            "          <vertex x=\"0\" y=\"1\" z=\"0\"/>\n" +
                            "        </vertices>\n" +
                            "        <triangles>\n" +
                            "          <triangle v1=\"0\" v2=\"1\" v3=\"2\"/>\n" +
                            "        </triangles>\n" +
                            "      </mesh>\n" +
                            "    </object>\n" +
                            "    <object id=\"2\" type=\"model\">\n" +
                            "      <mesh>\n" +
                            "        <vertices>\n" +
                            "          <vertex x=\"0\" y=\"0\" z=\"0\"/>\n" +
                            "        </vertices>\n" +
                            "        <triangles/>\n" +
                            "      </mesh>\n" +
                            "    </object>\n" +
                            "  </resources>\n" +
                            "  <build>\n" +
                            "    <item objectid=\"1\"/>\n" +
                            "    <item objectid=\"2\"/>\n" +
                            "  </build>\n" +
                            "</model>";
            
            zos.write(modelXml.getBytes());
            zos.closeEntry();
        }
        
        MockMultipartFile mfFile = new MockMultipartFile(
                "file", "test.3mf", "application/vnd.ms-package.3dmanufacturing-3dmodel+xml", baos.toByteArray());

        File pdfFile = tempDir.resolve("test3mfMultipleOutput.pdf").toFile();

        threeMfToPdfService.convert3mfToPdf(mfFile, pdfFile);

        assertTrue(pdfFile.exists());
    }

    @Test
    void testConvert3mfToPdf_EmptyZip() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            // Empty ZIP
        }
        
        MockMultipartFile mfFile = new MockMultipartFile(
                "file", "test.3mf", "application/vnd.ms-package.3dmanufacturing-3dmodel+xml", baos.toByteArray());

        File pdfFile = tempDir.resolve("test3mfEmptyOutput.pdf").toFile();

        threeMfToPdfService.convert3mfToPdf(mfFile, pdfFile);

        assertTrue(pdfFile.exists());
    }
}
