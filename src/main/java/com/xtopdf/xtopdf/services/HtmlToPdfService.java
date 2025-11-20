package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Service to convert HTML files to PDF.
 * 
 * Note: This implementation extracts text content from HTML using Jsoup.
 * For full HTML rendering with CSS and images, consider using external tools like:
 * - wkhtmltopdf (command-line tool)
 * - Playwright/Puppeteer (headless browser)
 * - Apache FOP with XHTML
 * 
 * TODO: For production-grade HTML rendering, integrate with external rendering engine.
 */
@Service
@Slf4j
public class HtmlToPdfService {
    
    private final PdfBackendProvider pdfBackend;
    
    @Autowired
    public HtmlToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }
    
    public void convertHtmlToPdf(MultipartFile htmlFile, File pdfFile) {
        try {
            // Parse HTML and extract text content
            String htmlContent = new String(htmlFile.getBytes(), StandardCharsets.UTF_8);
            Document doc = Jsoup.parse(htmlContent);
            
            // Extract text (preserves paragraphs and spacing)
            String textContent = doc.body().text();
            String title = doc.title();
            
            // Create PDF using abstraction layer
            try (PdfDocumentBuilder builder = pdfBackend.createBuilder()) {
                if (title != null && !title.isEmpty()) {
                    builder.addParagraph(title + "\n\n");
                }
                
                builder.addParagraph(textContent);
                builder.save(pdfFile);
            }
            
            log.info("PDF created successfully from HTML: {}", pdfFile.getName());
        } catch (IOException e) {
            log.error("Error during HTML to PDF conversion: {}", e.getMessage(), e);
        }
    }
}
