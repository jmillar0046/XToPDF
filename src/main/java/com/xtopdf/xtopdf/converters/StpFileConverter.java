package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.conversion.threed.StpToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class StpFileConverter extends AbstractFileConverter {
    private final StpToPdfService stpToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".stp");
    }

    @Override
    protected String getFormatName() {
        return "STP";
    }

    @Override
    protected void doConvert(MultipartFile inputFile, String outputFile) throws Exception {
        stpToPdfService.convertStpToPdf(inputFile, new File(outputFile));
    }
}
