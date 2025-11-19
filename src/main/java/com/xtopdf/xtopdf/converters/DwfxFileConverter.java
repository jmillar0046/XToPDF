package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.DwfxToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class DwfxFileConverter implements FileConverter {
    private final DwfxToPdfService dwfxToPdfService;

    @Override
    public void convertToPDF(MultipartFile dwfxFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            dwfxToPdfService.convertDwfxToPdf(dwfxFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting Dwfx to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
