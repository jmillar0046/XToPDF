package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.conversion.threed.X3dToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class X3dFileConverter extends AbstractFileConverter {
    private final X3dToPdfService x3dToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".x3d");
    }

    @Override
    protected String getFormatName() {
        return "X3D";
    }

    @Override
    protected void doConvert(MultipartFile inputFile, String outputFile) throws Exception {
        x3dToPdfService.convertX3dToPdf(inputFile, new File(outputFile));
    }
}
