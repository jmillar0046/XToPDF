package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;
import com.xtopdf.xtopdf.services.conversion.spreadsheet.ExcelToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@AllArgsConstructor
@Component
public class XlsxFileConverter implements FileConverter {
    private final ExcelToPdfService excelToPdfService;

    @Override
    public void convertToPDF(MultipartFile xlsxFile, String outputFile) throws FileConversionException {
        convertToPDF(xlsxFile, outputFile, false);
    }

    @Override
    public void convertToPDF(MultipartFile xlsxFile, String outputFile, boolean executeMacros) throws FileConversionException {
        var pdfFile = new File(outputFile);
        try {
            excelToPdfService.convertExcelToPdf(xlsxFile, pdfFile, executeMacros);
        } catch (IOException e) {
            throw new FileConversionException("Error converting XLSX to PDF: " + e.getMessage(), e);
        }
    }
}
