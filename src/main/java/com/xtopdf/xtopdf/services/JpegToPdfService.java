package com.xtopdf.xtopdf.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.io.image.ImageDataFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class JpegToPdfService {
    public void convertJpegToPdf(MultipartFile jpegFile, File pdfFile) throws IOException {
        try {
            // Create PDF writer and document
            try (PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile))) {
                PdfDocument pdfDocument = new PdfDocument(writer);
                Document document = new Document(pdfDocument);
                
                // Create image from JPEG input stream
                byte[] imageBytes = jpegFile.getBytes();
                Image image = new Image(ImageDataFactory.create(imageBytes));
                
                // Scale image to fit page if needed
                // Get page size and image dimensions
                float pageWidth = pdfDocument.getDefaultPageSize().getWidth() - 72; // 36pt margins on each side
                float pageHeight = pdfDocument.getDefaultPageSize().getHeight() - 72; // 36pt margins on each side
                
                // Calculate scaling to fit page while maintaining aspect ratio
                float imageWidth = image.getImageWidth();
                float imageHeight = image.getImageHeight();
                
                if (imageWidth > pageWidth || imageHeight > pageHeight) {
                    float scaleX = pageWidth / imageWidth;
                    float scaleY = pageHeight / imageHeight;
                    float scale = Math.min(scaleX, scaleY);
                    image.scale(scale, scale);
                }
                
                // Add image to document
                document.add(image);
                document.close();
            }
        } catch (Exception e) {
            throw new IOException("Error converting JPEG to PDF: " + e.getMessage(), e);
        }
    }
}