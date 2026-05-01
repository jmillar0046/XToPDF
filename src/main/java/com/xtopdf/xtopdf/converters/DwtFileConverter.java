package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.conversion.cad.DwtToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class DwtFileConverter implements FileConverter {
    private final DwtToPdfService dwtToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".dwt");
    }

    @Override
    public void convertToPDF(MultipartFile dwtFile, String outputFile) throws FileConversionException {
        var pdfFile = new File(outputFile);
        try {
            dwtToPdfService.convertDwtToPdf(dwtFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting Dwt to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
