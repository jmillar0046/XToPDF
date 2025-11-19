package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.IptToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class IptFileConverter implements FileConverter {
    private final IptToPdfService iptToPdfService;

    @Override
    public void convertToPDF(MultipartFile iptFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            iptToPdfService.convertIptToPdf(iptFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting Ipt to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
