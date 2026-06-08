package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.conversion.image.TiffToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class TiffFileConverter extends AbstractFileConverter {
    private final TiffToPdfService tiffToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".tiff", ".tif");
    }

    @Override
    protected String getFormatName() {
        return "TIFF";
    }

    @Override
    protected void doConvert(MultipartFile inputFile, String outputFile) throws Exception {
        tiffToPdfService.convertTiffToPdf(inputFile, new File(outputFile));
    }
}
