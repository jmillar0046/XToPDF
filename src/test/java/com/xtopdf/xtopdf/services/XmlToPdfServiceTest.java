package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.*;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class XmlToPdfServiceTest {

    private XmlToPdfService xmlToPdfService;

    @BeforeEach
    void setUp() {
        xmlToPdfService = new XmlToPdfService();
    }

    @Test
    void testConvertXmlToPdf_SimpleXml_Success(@TempDir Path tempDir) throws Exception {
        String content = "<?xml version=\"1.0\"?><root><item>Test</item></root>";
        MockMultipartFile xmlFile = new MockMultipartFile(
                "file", 
                "test.xml", 
                "text/xml", 
                content.getBytes()
        );

        File pdfFile = tempDir.resolve("testXmlOutput.pdf").toFile();

        xmlToPdfService.convertXmlToPdf(xmlFile, pdfFile);

        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        assertTrue(pdfFile.length() > 0, "The PDF file should not be empty.");
    }

    @Test
    void testConvertXmlToPdf_NestedXml_Success(@TempDir Path tempDir) throws Exception {
        String content = "<?xml version=\"1.0\"?><root><parent><child>Value1</child><child>Value2</child></parent></root>";
        MockMultipartFile xmlFile = new MockMultipartFile(
                "file", 
                "nested.xml", 
                "text/xml", 
                content.getBytes()
        );

        File pdfFile = tempDir.resolve("nested_output.pdf").toFile();

        xmlToPdfService.convertXmlToPdf(xmlFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertXmlToPdf_WithAttributes_Success(@TempDir Path tempDir) throws Exception {
        String content = "<?xml version=\"1.0\"?><root><item id=\"1\" name=\"Test\">Content</item></root>";
        MockMultipartFile xmlFile = new MockMultipartFile(
                "file", 
                "attributes.xml", 
                "text/xml", 
                content.getBytes()
        );

        File pdfFile = tempDir.resolve("attributes_output.pdf").toFile();

        xmlToPdfService.convertXmlToPdf(xmlFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertXmlToPdf_WithNamespace_Success(@TempDir Path tempDir) throws Exception {
        String content = "<?xml version=\"1.0\"?><root xmlns=\"http://example.com\"><item>Test</item></root>";
        MockMultipartFile xmlFile = new MockMultipartFile(
                "file", 
                "namespace.xml", 
                "text/xml", 
                content.getBytes()
        );

        File pdfFile = tempDir.resolve("namespace_output.pdf").toFile();

        xmlToPdfService.convertXmlToPdf(xmlFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertXmlToPdf_EmptyFile_Success(@TempDir Path tempDir) throws Exception {
        String content = "";
        MockMultipartFile xmlFile = new MockMultipartFile(
                "file", 
                "test.xml", 
                "text/xml", 
                content.getBytes()
        );

        File pdfFile = tempDir.resolve("testXmlEmptyOutput.pdf").toFile();

        xmlToPdfService.convertXmlToPdf(xmlFile, pdfFile);

        assertTrue(pdfFile.exists(), "The PDF file should be created.");
    }

    @Test
    void testConvertXmlToPdf_LargeXml_Success(@TempDir Path tempDir) throws Exception {
        StringBuilder largeXml = new StringBuilder("<?xml version=\"1.0\"?><root>");
        for (int i = 0; i < 100; i++) {
            largeXml.append("<item id=\"").append(i).append("\">Value").append(i).append("</item>");
        }
        largeXml.append("</root>");

        MockMultipartFile xmlFile = new MockMultipartFile(
                "file", 
                "large.xml", 
                "text/xml", 
                largeXml.toString().getBytes()
        );

        File pdfFile = tempDir.resolve("large_output.pdf").toFile();

        xmlToPdfService.convertXmlToPdf(xmlFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertXmlToPdf_SpecialCharacters_Success(@TempDir Path tempDir) throws Exception {
        String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><text>Hello 世界 Ñoño</text></root>";
        MockMultipartFile xmlFile = new MockMultipartFile(
                "file", 
                "special.xml", 
                "text/xml", 
                content.getBytes("UTF-8")
        );

        File pdfFile = tempDir.resolve("special_output.pdf").toFile();

        xmlToPdfService.convertXmlToPdf(xmlFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertXmlToPdf_WithCDATA_Success(@TempDir Path tempDir) throws Exception {
        String content = "<?xml version=\"1.0\"?><root><data><![CDATA[Some <special> data & stuff]]></data></root>";
        MockMultipartFile xmlFile = new MockMultipartFile(
                "file", 
                "cdata.xml", 
                "text/xml", 
                content.getBytes()
        );

        File pdfFile = tempDir.resolve("cdata_output.pdf").toFile();

        xmlToPdfService.convertXmlToPdf(xmlFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertXmlToPdf_NullMultipartFile_ThrowsNullPointerException(@TempDir Path tempDir) {
        File pdfFile = tempDir.resolve("nullInput.pdf").toFile();
        assertThrows(NullPointerException.class, 
            () -> xmlToPdfService.convertXmlToPdf(null, pdfFile));
    }

    @Test
    void testConvertXmlToPdf_NullOutputFile_ThrowsIOException() {
        String content = "test";
        MockMultipartFile xmlFile = new MockMultipartFile(
                "file", 
                "test.xml", 
                "text/xml", 
                content.getBytes()
        );
        assertThrows(IOException.class, 
            () -> xmlToPdfService.convertXmlToPdf(xmlFile, null));
    }

    @Test
    void testConvertXmlToPdf_MultipleRootElements_Success(@TempDir Path tempDir) throws Exception {
        String content = "<?xml version=\"1.0\"?><root><section id=\"1\"><title>Section 1</title><content>Content 1</content></section><section id=\"2\"><title>Section 2</title><content>Content 2</content></section></root>";
        MockMultipartFile xmlFile = new MockMultipartFile(
                "file", 
                "sections.xml", 
                "text/xml", 
                content.getBytes()
        );

        File pdfFile = tempDir.resolve("sections_output.pdf").toFile();

        xmlToPdfService.convertXmlToPdf(xmlFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }
}
