package com.xtopdf.xtopdf.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.io.image.ImageDataFactory;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

@Service
public class TiffToPdfService {
    
    public void convertTiffToPdf(MultipartFile tiffFile, File pdfFile) throws IOException {
        try (var inputStream = tiffFile.getInputStream()) {
            // Read the TIFF image
            BufferedImage bufferedImage = ImageIO.read(inputStream);
            
            if (bufferedImage == null) {
                throw new IOException("Unable to read TIFF image. The file may be corrupted or not a valid TIFF format.");
            }
            
            // Convert BufferedImage to byte array for iText
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            
            // Create PDF document using iText
            try (PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile))) {
                PdfDocument pdfDocument = new PdfDocument(writer);
                Document document = new Document(pdfDocument);
                
                // Create Image object from byte array
                Image pdfImage = new Image(ImageDataFactory.create(imageBytes));
                
                // Scale image to fit page if necessary
                float pageWidth = pdfDocument.getDefaultPageSize().getWidth() - 50; // 25pt margin on each side
                float pageHeight = pdfDocument.getDefaultPageSize().getHeight() - 50; // 25pt margin on top and bottom
                
                if (pdfImage.getImageWidth() > pageWidth || pdfImage.getImageHeight() > pageHeight) {
                    pdfImage.scaleToFit(pageWidth, pageHeight);
                }
                
                // Add the image to the PDF
                document.add(pdfImage);
                
                document.close();
            } catch (Exception e) {
                throw new IOException("Error creating PDF from TIFF: " + e.getMessage(), e);
            }
        } catch (IOException e) {
            throw new IOException("Error processing TIFF file: " + e.getMessage(), e);
        }
    }
}