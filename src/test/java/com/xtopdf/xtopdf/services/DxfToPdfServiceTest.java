package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DxfToPdfServiceTest {

    private DxfToPdfService dxfToPdfService;
    private File pdfFile;

    @BeforeEach
    void setUp() {
        dxfToPdfService = new DxfToPdfService(
            new com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend(),
            new DxfEntityParser(),
            new DxfCoordinateCalculator()
        );
    }

    @Test
    void testConvertDxfToPdf_Success() throws Exception {
        // Create a simple DXF content (basic DXF header)
        var content = "0\nSECTION\n2\nHEADER\n0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "test.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());

        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/testDxfOutput.pdf");

        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);

        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        assertTrue(pdfFile.length() > 0, "The PDF file should not be empty.");
        
        // Clean up
        pdfFile.delete();
    }

    @Test
    void testConvertDxfToPdf_EmptyFile() throws Exception {
        var content = "";
        var dxfFile = new MockMultipartFile("file", "test.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());

        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/testEmptyDxfOutput.pdf");

        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);

        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        
        // Clean up
        pdfFile.delete();
    }

    @Test
    void testConvertDxfToPdf_InvalidPdfCreation() {
        var content = "Test DXF content";
        var dxfFile = new MockMultipartFile("file", "test.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());

        assertThrows(IOException.class, () -> {
            dxfToPdfService.convertDxfToPdf(dxfFile, null);
        });
    }

    @Test
    void testConvertDxfToPdf_WithComplexContent() throws Exception {
        var content = "0\nSECTION\n2\nHEADER\n9\n$ACADVER\n1\nAC1009\n0\nENDSEC\n0\nSECTION\n2\nENTITIES\n0\nLINE\n8\n0\n10\n0.0\n20\n0.0\n11\n100.0\n21\n100.0\n0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "complex.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/complexDxfOutput.pdf");
        
        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
        
        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        assertTrue(pdfFile.length() > 0, "The PDF file should not be empty.");
        
        // Clean up
        pdfFile.delete();
    }

    @Test
    void testConvertDxfToPdf_NullMultipartFile_ThrowsNullPointerException() {
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/nullInput.pdf");
        assertThrows(NullPointerException.class, () -> dxfToPdfService.convertDxfToPdf(null, pdfFile));
    }

    @Test
    void testConvertDxfToPdf_NullOutputFile_ThrowsIOException() {
        var content = "test";
        var dxfFile = new MockMultipartFile("file", "test.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        assertThrows(IOException.class, () -> dxfToPdfService.convertDxfToPdf(dxfFile, null));
    }

    @Test
    void testConvertDxfToPdf_MultipleLines() throws Exception {
        var content = "Line 1\nLine 2\nLine 3";
        var dxfFile = new MockMultipartFile("file", "multilines.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/multilinesDxfOutput.pdf");
        
        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
        
        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        assertTrue(pdfFile.length() > 0, "The PDF file should not be empty.");
        
        // Clean up
        pdfFile.delete();
    }

    @Test
    void testConvertDxfToPdf_WithAllEntityTypes() throws Exception {
        // Create comprehensive DXF with multiple entity types
        var content = "0\nSECTION\n2\nHEADER\n9\n$ACADVER\n1\nAC1009\n0\nENDSEC\n0\nSECTION\n2\nENTITIES\n" +
                      "0\nLINE\n8\n0\n10\n0.0\n20\n0.0\n11\n100.0\n21\n100.0\n" +
                      "0\nCIRCLE\n8\n0\n10\n50.0\n20\n50.0\n40\n25.0\n" +
                      "0\nARC\n8\n0\n10\n0.0\n20\n0.0\n40\n10.0\n50\n0.0\n51\n90.0\n" +
                      "0\nPOINT\n8\n0\n10\n10.0\n20\n20.0\n" +
                      "0\nTEXT\n8\n0\n1\nHello\n10\n10.0\n20\n10.0\n40\n5.0\n" +
                      "0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "all_entities.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/allEntitiesDxfOutput.pdf");
        
        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
        
        // Clean up
        pdfFile.delete();
    }

    @Test
    void testConvertDxfToPdf_WithBlocks() throws Exception {
        var content = "0\nSECTION\n2\nHEADER\n9\n$ACADVER\n1\nAC1009\n0\nENDSEC\n" +
                      "0\nSECTION\n2\nBLOCKS\n" +
                      "0\nBLOCK\n2\nTestBlock\n10\n0.0\n20\n0.0\n" +
                      "0\nLINE\n8\n0\n10\n0.0\n20\n0.0\n11\n10.0\n21\n10.0\n" +
                      "0\nENDBLK\n" +
                      "0\nENDSEC\n" +
                      "0\nSECTION\n2\nENTITIES\n" +
                      "0\nINSERT\n2\nTestBlock\n10\n50.0\n20\n50.0\n" +
                      "0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "blocks.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/blocksDxfOutput.pdf");
        
        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        
        // Clean up
        pdfFile.delete();
    }

    @Test
    void testConvertDxfToPdf_With3DEntities() throws Exception {
        var content = "0\nSECTION\n2\nHEADER\n0\nENDSEC\n0\nSECTION\n2\nENTITIES\n" +
                      "0\n3DFACE\n8\n0\n10\n0.0\n20\n0.0\n30\n0.0\n11\n10.0\n21\n0.0\n31\n0.0\n12\n10.0\n22\n10.0\n32\n0.0\n13\n0.0\n23\n10.0\n33\n0.0\n" +
                      "0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "3d.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/3dDxfOutput.pdf");
        
        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        
        // Clean up
        pdfFile.delete();
    }

    @Test
    void testConvertDxfToPdf_WithEllipse() throws Exception {
        var content = "0\nSECTION\n2\nENTITIES\n0\nELLIPSE\n8\n0\n10\n50.0\n20\n50.0\n11\n30.0\n21\n0.0\n40\n0.5\n0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "ellipse.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/ellipseDxfOutput.pdf");
        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        pdfFile.delete();
    }

    @Test
    void testConvertDxfToPdf_WithPolyline() throws Exception {
        var content = "0\nSECTION\n2\nENTITIES\n0\nLWPOLYLINE\n8\n0\n90\n4\n70\n1\n10\n0.0\n20\n0.0\n10\n10.0\n20\n0.0\n10\n10.0\n20\n10.0\n10\n0.0\n20\n10.0\n0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "polyline.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/polylineDxfOutput.pdf");
        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        pdfFile.delete();
    }

    @Test
    void testConvertDxfToPdf_WithSolid() throws Exception {
        var content = "0\nSECTION\n2\nENTITIES\n0\nSOLID\n8\n0\n10\n0.0\n20\n0.0\n11\n10.0\n21\n0.0\n12\n10.0\n22\n10.0\n13\n0.0\n23\n10.0\n0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "solid.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/solidDxfOutput.pdf");
        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        pdfFile.delete();
    }

    @Test
    void testConvertDxfToPdf_WithMText() throws Exception {
        var content = "0\nSECTION\n2\nENTITIES\n0\nMTEXT\n8\n0\n10\n50.0\n20\n50.0\n40\n12.0\n41\n100.0\n1\nMulti\\Pline\\Ptext\n0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "mtext.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/mtextDxfOutput.pdf");
        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        pdfFile.delete();
    }

    @Test
    void testConvertDxfToPdf_WithDimension() throws Exception {
        var content = "0\nSECTION\n2\nENTITIES\n0\nDIMENSION\n8\n0\n70\n0\n10\n0.0\n20\n0.0\n11\n100.0\n21\n0.0\n13\n50.0\n23\n10.0\n14\n50.0\n24\n0.0\n0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "dimension.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/dimensionDxfOutput.pdf");
        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        pdfFile.delete();
    }

    @Test
    void testConvertDxfToPdf_WithLeader() throws Exception {
        var content = "0\nSECTION\n2\nENTITIES\n0\nLEADER\n8\n0\n90\n3\n10\n0.0\n20\n0.0\n10\n10.0\n20\n10.0\n10\n20.0\n20\n20.0\n10\n30.0\n20\n30.0\n1\nLeader Text\n0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "leader.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/leaderDxfOutput.pdf");
        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        pdfFile.delete();
    }

    @Test
    void testConvertDxfToPdf_WithTolerance() throws Exception {
        var content = "0\nSECTION\n2\nENTITIES\n0\nTOLERANCE\n8\n0\n10\n50.0\n20\n50.0\n40\n10.0\n1\nGD&T\n0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "tolerance.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/toleranceDxfOutput.pdf");
        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        pdfFile.delete();
    }

    @Test
    void testConvertDxfToPdf_WithTable() throws Exception {
        var content = "0\nSECTION\n2\nENTITIES\n0\nACAD_TABLE\n8\n0\n10\n50.0\n20\n50.0\n90\n2\n91\n2\n40\n20.0\n41\n50.0\n1\nA1\n1\nB1\n1\nA2\n1\nB2\n0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "table.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/tableDxfOutput.pdf");
        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        pdfFile.delete();
    }

    @Test
    void testConvertDxfToPdf_WithAttrib() throws Exception {
        var content = "0\nSECTION\n2\nENTITIES\n0\nATTRIB\n8\n0\n2\nTAG1\n3\nPrompt1\n1\nValue1\n10\n50.0\n20\n50.0\n40\n10.0\n0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "attrib.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/attribDxfOutput.pdf");
        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        pdfFile.delete();
    }

    @Test
    void testConvertDxfToPdf_WithXRef() throws Exception {
        var content = "0\nSECTION\n2\nENTITIES\n0\nXREF\n8\n0\n1\nexternal.dwg\n10\n50.0\n20\n50.0\n0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "xref.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/xrefDxfOutput.pdf");
        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        pdfFile.delete();
    }

    @Test
    void testConvertDxfToPdf_WithWipeout() throws Exception {
        var content = "0\nSECTION\n2\nENTITIES\n0\nWIPEOUT\n8\n0\n90\n4\n10\n0.0\n20\n0.0\n10\n10.0\n20\n0.0\n10\n10.0\n20\n10.0\n10\n0.0\n20\n10.0\n0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "wipeout.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/wipeoutDxfOutput.pdf");
        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        pdfFile.delete();
    }

    @Test
    void testConvertDxfToPdf_WithPolyFaceMesh() throws Exception {
        var content = "0\nSECTION\n2\nENTITIES\n0\nPOLYFACEMESH\n8\n0\n90\n4\n10\n0.0\n20\n0.0\n30\n0.0\n10\n10.0\n20\n0.0\n30\n0.0\n10\n10.0\n20\n10.0\n30\n0.0\n10\n0.0\n20\n10.0\n30\n0.0\n0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "mesh.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/meshDxfOutput.pdf");
        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        pdfFile.delete();
    }

    @Test
    void testConvertDxfToPdf_WithMesh() throws Exception {
        var content = "0\nSECTION\n2\nENTITIES\n0\nMESH\n8\n0\n91\n4\n92\n2\n10\n0.0\n20\n0.0\n30\n0.0\n10\n10.0\n20\n0.0\n30\n0.0\n10\n10.0\n20\n10.0\n30\n0.0\n10\n0.0\n20\n10.0\n30\n0.0\n0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "advmesh.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/advmeshDxfOutput.pdf");
        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        pdfFile.delete();
    }

    @Test
    void testConvertDxfToPdf_With3DSolid() throws Exception {
        var content = "0\nSECTION\n2\nENTITIES\n0\n3DSOLID\n8\n0\n10\n0.0\n20\n0.0\n30\n0.0\n11\n100.0\n21\n100.0\n31\n100.0\n0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "3dsolid.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/3dsolidDxfOutput.pdf");
        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        pdfFile.delete();
    }

    @Test
    void testConvertDxfToPdf_WithSurface() throws Exception {
        var content = "0\nSECTION\n2\nENTITIES\n0\nSURFACE\n8\n0\n70\n3\n71\n3\n72\n4\n73\n4\n74\n3\n0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "surface.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/surfaceDxfOutput.pdf");
        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        pdfFile.delete();
    }

    @Test
    void testConvertDxfToPdf_WithBody() throws Exception {
        var content = "0\nSECTION\n2\nENTITIES\n0\nBODY\n8\n0\n70\n1\n1\nACIS DATA\n0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "body.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/bodyDxfOutput.pdf");
        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        pdfFile.delete();
    }

    @Test
    void testConvertDxfToPdf_WithRegion() throws Exception {
        var content = "0\nSECTION\n2\nENTITIES\n0\nREGION\n8\n0\n90\n3\n70\n1\n10\n0.0\n20\n0.0\n10\n10.0\n20\n0.0\n10\n5.0\n20\n10.0\n0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "region.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/regionDxfOutput.pdf");
        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        pdfFile.delete();
    }

    @Test
    void testConvertDxfToPdf_WithViewport() throws Exception {
        var content = "0\nSECTION\n2\nENTITIES\n0\nVIEWPORT\n8\n0\n10\n100.0\n20\n100.0\n40\n50.0\n41\n50.0\n45\n1.0\n0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "viewport.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/viewportDxfOutput.pdf");
        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        pdfFile.delete();
    }

    @Test
    void testConvertDxfToPdf_WithImage() throws Exception {
        var content = "0\nSECTION\n2\nENTITIES\n0\nIMAGE\n8\n0\n10\n100.0\n20\n100.0\n40\n50.0\n41\n50.0\n1\nimage.png\n0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "image.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/imageDxfOutput.pdf");
        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        pdfFile.delete();
    }

    @Test
    void testConvertDxfToPdf_WithUnderlay() throws Exception {
        var content = "0\nSECTION\n2\nENTITIES\n0\nPDFUNDERLAY\n8\n0\n10\n100.0\n20\n100.0\n41\n1.0\n42\n1.0\n43\n1.0\n50\n0.0\n1\nunderlay.pdf\n0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "underlay.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/underlayDxfOutput.pdf");
        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        pdfFile.delete();
    }

    @Test
    void testConvertDxfToPdf_WithOleFrame() throws Exception {
        var content = "0\nSECTION\n2\nENTITIES\n0\nOLEFRAME\n8\n0\n10\n100.0\n20\n100.0\n40\n50.0\n41\n50.0\n70\n1\n90\n10\n1\nExcel.Sheet\n0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "oleframe.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/oleframeDxfOutput.pdf");
        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        pdfFile.delete();
    }

    @Test
    void testConvertDxfToPdf_WithNestedBlocks() throws Exception {
        var content = "0\nSECTION\n2\nHEADER\n0\nENDSEC\n" +
                      "0\nSECTION\n2\nBLOCKS\n" +
                      "0\nBLOCK\n2\nInner\n10\n0.0\n20\n0.0\n" +
                      "0\nCIRCLE\n8\n0\n10\n0.0\n20\n0.0\n40\n5.0\n" +
                      "0\nENDBLK\n" +
                      "0\nBLOCK\n2\nOuter\n10\n0.0\n20\n0.0\n" +
                      "0\nLINE\n8\n0\n10\n-10.0\n20\n-10.0\n11\n10.0\n21\n10.0\n" +
                      "0\nINSERT\n2\nInner\n10\n0.0\n20\n0.0\n41\n1.0\n42\n1.0\n50\n0.0\n" +
                      "0\nENDBLK\n" +
                      "0\nENDSEC\n" +
                      "0\nSECTION\n2\nENTITIES\n" +
                      "0\nINSERT\n2\nOuter\n10\n100.0\n20\n100.0\n41\n2.0\n42\n2.0\n50\n45.0\n" +
                      "0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "nested.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/nestedDxfOutput.pdf");
        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        pdfFile.delete();
    }
    
    @Test
    void testConvertDxfToPdf_InvalidGroupCodeOutOfRange() throws Exception {
        // Test with group code that would exceed Integer.MAX_VALUE
        var content = "0\nSECTION\n2\nENTITIES\n" +
                      "9999999999999\n" + // Invalid group code (too large)
                      "LINE\n" +
                      "0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "invalid.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/invalidGroupCodeDxf.pdf");
        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        pdfFile.delete();
    }
    
    @Test
    void testConvertDxfToPdf_InvalidDoubleValue() throws Exception {
        // Test with invalid double value (Infinity)
        var content = "0\nSECTION\n2\nENTITIES\n" +
                      "0\nLINE\n8\n0\n" +
                      "10\nInfinity\n" +
                      "20\n0.0\n11\n100.0\n21\n100.0\n" +
                      "0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "invalid.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/invalidDoubleDxf.pdf");
        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        pdfFile.delete();
    }
    
    @Test
    void testConvertDxfToPdf_NaNValue() throws Exception {
        // Test with NaN value
        var content = "0\nSECTION\n2\nENTITIES\n" +
                      "0\nCIRCLE\n8\n0\n" +
                      "10\n50.0\n20\n50.0\n" +
                      "40\nNaN\n" + // Invalid radius
                      "0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "nan.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/nanDxf.pdf");
        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        pdfFile.delete();
    }
    
    @Test
    void testConvertDxfToPdf_VeryLargeCoordinates() throws Exception {
        // Test with very large but valid coordinates
        var content = "0\nSECTION\n2\nENTITIES\n" +
                      "0\nLINE\n8\n0\n" +
                      "10\n999999999.99\n20\n999999999.99\n" +
                      "11\n-999999999.99\n21\n-999999999.99\n" +
                      "0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "large.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/largeDxf.pdf");
        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        pdfFile.delete();
    }
    
    @Test
    void testConvertDxfToPdf_MixedValidInvalidEntities() throws Exception {
        // Test with mix of valid and invalid entities
        var content = "0\nSECTION\n2\nENTITIES\n" +
                      "0\nLINE\n8\n0\n10\n0.0\n20\n0.0\n11\n100.0\n21\n100.0\n" +
                      "0\nCIRCLE\n8\n0\n10\nInvalid\n20\n50.0\n40\n25.0\n" + // Invalid X coord
                      "0\nARC\n8\n0\n10\n150.0\n20\n150.0\n40\n30.0\n50\n0.0\n51\n90.0\n" +
                      "0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "mixed.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/mixedDxf.pdf");
        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        pdfFile.delete();
    }
    
    @Test
    void testConvertDxfToPdf_ScientificNotation() throws Exception {
        // Test with scientific notation values
        var content = "0\nSECTION\n2\nENTITIES\n" +
                      "0\nLINE\n8\n0\n" +
                      "10\n1.5e2\n20\n2.3e1\n" + // 150.0, 23.0
                      "11\n3.7e-1\n21\n4.9e0\n" + // 0.37, 4.9
                      "0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "scientific.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/scientificDxf.pdf");
        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        pdfFile.delete();
    }
    
    @Test
    void testConvertDxfToPdf_EmptyStringCoordinates() throws Exception {
        // Test with empty string coordinates
        var content = "0\nSECTION\n2\nENTITIES\n" +
                      "0\nLINE\n8\n0\n" +
                      "10\n\n" + // Empty coordinate
                      "20\n0.0\n11\n100.0\n21\n100.0\n" +
                      "0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "empty.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/emptyCoordDxf.pdf");
        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        pdfFile.delete();
    }
    
    @Test
    void testConvertDxfToPdf_WhitespaceCoordinates() throws Exception {
        // Test with whitespace-only coordinates
        var content = "0\nSECTION\n2\nENTITIES\n" +
                      "0\nCIRCLE\n8\n0\n" +
                      "10\n   \n" + // Whitespace only
                      "20\n50.0\n40\n25.0\n" +
                      "0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "whitespace.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/whitespaceDxf.pdf");
        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        pdfFile.delete();
    }
    
    @Test
    void testConvertDxfToPdf_ZeroRadiusCircle() throws Exception {
        // Test with zero radius circle
        var content = "0\nSECTION\n2\nENTITIES\n" +
                      "0\nCIRCLE\n8\n0\n" +
                      "10\n50.0\n20\n50.0\n40\n0.0\n" +
                      "0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "zerocircle.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/zeroCircleDxf.pdf");
        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        pdfFile.delete();
    }
    
    @Test
    void testConvertDxfToPdf_NegativeAngles() throws Exception {
        // Test with negative angles
        var content = "0\nSECTION\n2\nENTITIES\n" +
                      "0\nARC\n8\n0\n" +
                      "10\n100.0\n20\n100.0\n40\n50.0\n" +
                      "50\n-45.0\n51\n-180.0\n" + // Negative angles
                      "0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "negangles.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/negAnglesDxf.pdf");
        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        pdfFile.delete();
    }
}
