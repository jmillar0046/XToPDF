package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.F3dToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class F3dFileConverter implements FileConverter {
    private final F3dToPdfService f3dToPdfService;

    @Override
    public void convertToPDF(MultipartFile f3dFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            f3dToPdfService.convertF3dToPdf(f3dFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting F3d to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
