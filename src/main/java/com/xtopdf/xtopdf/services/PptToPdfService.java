package com.xtopdf.xtopdf.services;

import java.io.File;
import java.io.IOException;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTextParagraph;
import org.apache.poi.hslf.usermodel.HSLFTextShape;
import org.apache.poi.sl.usermodel.Shape;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class PptToPdfService {
    public void convertPptToPdf(MultipartFile pptFile, File pdfFile) throws IOException {
        try (var fis = pptFile.getInputStream();
             HSLFSlideShow pptShow = new HSLFSlideShow(fis);
             PdfWriter writer = new PdfWriter(pdfFile)) {
            
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document pdfDoc = new Document(pdfDocument);
            
            // Process each slide
            for (HSLFSlide slide : pptShow.getSlides()) {
                // Add slide title
                String title = slide.getTitle();
                if (title != null && !title.isEmpty()) {
                    Paragraph titlePara = new Paragraph(title);
                    try {
                        titlePara.setFont(com.itextpdf.kernel.font.PdfFontFactory.createFont(
                            com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD));
                        titlePara.setFontSize(14);
                    } catch (Exception e) {
                        log.warn("Could not set font for title", e);
                    }
                    pdfDoc.add(titlePara);
                }
                
                // Extract text from shapes
                for (Shape<?,?> shape : slide.getShapes()) {
                    if (shape instanceof HSLFTextShape) {
                        HSLFTextShape textShape = (HSLFTextShape) shape;
                        String text = textShape.getText();
                        if (text != null && !text.isEmpty()) {
                            pdfDoc.add(new Paragraph(text));
                        }
                    }
                }
                
                // Add spacing between slides
                pdfDoc.add(new Paragraph("\n"));
            }
            
            pdfDoc.close();
        } catch (Exception e) {
            log.error("Error processing PPT file: {}", e.getMessage(), e);
            throw new IOException("Error processing PPT file: " + e.getMessage(), e);
        }
    }
}
