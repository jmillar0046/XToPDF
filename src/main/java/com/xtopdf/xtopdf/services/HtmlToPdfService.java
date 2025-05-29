package com.xtopdf.xtopdf.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import com.itextpdf.html2pdf.HtmlConverter;

@Service
public class HtmlToPdfService {
    public void convertHtmlToPdf(MultipartFile htmlFile, File pdfFile) {
        try (var outputStream = new FileOutputStream(pdfFile)) {
            HtmlConverter.convertToPdf(htmlFile.getInputStream(), outputStream);
            System.out.println("PDF created successfully!");
        } catch (IOException e) {
            System.err.println("Error during conversion: " + e.getMessage());
        }
    }
}
