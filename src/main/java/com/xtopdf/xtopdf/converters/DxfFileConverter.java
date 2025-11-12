package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.DxfToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class DxfFileConverter implements FileConverter {
    private final DxfToPdfService dxfToPdfService;

    @Override
    public void convertToPDF(MultipartFile dxfFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting DXF to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
