package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.conversion.threed.StepToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class StepFileConverter extends AbstractFileConverter {
    private final StepToPdfService stepToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".step");
    }

    @Override
    protected String getFormatName() {
        return "STEP";
    }

    @Override
    protected void doConvert(MultipartFile inputFile, String outputFile) throws Exception {
        stepToPdfService.convertStepToPdf(inputFile, new File(outputFile));
    }
}
