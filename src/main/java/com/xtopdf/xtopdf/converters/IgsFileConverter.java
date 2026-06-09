package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.conversion.threed.IgsToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class IgsFileConverter extends AbstractFileConverter {
    private final IgsToPdfService igsToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".igs");
    }

    @Override
    protected String getFormatName() {
        return "IGS";
    }

    @Override
    protected void doConvert(MultipartFile inputFile, String outputFile) throws Exception {
        igsToPdfService.convertIgsToPdf(inputFile, new File(outputFile));
    }
}
