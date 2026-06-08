package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.conversion.presentation.OdpToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class OdpFileConverter extends AbstractFileConverter {
    private final OdpToPdfService odpToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".odp");
    }

    @Override
    protected String getFormatName() {
        return "ODP";
    }

    @Override
    protected void doConvert(MultipartFile inputFile, String outputFile) throws Exception {
        odpToPdfService.convertOdpToPdf(inputFile, new File(outputFile));
    }
}
