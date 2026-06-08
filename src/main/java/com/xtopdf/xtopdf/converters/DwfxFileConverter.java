package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.conversion.cad.DwfxToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class DwfxFileConverter extends AbstractFileConverter {
    private final DwfxToPdfService dwfxToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".dwfx");
    }

    @Override
    protected String getFormatName() {
        return "DWFX";
    }

    @Override
    protected void doConvert(MultipartFile inputFile, String outputFile) throws Exception {
        dwfxToPdfService.convertDwfxToPdf(inputFile, new File(outputFile));
    }
}
