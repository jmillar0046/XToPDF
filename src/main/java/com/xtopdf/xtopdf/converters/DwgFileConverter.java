package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.conversion.cad.DwgToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class DwgFileConverter extends AbstractFileConverter {
    private final DwgToPdfService dwgToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".dwg");
    }

    @Override
    protected String getFormatName() {
        return "DWG";
    }

    @Override
    protected void doConvert(MultipartFile inputFile, String outputFile) throws Exception {
        dwgToPdfService.convertDwgToPdf(inputFile, new File(outputFile));
    }
}
