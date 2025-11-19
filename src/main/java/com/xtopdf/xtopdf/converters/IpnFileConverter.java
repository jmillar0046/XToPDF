package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.IpnToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class IpnFileConverter implements FileConverter {
    private final IpnToPdfService ipnToPdfService;

    @Override
    public void convertToPDF(MultipartFile ipnFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            ipnToPdfService.convertIpnToPdf(ipnFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting Ipn to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
