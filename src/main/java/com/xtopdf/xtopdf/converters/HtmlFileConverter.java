package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.HtmlToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@AllArgsConstructor
@Component
public class HtmlFileConverter implements FileConverter {
    private final HtmlToPdfService htmlToPdfService;

    @Override
    public void convertToPDF(MultipartFile htmlFile, String outputFile) {
        if (htmlFile == null) {
            throw new NullPointerException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new NullPointerException("Output file must not be null");
        }

        htmlToPdfService.convertHtmlToPdf(htmlFile, new File(outputFile));
    }
}
