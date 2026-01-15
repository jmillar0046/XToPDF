package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import lombok.extern.slf4j.Slf4j;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hslf.usermodel.HSLFTextParagraph;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hslf.usermodel.HSLFTextShape;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hslf.usermodel.HSLFShape;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
