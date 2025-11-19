package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class PptxToPdfService {
    
    private final PdfBackendProvider pdfBackend;
    
    public PptxToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }
    
    public void convertPptxToPdf(MultipartFile pptxFile, File pdfFile) throws IOException {
        if (pptxFile == null) {
            throw new NullPointerException("Input file must not be null");
        }
        if (pdfFile == null) {
            throw new IOException("Output file must not be null");
        }
        
        try (var inputStream = pptxFile.getInputStream();
             XMLSlideShow pptxDocument = new XMLSlideShow(inputStream);
             PdfDocumentBuilder builder = pdfBackend.createBuilder()) {
            
            int slideNumber = 1;
            for (XSLFSlide slide : pptxDocument.getSlides()) {
                builder.addParagraph("Slide " + slideNumber + "\n");
                
                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape textShape) {
                        for (XSLFTextParagraph paragraph : textShape.getTextParagraphs()) {
                            String text = paragraph.getText();
                            if (text != null && !text.trim().isEmpty()) {
                                builder.addParagraph(text);
                            }
                        }
                    }
                }
                
                builder.addParagraph("\n");
                slideNumber++;
            }
            
            builder.save(pdfFile);
        } catch (Exception e) {
            throw new IOException("Error processing PPTX file: " + e.getMessage(), e);
        }
    }
}
