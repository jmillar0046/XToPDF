package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.ThreeMfToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class ThreeMfFileConverter implements FileConverter {
    private final ThreeMfToPdfService threemfToPdfService;

    @Override
    public void convertToPDF(MultipartFile threemfFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            threemfToPdfService.convert3mfToPdf(threemfFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting ThreeMf to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
