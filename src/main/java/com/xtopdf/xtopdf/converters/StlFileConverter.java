package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.conversion.threed.StlToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class StlFileConverter extends AbstractFileConverter {
    private final StlToPdfService stlToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".stl");
    }

    @Override
    protected String getFormatName() {
        return "STL";
    }

    @Override
    protected void doConvert(MultipartFile inputFile, String outputFile) throws Exception {
        stlToPdfService.convertStlToPdf(inputFile, new File(outputFile));
    }
}
