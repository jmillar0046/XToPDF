package com.xtopdf.xtopdf.factories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.DocxFileConverter;
import com.xtopdf.xtopdf.converters.FileConverter;

@Component
public class DocxFileConverterFactory implements FileConverterFactory {

    @Autowired
    private DocxFileConverter docxFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return docxFileConverter;
    }
    
}