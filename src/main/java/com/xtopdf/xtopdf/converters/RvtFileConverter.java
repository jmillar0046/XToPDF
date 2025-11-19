package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.RvtToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class RvtFileConverter implements FileConverter {
    private final RvtToPdfService rvtToPdfService;

    @Override
    public void convertToPDF(MultipartFile rvtFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            rvtToPdfService.convertRvtToPdf(rvtFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting Rvt to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
