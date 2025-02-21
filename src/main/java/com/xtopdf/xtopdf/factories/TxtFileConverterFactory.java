package com.xtopdf.xtopdf.factories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.converters.TxtFileConverter;

@Component
public class TxtFileConverterFactory implements FileConverterFactory{

    @Autowired
    private TxtFileConverter txtFileConverter;

    @Override
    public FileConverter createFileConverter() {
        return txtFileConverter;
    }
    
}
