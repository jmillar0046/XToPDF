package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.conversion.document.MarkdownToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class MarkdownFileConverter extends AbstractFileConverter {
    private final MarkdownToPdfService markdownToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".md", ".markdown");
    }

    @Override
    protected String getFormatName() {
        return "Markdown";
    }

    @Override
    protected void doConvert(MultipartFile inputFile, String outputFile) throws Exception {
        markdownToPdfService.convertMarkdownToPdf(inputFile, new File(outputFile));
    }
}
