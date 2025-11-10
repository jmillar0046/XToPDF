package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MarkdownToPdfServiceTest {

    private MarkdownToPdfService markdownToPdfService;

    private File pdfFile;

    @BeforeEach
    void setUp() {
        markdownToPdfService = new MarkdownToPdfService();
    }

    @Test
    void testConvertMarkdownToPdf_Success() throws Exception {
        var content = "# Hello World\n\nThis is a **test** markdown file.";
        var markdownFile = new MockMultipartFile("file", "test.md", MediaType.TEXT_MARKDOWN_VALUE, content.getBytes());

        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/testMarkdownOutput.pdf");

        markdownToPdfService.convertMarkdownToPdf(markdownFile, pdfFile);

        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        assertTrue(pdfFile.length() > 0, "The PDF file should not be empty.");
    }

    @Test
    void testConvertMarkdownToPdf_EmptyFile() throws Exception {
        var content = "";
        var markdownFile = new MockMultipartFile("file", "test.md", MediaType.TEXT_MARKDOWN_VALUE, content.getBytes());

        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/testEmptyMarkdownOutput.pdf");

        markdownToPdfService.convertMarkdownToPdf(markdownFile, pdfFile);

        assertTrue(pdfFile.exists(), "The PDF file should be created.");
    }

    @Test
    void testConvertMarkdownToPdf_InvalidPdfCreation() throws Exception {
        var content = "# Test Markdown";
        var markdownFile = new MockMultipartFile("file", "test.md", MediaType.TEXT_MARKDOWN_VALUE, content.getBytes());

        assertThrows(IOException.class, () -> {
            markdownToPdfService.convertMarkdownToPdf(markdownFile, pdfFile);
        });
    }

    @Test
    void testConvertMarkdownToPdf_WithUnicodeCharacters() throws Exception {
        var content = "# Hello, 你好, привет, 😀\n\n**Bold** and *italic* text.";
        var markdownFile = new MockMultipartFile("file", "unicode.md", MediaType.TEXT_MARKDOWN_VALUE, content.getBytes("UTF-8"));
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/unicodeMarkdownOutput.pdf");
        markdownToPdfService.convertMarkdownToPdf(markdownFile, pdfFile);
        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        assertTrue(pdfFile.length() > 0, "The PDF file should not be empty.");
    }

    @Test
    void testConvertMarkdownToPdf_OnlyWhitespace() throws Exception {
        var content = "     \n   \n";
        var markdownFile = new MockMultipartFile("file", "whitespace.md", MediaType.TEXT_MARKDOWN_VALUE, content.getBytes());
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/whitespaceMarkdownOutput.pdf");
        markdownToPdfService.convertMarkdownToPdf(markdownFile, pdfFile);
        assertTrue(pdfFile.exists(), "The PDF file should be created.");
    }

    @Test
    void testConvertMarkdownToPdf_NullMultipartFile_ThrowsNullPointerException() {
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/nullInput.pdf");
        assertThrows(NullPointerException.class, () -> markdownToPdfService.convertMarkdownToPdf(null, pdfFile));
    }

    @Test
    void testConvertMarkdownToPdf_NullOutputFile_ThrowsIOException() {
        var content = "# test";
        var markdownFile = new MockMultipartFile("file", "test.md", MediaType.TEXT_MARKDOWN_VALUE, content.getBytes());
        assertThrows(IOException.class, () -> markdownToPdfService.convertMarkdownToPdf(markdownFile, null));
    }

    @Test
    void testConvertMarkdownToPdf_MultipleLines() throws Exception {
        var content = "# Line 1\n## Line 2\n### Line 3";
        var markdownFile = new MockMultipartFile("file", "multilines.md", MediaType.TEXT_MARKDOWN_VALUE, content.getBytes());
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/multilinesMarkdownOutput.pdf");
        markdownToPdfService.convertMarkdownToPdf(markdownFile, pdfFile);
        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        assertTrue(pdfFile.length() > 0, "The PDF file should not be empty.");
    }

    @Test
    void testConvertMarkdownToPdf_WithLists() throws Exception {
        var content = "# List Test\n\n- Item 1\n- Item 2\n- Item 3";
        var markdownFile = new MockMultipartFile("file", "lists.md", MediaType.TEXT_MARKDOWN_VALUE, content.getBytes());
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/listsMarkdownOutput.pdf");
        markdownToPdfService.convertMarkdownToPdf(markdownFile, pdfFile);
        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        assertTrue(pdfFile.length() > 0, "The PDF file should not be empty.");
    }

    @Test
    void testConvertMarkdownToPdf_WithLinks() throws Exception {
        var content = "# Link Test\n\n[Example Link](https://example.com)";
        var markdownFile = new MockMultipartFile("file", "links.md", MediaType.TEXT_MARKDOWN_VALUE, content.getBytes());
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/linksMarkdownOutput.pdf");
        markdownToPdfService.convertMarkdownToPdf(markdownFile, pdfFile);
        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        assertTrue(pdfFile.length() > 0, "The PDF file should not be empty.");
    }

    @Test
    void testConvertMarkdownToPdf_WithCodeBlock() throws Exception {
        var content = "# Code Test\n\n```java\npublic class Test {}\n```";
        var markdownFile = new MockMultipartFile("file", "code.md", MediaType.TEXT_MARKDOWN_VALUE, content.getBytes());
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/codeMarkdownOutput.pdf");
        markdownToPdfService.convertMarkdownToPdf(markdownFile, pdfFile);
        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        assertTrue(pdfFile.length() > 0, "The PDF file should not be empty.");
    }

    @Test
    void testConvertMarkdownToPdf_WithBoldAndItalic() throws Exception {
        var content = "**Bold text** and *italic text* and ***both***";
        var markdownFile = new MockMultipartFile("file", "formatting.md", MediaType.TEXT_MARKDOWN_VALUE, content.getBytes());
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/formattingMarkdownOutput.pdf");
        markdownToPdfService.convertMarkdownToPdf(markdownFile, pdfFile);
        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        assertTrue(pdfFile.length() > 0, "The PDF file should not be empty.");
    }

    @Test
    void testConvertMarkdownToPdf_WithBlockquote() throws Exception {
        var content = "> This is a blockquote\n> With multiple lines";
        var markdownFile = new MockMultipartFile("file", "blockquote.md", MediaType.TEXT_MARKDOWN_VALUE, content.getBytes());
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/blockquoteMarkdownOutput.pdf");
        markdownToPdfService.convertMarkdownToPdf(markdownFile, pdfFile);
        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        assertTrue(pdfFile.length() > 0, "The PDF file should not be empty.");
    }
}
