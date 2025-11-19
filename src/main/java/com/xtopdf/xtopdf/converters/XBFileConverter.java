package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.XBToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class XBFileConverter implements FileConverter {
    private final XBToPdfService xbToPdfService;

    @Override
    public void convertToPDF(MultipartFile xbFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            xbToPdfService.convertXBToPdf(xbFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting XB to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
