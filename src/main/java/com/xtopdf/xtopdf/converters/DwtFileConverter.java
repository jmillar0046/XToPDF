package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.conversion.cad.DwtToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class DwtFileConverter extends AbstractFileConverter {
    private final DwtToPdfService dwtToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".dwt");
    }

    @Override
    protected String getFormatName() {
        return "DWT";
    }

    @Override
    protected void doConvert(MultipartFile inputFile, String outputFile) throws Exception {
        dwtToPdfService.convertDwtToPdf(inputFile, new File(outputFile));
    }
}
