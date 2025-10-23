package com.xtopdf.xtopdf.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.io.font.constants.StandardFonts;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PptxToPdfService {
    
    public void convertPptxToPdf(MultipartFile pptxFile, File pdfFile) throws IOException {
        if (pptxFile == null) {
            throw new NullPointerException("Input file must not be null");
        }
        if (pdfFile == null) {
            throw new IOException("Output file must not be null");
        }
        
        try (var inputStream = pptxFile.getInputStream();
             XMLSlideShow pptxDocument = new XMLSlideShow(inputStream);
             PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile))) {
            
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document pdfDoc = new Document(pdfDocument);

            // Process each slide
            int slideNumber = 1;
            for (XSLFSlide slide : pptxDocument.getSlides()) {
                // Add slide title
                Paragraph slideTitle = new Paragraph();
                Text titleText = new Text("Slide " + slideNumber);
                titleText.setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD));
                titleText.setFontSize(16);
                slideTitle.add(titleText);
                pdfDoc.add(slideTitle);
                
                // Process shapes in the slide
                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape) {
                        processTextShape((XSLFTextShape) shape, pdfDoc);
                    }
                }
                
                // Add some spacing after each slide
                pdfDoc.add(new Paragraph("\n"));
                slideNumber++;
            }

            pdfDoc.close();
        } catch (Exception e) {
            throw new IOException("Error processing PPTX file: " + e.getMessage(), e);
        }
    }

    private void processTextShape(XSLFTextShape textShape, Document pdfDoc) throws IOException {
        try {
            for (XSLFTextParagraph paragraph : textShape.getTextParagraphs()) {
                Paragraph pdfParagraph = new Paragraph();
                
                for (XSLFTextRun run : paragraph.getTextRuns()) {
                    String text = run.getRawText();
                    if (text != null && !text.trim().isEmpty()) {
                        Text pdfText = new Text(text);
                        
                        // Apply formatting if available
                        if (run.isBold() && run.isItalic()) {
                            pdfText.setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLDOBLIQUE));
                        } else if (run.isBold()) {
                            pdfText.setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD));
                        } else if (run.isItalic()) {
                            pdfText.setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE));
                        }
                        
                        // Set font size if available
                        Double fontSize = run.getFontSize();
                        if (fontSize != null && fontSize > 0) {
                            pdfText.setFontSize(fontSize.floatValue());
                        }
                        
                        pdfParagraph.add(pdfText);
                    }
                }
                
                if (!pdfParagraph.isEmpty()) {
                    pdfDoc.add(pdfParagraph);
                }
            }
        } catch (Exception e) {
            // If there's an error processing the text shape, add a simple fallback
            pdfDoc.add(new Paragraph("[Text content could not be processed]"));
        }
    }
}