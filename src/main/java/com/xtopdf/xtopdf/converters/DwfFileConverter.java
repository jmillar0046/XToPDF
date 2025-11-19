package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.DwfToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class DwfFileConverter implements FileConverter {
    private final DwfToPdfService dwfToPdfService;

    @Override
    public void convertToPDF(MultipartFile dwfFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            dwfToPdfService.convertDwfToPdf(dwfFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting Dwf to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
