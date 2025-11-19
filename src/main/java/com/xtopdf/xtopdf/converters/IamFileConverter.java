package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.IamToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class IamFileConverter implements FileConverter {
    private final IamToPdfService iamToPdfService;

    @Override
    public void convertToPDF(MultipartFile iamFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            iamToPdfService.convertIamToPdf(iamFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting Iam to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
