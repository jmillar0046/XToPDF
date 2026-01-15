package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import lombok.extern.slf4j.Slf4j;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import lombok.extern.slf4j.Slf4j;
import org.commonmark.node.Node;
import lombok.extern.slf4j.Slf4j;
import org.commonmark.parser.Parser;
import lombok.extern.slf4j.Slf4j;
import org.commonmark.renderer.text.TextContentRenderer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import lombok.extern.slf4j.Slf4j;
import java.io.File;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import java.io.InputStreamReader;
import lombok.extern.slf4j.Slf4j;

/**
 * Service to convert Markdown files to PDF.
 * Parses Markdown and renders as plain text to PDF.
 * Uses the PDF backend abstraction layer with Apache PDFBox.
 * 
 * Note: This implementation renders Markdown as plain text.
 * For HTML-based rendering with better formatting, consider using
 * a specialized HTML-to-PDF library.
 */
@Slf4j
@Service
public class MarkdownToPdfService {
    
    private final PdfBackendProvider pdfBackend;
    
    public MarkdownToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }
    
    public void convertMarkdownToPdf(MultipartFile markdownFile, File pdfFile) throws IOException {
        // Read the markdown file content
        StringBuilder markdownContent = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(markdownFile.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                markdownContent.append(line).append("\n");
            }
        }

        // Parse markdown and render as plain text
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdownContent.toString());
        TextContentRenderer renderer = TextContentRenderer.builder().build();
        String textContent = renderer.render(document);

        // Create PDF using abstraction layer (PDFBox backend)
        try (PdfDocumentBuilder builder = pdfBackend.createBuilder()) {
            builder.addParagraph(textContent);
            builder.save(pdfFile);
        } catch (Exception e) {
            throw new IOException("Error creating PDF from markdown: " + e.getMessage(), e);
        }
    }
}
