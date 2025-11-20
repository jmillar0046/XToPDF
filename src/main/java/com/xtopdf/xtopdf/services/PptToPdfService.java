package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTextParagraph;
import org.apache.poi.hslf.usermodel.HSLFTextShape;
import org.apache.poi.hslf.usermodel.HSLFShape;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class PptToPdfService {
    
    private final PdfBackendProvider pdfBackend;
    
    public PptToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }
    
    public void convertPptToPdf(MultipartFile pptFile, File pdfFile) throws IOException {
        if (pptFile == null) {
            throw new IOException("Input file must not be null");
        }
        if (pdfFile == null) {
            throw new IOException("Output file must not be null");
        }
        
        try (var inputStream = pptFile.getInputStream();
             HSLFSlideShow pptDocument = new HSLFSlideShow(inputStream);
             PdfDocumentBuilder builder = pdfBackend.createBuilder()) {
            
            int slideNumber = 1;
            for (HSLFSlide slide : pptDocument.getSlides()) {
                builder.addParagraph("Slide " + slideNumber + "\n");
                
                for (HSLFShape shape : slide.getShapes()) {
                    if (shape instanceof HSLFTextShape) {
                        HSLFTextShape textShape = (HSLFTextShape) shape;
                        String text = textShape.getText();
                        if (text != null && !text.trim().isEmpty()) {
                            builder.addParagraph(text);
                        }
                    }
                }
                
                builder.addParagraph("\n");
                slideNumber++;
            }
            
            builder.save(pdfFile);
        } catch (Exception e) {
            throw new IOException("Error processing PPT file: " + e.getMessage(), e);
        }
    }
}
