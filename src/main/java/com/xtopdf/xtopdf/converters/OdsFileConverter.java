package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.conversion.spreadsheet.OdsToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class OdsFileConverter extends AbstractFileConverter {
    private final OdsToPdfService odsToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".ods");
    }

    @Override
    protected String getFormatName() {
        return "ODS";
    }

    @Override
    protected void doConvert(MultipartFile inputFile, String outputFile) throws Exception {
        odsToPdfService.convertOdsToPdf(inputFile, new File(outputFile));
    }
}
