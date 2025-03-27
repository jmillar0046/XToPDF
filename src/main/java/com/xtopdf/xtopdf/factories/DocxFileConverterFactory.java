package com.xtopdf.xtopdf.factories;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.DocxFileConverter;
import com.xtopdf.xtopdf.converters.FileConverter;

@AllArgsConstructor
@Component
public class DocxFileConverterFactory implements FileConverterFactory {
    private final DocxFileConverter docxFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return docxFileConverter;
    }
    
}