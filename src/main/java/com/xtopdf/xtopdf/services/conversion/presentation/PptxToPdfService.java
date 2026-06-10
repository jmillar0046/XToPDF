package com.xtopdf.xtopdf.services.conversion.presentation;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xslf.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Slf4j
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

        try (var is = pptxFile.getInputStream();
             XMLSlideShow pptx = new XMLSlideShow(is);
             PdfDocumentBuilder builder = pdfBackend.createBuilder()) {

            Dimension pageSize = pptx.getPageSize();
            float pageWidth = pageSize.width;
            float pageHeight = pageSize.height;
            List<XSLFSlide> slides = pptx.getSlides();

            if (slides.isEmpty()) {
                builder.addParagraph("This presentation contains no slides.");
                builder.save(pdfFile);
                return;
            }

            for (XSLFSlide slide : slides) {
                builder.newPage(pageWidth, pageHeight);
                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape textShape) {
                        renderTextShape(builder, textShape);
                    } else if (shape instanceof XSLFPictureShape picShape) {
                        renderPictureShape(builder, picShape);
                    }
                }
            }
            builder.save(pdfFile);
        }
    }

    public void renderTextShape(PdfDocumentBuilder builder, XSLFTextShape textShape) throws IOException {
        for (XSLFTextParagraph para : textShape.getTextParagraphs()) {
            for (XSLFTextRun run : para.getTextRuns()) {
                String text = run.getRawText();
                if (text != null && !text.isEmpty()) {
                    double fontSize = run.getFontSize() != null ? run.getFontSize() : 12.0;
                    builder.addFormattedText(text, run.isBold(), run.isItalic(), (float) fontSize);
                }
            }
            builder.endParagraph();
        }
    }

    public void renderPictureShape(PdfDocumentBuilder builder, XSLFPictureShape picShape) throws IOException {
        try {
            byte[] imageData = picShape.getPictureData().getData();
            builder.addImage(imageData);
        } catch (Exception e) {
            log.debug("Failed to render picture shape, skipping");
        }
    }
}
