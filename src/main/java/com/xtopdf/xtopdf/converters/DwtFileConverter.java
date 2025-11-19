package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.DwtToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class DwtFileConverter implements FileConverter {
    private final DwtToPdfService dwtToPdfService;

    @Override
    public void convertToPDF(MultipartFile dwtFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            dwtToPdfService.convertDwtToPdf(dwtFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting Dwt to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
