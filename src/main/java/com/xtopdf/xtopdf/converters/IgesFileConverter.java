package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.conversion.threed.IgesToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class IgesFileConverter extends AbstractFileConverter {
    private final IgesToPdfService igesToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".iges");
    }

    @Override
    protected String getFormatName() {
        return "IGES";
    }

    @Override
    protected void doConvert(MultipartFile inputFile, String outputFile) throws Exception {
        igesToPdfService.convertIgesToPdf(inputFile, new File(outputFile));
    }
}
