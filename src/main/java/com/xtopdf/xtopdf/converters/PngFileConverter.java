package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.conversion.image.PngToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class PngFileConverter extends AbstractFileConverter {
    private final PngToPdfService pngToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".png");
    }

    @Override
    protected String getFormatName() {
        return "PNG";
    }

    @Override
    protected void doConvert(MultipartFile inputFile, String outputFile) throws Exception {
        pngToPdfService.convertPngToPdf(inputFile, new File(outputFile));
    }
}
