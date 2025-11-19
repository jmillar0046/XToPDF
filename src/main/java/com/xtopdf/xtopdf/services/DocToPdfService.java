package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Table;
import org.apache.poi.hwpf.usermodel.TableRow;
import org.apache.poi.hwpf.usermodel.TableCell;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
@Slf4j
public class DocToPdfService {
    
    private final PdfBackendProvider pdfBackend;
    
    public DocToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }
    
    public void convertDocToPdf(MultipartFile docFile, File pdfFile) throws IOException {
        try (var fis = docFile.getInputStream();
             HWPFDocument docDocument = new HWPFDocument(fis);
             PdfDocumentBuilder builder = pdfBackend.createBuilder()) {
            
            Range range = docDocument.getRange();
            
            for (int i = 0; i < range.numParagraphs(); i++) {
                Paragraph para = range.getParagraph(i);
                String text = para.text();
                if (text != null && !text.trim().isEmpty()) {
                    builder.addParagraph(text);
                }
            }
            
            builder.save(pdfFile);
        } catch (Exception e) {
            log.error("Error processing DOC file: {}", e.getMessage(), e);
            throw new IOException("Error processing DOC file: " + e.getMessage());
        }
    }
}
