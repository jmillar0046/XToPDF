package com.xtopdf.xtopdf.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import com.itextpdf.html2pdf.HtmlConverter;

@Service
@Slf4j
public class HtmlToPdfService {
    public void convertHtmlToPdf(MultipartFile htmlFile, File pdfFile) {
        try (var outputStream = new FileOutputStream(pdfFile)) {
            HtmlConverter.convertToPdf(htmlFile.getInputStream(), outputStream);
            log.info("PDF created successfully from HTML: {}", pdfFile.getName());
        } catch (IOException e) {
            log.error("Error during HTML to PDF conversion: {}", e.getMessage(), e);
        }
    }
}
