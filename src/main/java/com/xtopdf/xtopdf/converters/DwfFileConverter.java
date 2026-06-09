package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.conversion.cad.DwfToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class DwfFileConverter extends AbstractFileConverter {
    private final DwfToPdfService dwfToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".dwf");
    }

    @Override
    protected String getFormatName() {
        return "DWF";
    }

    @Override
    protected void doConvert(MultipartFile inputFile, String outputFile) throws Exception {
        dwfToPdfService.convertDwfToPdf(inputFile, new File(outputFile));
    }
}
