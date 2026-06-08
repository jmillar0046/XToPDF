package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.conversion.cad.DxfToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class DxfFileConverter extends AbstractFileConverter {
    private final DxfToPdfService dxfToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".dxf");
    }

    @Override
    protected String getFormatName() {
        return "DXF";
    }

    @Override
    protected void doConvert(MultipartFile inputFile, String outputFile) throws Exception {
        dxfToPdfService.convertDxfToPdf(inputFile, new File(outputFile));
    }
}
