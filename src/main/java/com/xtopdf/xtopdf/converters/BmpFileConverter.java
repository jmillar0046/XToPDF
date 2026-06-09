package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.conversion.image.BmpToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class BmpFileConverter extends AbstractFileConverter {
    private final BmpToPdfService bmpToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".bmp");
    }

    @Override
    protected String getFormatName() {
        return "BMP";
    }

    @Override
    protected void doConvert(MultipartFile inputFile, String outputFile) throws Exception {
        bmpToPdfService.convertBmpToPdf(inputFile, new File(outputFile));
    }
}
