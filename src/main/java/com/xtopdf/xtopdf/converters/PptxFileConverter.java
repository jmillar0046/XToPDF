package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.conversion.presentation.PptxToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class PptxFileConverter extends AbstractFileConverter {
    private final PptxToPdfService pptxToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".pptx");
    }

    @Override
    protected String getFormatName() {
        return "PPTX";
    }

    @Override
    protected void doConvert(MultipartFile inputFile, String outputFile) throws Exception {
        pptxToPdfService.convertPptxToPdf(inputFile, new File(outputFile));
    }
}
