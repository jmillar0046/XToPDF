package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.conversion.data.XmlToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class XmlFileConverter extends AbstractFileConverter {
    private final XmlToPdfService xmlToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".xml");
    }

    @Override
    protected String getFormatName() {
        return "XML";
    }

    @Override
    protected void doConvert(MultipartFile inputFile, String outputFile) throws Exception {
        xmlToPdfService.convertXmlToPdf(inputFile, new File(outputFile));
    }
}
