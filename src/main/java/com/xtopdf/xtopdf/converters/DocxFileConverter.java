package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.conversion.document.DocxToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class DocxFileConverter extends AbstractFileConverter {
    private final DocxToPdfService docxToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".docx");
    }

    @Override
    protected String getFormatName() {
        return "DOCX";
    }

    @Override
    protected void doConvert(MultipartFile inputFile, String outputFile) throws Exception {
        docxToPdfService.convertDocxToPdf(inputFile, new File(outputFile));
    }
}
