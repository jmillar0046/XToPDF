package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.conversion.data.HtmlToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class HtmlFileConverter extends AbstractFileConverter {
    private final HtmlToPdfService htmlToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".html");
    }

    @Override
    protected String getFormatName() {
        return "HTML";
    }

    @Override
    protected void doConvert(MultipartFile inputFile, String outputFile) throws Exception {
        htmlToPdfService.convertHtmlToPdf(inputFile, new File(outputFile));
    }
}
