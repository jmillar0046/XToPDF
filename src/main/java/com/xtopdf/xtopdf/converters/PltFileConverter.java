package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.conversion.cad.PltToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class PltFileConverter extends AbstractFileConverter {
    private final PltToPdfService pltToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".plt");
    }

    @Override
    protected String getFormatName() {
        return "PLT";
    }

    @Override
    protected void doConvert(MultipartFile inputFile, String outputFile) throws Exception {
        pltToPdfService.convertPltToPdf(inputFile, new File(outputFile));
    }
}
