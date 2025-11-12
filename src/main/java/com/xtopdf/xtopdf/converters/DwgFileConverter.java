package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.DwgToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class DwgFileConverter implements FileConverter {
    private final DwgToPdfService dwgToPdfService;

    @Override
    public void convertToPDF(MultipartFile dwgFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            dwgToPdfService.convertDwgToPdf(dwgFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting DWG to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
