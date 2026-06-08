package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.conversion.image.EmfToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class EmfFileConverter extends AbstractFileConverter {
    private final EmfToPdfService emfToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".emf");
    }

    @Override
    protected String getFormatName() {
        return "EMF";
    }

    @Override
    protected void doConvert(MultipartFile inputFile, String outputFile) throws Exception {
        emfToPdfService.convertEmfToPdf(inputFile, new File(outputFile));
    }
}
