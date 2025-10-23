package com.xtopdf.xtopdf.services;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.layout.Document;
import com.itextpdf.svg.converter.SvgConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class SvgToPdfService {
    public void convertSvgToPdf(MultipartFile svgFile, File pdfFile) throws IOException {
        try (PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile))) {
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);
            
            // Add a new page to the document
            PdfPage page = pdfDocument.addNewPage();
            
            // Convert SVG to PDF using iText SVG converter
            // Draw the SVG on the page
            SvgConverter.drawOnPage(svgFile.getInputStream(), page);
            
            document.close();
        } catch (Exception e) {
            throw new IOException("Error creating PDF from SVG: " + e.getMessage(), e);
        }
    }
}