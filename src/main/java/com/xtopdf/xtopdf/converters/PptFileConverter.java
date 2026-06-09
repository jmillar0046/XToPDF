package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.conversion.presentation.PptToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class PptFileConverter extends AbstractFileConverter {
    private final PptToPdfService pptToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".ppt");
    }

    @Override
    protected String getFormatName() {
        return "PPT";
    }

    @Override
    protected void doConvert(MultipartFile inputFile, String outputFile) throws Exception {
        pptToPdfService.convertPptToPdf(inputFile, new File(outputFile));
    }
}
