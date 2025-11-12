package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.DxfFileConverter;

@AllArgsConstructor
@Component
public class DxfFileConverterFactory implements FileConverterFactory {
    private final DxfFileConverter dxfFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return dxfFileConverter;
    }
    
}
