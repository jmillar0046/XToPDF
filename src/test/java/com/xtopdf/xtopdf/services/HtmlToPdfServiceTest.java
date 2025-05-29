package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.Test;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.http.MediaType;
import java.io.File;
import java.io.IOException;

class HtmlToPdfServiceTest {

    @Test
    void testConvertValidHtmlToPdf() throws Exception {
        HtmlToPdfService service = new HtmlToPdfService();
        String html = "<html><body><h1>Hello PDF</h1></body></html>";
        MockMultipartFile htmlFile = new MockMultipartFile("file", "test.html", MediaType.TEXT_HTML_VALUE, html.getBytes());
        File pdfFile = new File(System.getProperty("java.io.tmpdir") + "/test.pdf");
        service.convertHtmlToPdf(htmlFile, pdfFile);
        assert pdfFile.exists();
        assert pdfFile.length() > 0;
    }

    @Test
    void testConvertEmptyHtmlToPdf() throws Exception {
        HtmlToPdfService service = new HtmlToPdfService();
        MockMultipartFile htmlFile = new MockMultipartFile("file", "empty.html", MediaType.TEXT_HTML_VALUE, new byte[0]);
        File pdfFile = new File(System.getProperty("java.io.tmpdir") + "/empty.pdf");
        service.convertHtmlToPdf(htmlFile, pdfFile);
        assert pdfFile.exists();
        assert pdfFile.length() > 0;
    }

    @Test
    void testConvertInvalidHtmlDoesNotThrow() {
        HtmlToPdfService service = new HtmlToPdfService();
        MockMultipartFile htmlFile = new MockMultipartFile("file", "invalid.html", MediaType.TEXT_HTML_VALUE, "<html><body>".getBytes());
        File pdfFile = new File(System.getProperty("java.io.tmpdir") + "/invalid.pdf");
        service.convertHtmlToPdf(htmlFile, pdfFile);
        assert pdfFile.exists();
    }

    @Test
    void testConvertHtmlToPdfWithNullFileThrows() {
        HtmlToPdfService service = new HtmlToPdfService();
        File pdfFile = new File(System.getProperty("java.io.tmpdir") + "/nullfile.pdf");
        try {
            service.convertHtmlToPdf(null, pdfFile);
            assert false : "Should throw NullPointerException for null MultipartFile";
        } catch (NullPointerException e) {
            assert true;
        } catch (Exception e) {
            assert false : "Expected NullPointerException";
        }
    }

    @Test
    void testConvertHtmlToPdfWithNullOutputFileThrows() {
        HtmlToPdfService service = new HtmlToPdfService();
        MockMultipartFile htmlFile = new MockMultipartFile("file", "test.html", MediaType.TEXT_HTML_VALUE, "<html></html>".getBytes());
        try {
            service.convertHtmlToPdf(htmlFile, null);
            assert false : "Should throw NullPointerException for null output file";
        } catch (NullPointerException e) {
            assert true;
        } catch (Exception e) {
            assert false : "Expected NullPointerException";
        }
    }

}