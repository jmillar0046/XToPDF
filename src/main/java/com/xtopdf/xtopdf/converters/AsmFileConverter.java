package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.AsmToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class AsmFileConverter implements FileConverter {
    private final AsmToPdfService asmToPdfService;

    @Override
    public void convertToPDF(MultipartFile asmFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            asmToPdfService.convertAsmToPdf(asmFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting Asm to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
