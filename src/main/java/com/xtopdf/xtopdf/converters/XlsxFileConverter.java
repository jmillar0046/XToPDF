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
public class XlsxFileConverter extends AbstractFileConverter {
    private final ExcelToPdfService excelToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".xlsx");
    }

    @Override
    protected String getFormatName() {
        return "XLSX";
    }

    @Override
    protected void doConvert(MultipartFile inputFile, String outputFile) throws Exception {
        excelToPdfService.convertExcelToPdf(inputFile, new File(outputFile), false);
    }

    @Override
    public void convertToPDF(MultipartFile inputFile, String outputFile, boolean executeMacros)
            throws FileConversionException {
        if (inputFile == null) {
            throw new FileConversionException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new FileConversionException("Output file path must not be null");
        }
        try {
            excelToPdfService.convertExcelToPdf(inputFile, new File(outputFile), executeMacros);
        } catch (Exception e) {
            throw new FileConversionException(
                "Error converting " + getFormatName() + " to PDF: " + e.getMessage(), e);
        }
    }
}
