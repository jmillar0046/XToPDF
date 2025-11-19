package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.WmfToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class WmfFileConverter implements FileConverter {
    private final WmfToPdfService wmfToPdfService;

    @Override
    public void convertToPDF(MultipartFile wmfFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            wmfToPdfService.convertWmfToPdf(wmfFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting Wmf to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
