package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.conversion.document.RtfToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class RtfFileConverter extends AbstractFileConverter {
    private final RtfToPdfService rtfToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".rtf");
    }

    @Override
    protected String getFormatName() {
        return "RTF";
    }

    @Override
    protected void doConvert(MultipartFile inputFile, String outputFile) throws Exception {
        rtfToPdfService.convertRtfToPdf(inputFile, new File(outputFile));
    }
}
