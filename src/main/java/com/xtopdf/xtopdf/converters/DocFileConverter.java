package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.conversion.document.DocToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class DocFileConverter extends AbstractFileConverter {
    private final DocToPdfService docToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".doc");
    }

    @Override
    protected String getFormatName() {
        return "DOC";
    }

    @Override
    protected void doConvert(MultipartFile inputFile, String outputFile) throws Exception {
        docToPdfService.convertDocToPdf(inputFile, new File(outputFile));
    }
}
