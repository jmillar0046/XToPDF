package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.conversion.threed.ThreeMfToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class ThreeMfFileConverter extends AbstractFileConverter {
    private final ThreeMfToPdfService threemfToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".3mf");
    }

    @Override
    protected String getFormatName() {
        return "3MF";
    }

    @Override
    protected void doConvert(MultipartFile inputFile, String outputFile) throws Exception {
        threemfToPdfService.convert3mfToPdf(inputFile, new File(outputFile));
    }
}
