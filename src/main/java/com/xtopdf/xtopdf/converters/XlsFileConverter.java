package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;
import com.xtopdf.xtopdf.services.conversion.spreadsheet.ExcelToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class XlsFileConverter implements FileConverter {
    private final ExcelToPdfService excelToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".xls");
    }

    @Override
    public void convertToPDF(MultipartFile xlsFile, String outputFile) throws FileConversionException {
        convertToPDF(xlsFile, outputFile, false);
    }

    @Override
    public void convertToPDF(MultipartFile xlsFile, String outputFile, boolean executeMacros) throws FileConversionException {
        if (xlsFile == null) {
            throw new FileConversionException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new FileConversionException("Output file path must not be null");
        }
        try {
            var pdfFile = new File(outputFile);
            excelToPdfService.convertExcelToPdf(xlsFile, pdfFile, executeMacros);
        } catch (Exception e) {
            throw new FileConversionException("Error converting XLS to PDF: " + e.getMessage(), e);
        }
    }
}
