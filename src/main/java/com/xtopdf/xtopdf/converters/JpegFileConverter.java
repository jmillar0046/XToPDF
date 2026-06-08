package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.conversion.image.JpegToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class JpegFileConverter extends AbstractFileConverter {
    private final JpegToPdfService jpegToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".jpeg", ".jpg");
    }

    @Override
    protected String getFormatName() {
        return "JPEG";
    }

    @Override
    protected void doConvert(MultipartFile inputFile, String outputFile) throws Exception {
        jpegToPdfService.convertJpegToPdf(inputFile, new File(outputFile));
    }
}
