package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.EmfFileConverter;

@AllArgsConstructor
@Component
public class EmfFileConverterFactory implements FileConverterFactory {
    private final EmfFileConverter emfFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return emfFileConverter;
    }
}
