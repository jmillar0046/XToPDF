package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Service for converting BMP files to PDF.
 * Uses the PDF backend abstraction layer with Apache PDFBox.
 */
@Service
public class BmpToPdfService {
    
    private final PdfBackendProvider pdfBackend;
    
    public BmpToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }
    
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
            
            // Create PDF using abstraction layer (PDFBox backend)
            try (PdfDocumentBuilder builder = pdfBackend.createBuilder()) {
                builder.addImage(imageBytes);
                builder.save(pdfFile);
            }
        } catch (IOException e) {
            throw new IOException("Error converting BMP to PDF: " + e.getMessage(), e);
        }
    }
}