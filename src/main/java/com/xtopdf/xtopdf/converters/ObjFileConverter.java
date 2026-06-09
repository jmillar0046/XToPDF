package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.conversion.threed.ObjToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class ObjFileConverter extends AbstractFileConverter {
    private final ObjToPdfService objToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".obj");
    }

    @Override
    protected String getFormatName() {
        return "OBJ";
    }

    @Override
    protected void doConvert(MultipartFile inputFile, String outputFile) throws Exception {
        objToPdfService.convertObjToPdf(inputFile, new File(outputFile));
    }
}
