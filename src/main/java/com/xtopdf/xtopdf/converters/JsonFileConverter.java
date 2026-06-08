package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.conversion.data.JsonToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class JsonFileConverter extends AbstractFileConverter {
    private final JsonToPdfService jsonToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".json");
    }

    @Override
    protected String getFormatName() {
        return "JSON";
    }

    @Override
    protected void doConvert(MultipartFile inputFile, String outputFile) throws Exception {
        jsonToPdfService.convertJsonToPdf(inputFile, new File(outputFile));
    }
}
