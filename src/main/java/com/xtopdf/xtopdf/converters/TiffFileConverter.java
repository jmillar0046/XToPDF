package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.TiffToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class TiffFileConverter implements FileConverter {
    private final TiffToPdfService tiffToPdfService;

    @Override
    public void convertToPDF(MultipartFile tiffFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            tiffToPdfService.convertTiffToPdf(tiffFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting TIFF to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
    
}