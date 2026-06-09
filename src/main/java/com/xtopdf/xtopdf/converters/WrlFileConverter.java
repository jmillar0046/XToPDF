package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.conversion.threed.WrlToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class WrlFileConverter extends AbstractFileConverter {
    private final WrlToPdfService wrlToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".wrl");
    }

    @Override
    protected String getFormatName() {
        return "WRL";
    }

    @Override
    protected void doConvert(MultipartFile inputFile, String outputFile) throws Exception {
        wrlToPdfService.convertWrlToPdf(inputFile, new File(outputFile));
    }
}
