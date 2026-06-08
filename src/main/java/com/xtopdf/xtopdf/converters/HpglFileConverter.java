package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.conversion.cad.HpglToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class HpglFileConverter extends AbstractFileConverter {
    private final HpglToPdfService hpglToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".hpgl");
    }

    @Override
    protected String getFormatName() {
        return "HPGL";
    }

    @Override
    protected void doConvert(MultipartFile inputFile, String outputFile) throws Exception {
        hpglToPdfService.convertHpglToPdf(inputFile, new File(outputFile));
    }
}
