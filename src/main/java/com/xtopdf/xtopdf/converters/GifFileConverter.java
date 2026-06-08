package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.conversion.image.GifToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class GifFileConverter extends AbstractFileConverter {
    private final GifToPdfService gifToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".gif");
    }

    @Override
    protected String getFormatName() {
        return "GIF";
    }

    @Override
    protected void doConvert(MultipartFile inputFile, String outputFile) throws Exception {
        gifToPdfService.convertGifToPdf(inputFile, new File(outputFile));
    }
}
