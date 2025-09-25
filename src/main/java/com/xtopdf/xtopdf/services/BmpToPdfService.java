package com.xtopdf.xtopdf.services;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class BmpToPdfService {
    
    public void convertBmpToPdf(MultipartFile bmpFile, File pdfFile) throws IOException {
        try {
            // Read the BMP image
            BufferedImage bufferedImage = ImageIO.read(bmpFile.getInputStream());
            if (bufferedImage == null) {
                throw new IOException("Unable to read BMP image - invalid format or corrupted file");
            }
            
            // Convert BufferedImage to byte array in PNG format (for better PDF compatibility)
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "PNG", baos);
            byte[] imageBytes = baos.toByteArray();
            
            // Create PDF document using iText
            try (PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile))) {
                PdfDocument pdfDocument = new PdfDocument(writer);
                Document document = new Document(pdfDocument);
                
                // Create image from byte array
                Image image = new Image(ImageDataFactory.create(imageBytes));
                
                // Scale image to fit page if necessary
                image.setAutoScale(true);
                
                // Add image to PDF
                document.add(image);
                
                document.close();
            }
        } catch (IOException e) {
            throw new IOException("Error converting BMP to PDF: " + e.getMessage(), e);
        }
    }
}