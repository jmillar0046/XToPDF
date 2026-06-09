package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.conversion.document.TxtToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class TxtFileConverter extends AbstractFileConverter {
    private final TxtToPdfService txtToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".txt");
    }

    @Override
    protected String getFormatName() {
        return "TXT";
    }

    @Override
    protected void doConvert(MultipartFile inputFile, String outputFile) throws Exception {
        txtToPdfService.convertTxtToPdf(inputFile, new File(outputFile));
    }
}
