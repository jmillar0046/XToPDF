package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.conversion.spreadsheet.DelimiterSeparatedToPdfService;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

/**
 * Unified converter for delimiter-separated files (CSV, TSV, etc.).
 * Parameterized by delimiter character, format name, and supported extensions.
 * Two instances are registered as Spring beans via DelimiterConverterConfig:
 * one for CSV (comma) and one for TSV (tab).
 */
public class DelimiterSeparatedConverter extends AbstractFileConverter {

    private final DelimiterSeparatedToPdfService toPdfService;
    private final char delimiter;
    private final String formatName;
    private final Set<String> supportedExtensions;

    public DelimiterSeparatedConverter(
            DelimiterSeparatedToPdfService toPdfService,
            char delimiter,
            String formatName,
            Set<String> supportedExtensions) {
        this.toPdfService = toPdfService;
        this.delimiter = delimiter;
        this.formatName = formatName;
        this.supportedExtensions = Set.copyOf(supportedExtensions);
    }

    @Override
    public Set<String> getSupportedExtensions() {
        return supportedExtensions;
    }

    @Override
    protected String getFormatName() {
        return formatName;
    }

    @Override
    protected void doConvert(MultipartFile inputFile, String outputFile) throws Exception {
        toPdfService.convertDelimiterSeparatedToPdf(inputFile, new File(outputFile), delimiter);
    }
}
