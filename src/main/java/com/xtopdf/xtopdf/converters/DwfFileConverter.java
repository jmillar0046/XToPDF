package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.conversion.cad.DwfToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class DwfFileConverter implements FileConverter {
    private final DwfToPdfService dwfToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".dwf");
    }

    @Override
    public void convertToPDF(MultipartFile dwfFile, String outputFile) throws FileConversionException {
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
