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
public class PngToPdfService {
    public void convertPngToPdf(MultipartFile pngFile, File pdfFile) throws IOException {
        if (pngFile == null) {
            throw new NullPointerException("PNG file cannot be null");
        }
        if (pdfFile == null) {
            throw new NullPointerException("Output PDF file cannot be null");
        }
        
        // Create a PDF document using iText
        try (PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile))) {
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);
            
            // Create image from PNG file
            byte[] imageBytes = pngFile.getBytes();
            if (imageBytes == null || imageBytes.length == 0) {
                throw new IOException("PNG file is empty or invalid");
            }
            
            Image image = new Image(ImageDataFactory.create(imageBytes));
            
            // Scale image to fit page if necessary
            image.setAutoScale(true);
            
            // Add the image to the PDF
            document.add(image);
            
            document.close();
        } catch (Exception e) {
            throw new IOException("Error creating PDF from PNG: " + e.getMessage(), e);
        }
    }
}