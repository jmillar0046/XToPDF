package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.conversion.image.WmfToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class WmfFileConverter extends AbstractFileConverter {
    private final WmfToPdfService wmfToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".wmf");
    }

    @Override
    protected String getFormatName() {
        return "WMF";
    }

    @Override
    protected void doConvert(MultipartFile inputFile, String outputFile) throws Exception {
        wmfToPdfService.convertWmfToPdf(inputFile, new File(outputFile));
    }
}
