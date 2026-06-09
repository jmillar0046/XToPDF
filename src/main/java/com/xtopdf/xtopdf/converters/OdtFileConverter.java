package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.conversion.document.OdtToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class OdtFileConverter extends AbstractFileConverter {
    private final OdtToPdfService odtToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".odt");
    }

    @Override
    protected String getFormatName() {
        return "ODT";
    }

    @Override
    protected void doConvert(MultipartFile inputFile, String outputFile) throws Exception {
        odtToPdfService.convertOdtToPdf(inputFile, new File(outputFile));
    }
}
