package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import com.xtopdf.xtopdf.services.conversion.spreadsheet.XlsxToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Set;

@AllArgsConstructor
@Component
public class XlsxFileConverter implements FileConverter {
    private final XlsxToPdfService xlsxToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".xlsx");
    }

    @Override
    public void convertToPDF(MultipartFile xlsxFile, String outputFile) throws FileConversionException {
        convertToPDF(xlsxFile, outputFile, false);
    }
    
    @Override
    public void convertToPDF(MultipartFile xlsxFile, String outputFile, boolean executeMacros) throws FileConversionException {
        var pdfFile = new File(outputFile);
        try {
            xlsxToPdfService.convertXlsxToPdf(xlsxFile, pdfFile, executeMacros);
        } catch (IOException e) {
            throw new RuntimeException("Error converting XLSX to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}