package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.http.MediaType;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class HtmlToPdfServiceTest {

    private HtmlToPdfService createService() {
        HtmlToPdfService service = new HtmlToPdfService();
        service.pdfBackend = new PdfBoxBackend();
        return service;
    }

    @Test
    void testConvertValidHtmlToPdf(@TempDir Path tempDir) throws Exception {
        HtmlToPdfService service = createService();
        
        String html = "<html><body><h1>Hello PDF</h1></body></html>";
        MockMultipartFile htmlFile = new MockMultipartFile("file", "test.html", MediaType.TEXT_HTML_VALUE, html.getBytes());
        File pdfFile = tempDir.resolve("test.pdf").toFile();
        service.convertHtmlToPdf(htmlFile, pdfFile);
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertEmptyHtmlToPdf(@TempDir Path tempDir) throws Exception {
        HtmlToPdfService service = createService();
        
        MockMultipartFile htmlFile = new MockMultipartFile("file", "empty.html", MediaType.TEXT_HTML_VALUE, new byte[0]);
        File pdfFile = tempDir.resolve("empty.pdf").toFile();
        service.convertHtmlToPdf(htmlFile, pdfFile);
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertInvalidHtmlDoesNotThrow(@TempDir Path tempDir) {
        HtmlToPdfService service = createService();
        
        MockMultipartFile htmlFile = new MockMultipartFile("file", "invalid.html", MediaType.TEXT_HTML_VALUE, "<html><body>".getBytes());
        File pdfFile = tempDir.resolve("invalid.pdf").toFile();
        service.convertHtmlToPdf(htmlFile, pdfFile);
        assertTrue(pdfFile.exists());
    }

    @Test
    void testConvertHtmlWithStyles_Success(@TempDir Path tempDir) {
        HtmlToPdfService service = createService();
        String html = "<html><head><style>h1 { color: blue; }</style></head>" +
                     "<body><h1>Styled Heading</h1><p>Paragraph text</p></body></html>";
        MockMultipartFile htmlFile = new MockMultipartFile("file", "styled.html", MediaType.TEXT_HTML_VALUE, html.getBytes());
        File pdfFile = tempDir.resolve("styled.pdf").toFile();
        service.convertHtmlToPdf(htmlFile, pdfFile);
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertHtmlWithTable_Success(@TempDir Path tempDir) {
        HtmlToPdfService service = createService();
        String html = "<html><body>" +
                     "<table border='1'><tr><th>Name</th><th>Age</th></tr>" +
                     "<tr><td>John</td><td>30</td></tr>" +
                     "<tr><td>Jane</td><td>25</td></tr></table>" +
                     "</body></html>";
        MockMultipartFile htmlFile = new MockMultipartFile("file", "table.html", MediaType.TEXT_HTML_VALUE, html.getBytes());
        File pdfFile = tempDir.resolve("table.pdf").toFile();
        service.convertHtmlToPdf(htmlFile, pdfFile);
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertHtmlWithList_Success(@TempDir Path tempDir) {
        HtmlToPdfService service = createService();
        String html = "<html><body>" +
                     "<ul><li>Item 1</li><li>Item 2</li><li>Item 3</li></ul>" +
                     "<ol><li>First</li><li>Second</li><li>Third</li></ol>" +
                     "</body></html>";
        MockMultipartFile htmlFile = new MockMultipartFile("file", "list.html", MediaType.TEXT_HTML_VALUE, html.getBytes());
        File pdfFile = tempDir.resolve("list.pdf").toFile();
        service.convertHtmlToPdf(htmlFile, pdfFile);
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertHtmlWithLinks_Success(@TempDir Path tempDir) {
        HtmlToPdfService service = createService();
        String html = "<html><body>" +
                     "<a href='https://example.com'>Example Link</a>" +
                     "<p>Text with <a href='#'>internal link</a></p>" +
                     "</body></html>";
        MockMultipartFile htmlFile = new MockMultipartFile("file", "links.html", MediaType.TEXT_HTML_VALUE, html.getBytes());
        File pdfFile = tempDir.resolve("links.pdf").toFile();
        service.convertHtmlToPdf(htmlFile, pdfFile);
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertHtmlWithImages_Success(@TempDir Path tempDir) {
        HtmlToPdfService service = createService();
        String html = "<html><body>" +
                     "<img src='data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==' alt='test'/>" +
                     "</body></html>";
        MockMultipartFile htmlFile = new MockMultipartFile("file", "image.html", MediaType.TEXT_HTML_VALUE, html.getBytes());
        File pdfFile = tempDir.resolve("image.pdf").toFile();
        service.convertHtmlToPdf(htmlFile, pdfFile);
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertHtmlWithFormElements_Success(@TempDir Path tempDir) {
        HtmlToPdfService service = createService();
        String html = "<html><body>" +
                     "<form><input type='text' value='Sample'/>" +
                     "<textarea>Text area content</textarea>" +
                     "<select><option>Option 1</option></select></form>" +
                     "</body></html>";
        MockMultipartFile htmlFile = new MockMultipartFile("file", "form.html", MediaType.TEXT_HTML_VALUE, html.getBytes());
        File pdfFile = tempDir.resolve("form.pdf").toFile();
        service.convertHtmlToPdf(htmlFile, pdfFile);
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertHtmlWithSpecialCharacters_Success(@TempDir Path tempDir) throws Exception {
        HtmlToPdfService service = createService();
        String html = "<html><body>" +
                     "<p>Special: &lt; &gt; &amp; &quot; &apos;</p>" +
                     "<p>Unicode: 你好 世界 Ñoño</p>" +
                     "</body></html>";
        MockMultipartFile htmlFile = new MockMultipartFile("file", "special.html", MediaType.TEXT_HTML_VALUE, html.getBytes("UTF-8"));
        File pdfFile = tempDir.resolve("special.pdf").toFile();
        service.convertHtmlToPdf(htmlFile, pdfFile);
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertLargeHtml_Success(@TempDir Path tempDir) {
        HtmlToPdfService service = createService();
        StringBuilder html = new StringBuilder("<html><body>");
        for (int i = 0; i < 100; i++) {
            html.append("<p>This is paragraph ").append(i).append("</p>");
        }
        html.append("</body></html>");
        MockMultipartFile htmlFile = new MockMultipartFile("file", "large.html", MediaType.TEXT_HTML_VALUE, html.toString().getBytes());
        File pdfFile = tempDir.resolve("large.pdf").toFile();
        service.convertHtmlToPdf(htmlFile, pdfFile);
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertHtmlToPdfWithNullFileThrows(@TempDir Path tempDir) {
        HtmlToPdfService service = createService();
        File pdfFile = tempDir.resolve("nullfile.pdf").toFile();
        assertThrows(NullPointerException.class, () -> {
            service.convertHtmlToPdf(null, pdfFile);
        });
    }

    @Test
    void testConvertHtmlToPdfWithNullOutputFileThrows() {
        HtmlToPdfService service = createService();
        MockMultipartFile htmlFile = new MockMultipartFile("file", "test.html", MediaType.TEXT_HTML_VALUE, "<html></html>".getBytes());
        assertThrows(NullPointerException.class, () -> {
            service.convertHtmlToPdf(htmlFile, null);
        });
    }

    @Test
    void testConvertHtmlToPdfWithInvalidOutputPath() {
        HtmlToPdfService service = createService();
        MockMultipartFile htmlFile = new MockMultipartFile("file", "test.html", MediaType.TEXT_HTML_VALUE, "<html></html>".getBytes());
        // Use an invalid path (directory that doesn't exist and can't be created)
        File invalidFile = new File("/nonexistent/directory/that/cannot/be/created/test.pdf");
        
        // This should trigger the IOException catch block without throwing an exception
        service.convertHtmlToPdf(htmlFile, invalidFile);
        
        // If we reach here, the exception was caught properly and didn't propagate
        assertTrue(true);
    }

}