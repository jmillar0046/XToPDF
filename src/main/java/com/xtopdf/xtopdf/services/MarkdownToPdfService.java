package com.xtopdf.xtopdf.services;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.itextpdf.html2pdf.HtmlConverter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class MarkdownToPdfService {
    public void convertMarkdownToPdf(MultipartFile markdownFile, File pdfFile) throws IOException {
        // Read the markdown file content
        StringBuilder markdownContent = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(markdownFile.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                markdownContent.append(line).append("\n");
            }
        }

        // Parse markdown to HTML
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdownContent.toString());
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String htmlContent = renderer.render(document);

        // Add basic HTML structure for better PDF rendering
        String fullHtml = """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"></head>
                <body>%s</body>
                </html>
                """.formatted(htmlContent);

        // Convert HTML to PDF using iText
        try (FileOutputStream outputStream = new FileOutputStream(pdfFile)) {
            HtmlConverter.convertToPdf(fullHtml, outputStream);
        } catch (Exception e) {
            throw new IOException("Error creating PDF from markdown: " + e.getMessage(), e);
        }
    }
}
