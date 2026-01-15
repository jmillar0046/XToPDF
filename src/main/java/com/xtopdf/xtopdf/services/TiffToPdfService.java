package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import lombok.extern.slf4j.Slf4j;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import java.awt.image.BufferedImage;
import lombok.extern.slf4j.Slf4j;
import java.io.ByteArrayOutputStream;
import lombok.extern.slf4j.Slf4j;
import java.io.File;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for converting TIFF files to PDF.
 * Uses the PDF backend abstraction layer with Apache PDFBox.
 */
@Slf4j
@Service
public class TiffToPdfService {
    
    private final PdfBackendProvider pdfBackend;
    
    public TiffToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }
    
    public void convertTiffToPdf(MultipartFile tiffFile, File pdfFile) throws IOException {
        try (var inputStream = tiffFile.getInputStream()) {
            // Read the TIFF image
            BufferedImage bufferedImage = ImageIO.read(inputStream);
            
            if (bufferedImage == null) {
                throw new IOException("Unable to read TIFF image. The file may be corrupted or not a valid TIFF format.");
            }
            
            // Convert BufferedImage to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            
            // Create PDF using abstraction layer (PDFBox backend)
            try (PdfDocumentBuilder builder = pdfBackend.createBuilder()) {
                builder.addImage(imageBytes);
                builder.save(pdfFile);
            }
        } catch (IOException e) {
            throw new IOException("Error processing TIFF file: " + e.getMessage(), e);
        }
    }
}