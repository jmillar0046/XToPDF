package com.xtopdf.xtopdf.services;

import java.io.File;
import java.io.IOException;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import lombok.extern.slf4j.Slf4j;
import org.odftoolkit.odfdom.doc.OdfPresentationDocument;
import org.odftoolkit.odfdom.doc.presentation.OdfSlide;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class OdpToPdfService {
    public void convertOdpToPdf(MultipartFile odpFile, File pdfFile) throws IOException {
        try (var fis = odpFile.getInputStream();
             PdfWriter writer = new PdfWriter(pdfFile)) {
            
            OdfPresentationDocument odpDocument = OdfPresentationDocument.loadDocument(fis);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document pdfDoc = new Document(pdfDocument);
            
            // Get all slides
            java.util.Iterator<OdfSlide> slideIterator = odpDocument.getSlides();
            
            int slideIndex = 0;
            while (slideIterator.hasNext()) {
                OdfSlide slide = slideIterator.next();
                
                if (slideIndex > 0) {
                    pdfDoc.add(new Paragraph("\n--- Slide " + (slideIndex + 1) + " ---\n"));
                } else {
                    pdfDoc.add(new Paragraph("--- Slide " + (slideIndex + 1) + " ---\n"));
                }
                
                // Extract text content from the slide
                String slideText = slide.getOdfElement().getTextContent();
                if (slideText != null && !slideText.isEmpty()) {
                    pdfDoc.add(new Paragraph(slideText));
                }
                
                slideIndex++;
            }
            
            pdfDoc.close();
        } catch (Exception e) {
            log.error("Error processing ODP file: {}", e.getMessage(), e);
            throw new IOException("Error processing ODP file: " + e.getMessage(), e);
        }
    }
}
