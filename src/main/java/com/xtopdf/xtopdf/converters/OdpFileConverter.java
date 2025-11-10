package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.OdpToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class OdpFileConverter implements FileConverter {
    private final OdpToPdfService odpToPdfService;

    @Override
    public void convertToPDF(MultipartFile odpFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            odpToPdfService.convertOdpToPdf(odpFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting ODP to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
