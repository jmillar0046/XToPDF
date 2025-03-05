package com.xtopdf.xtopdf.services;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.factories.DocxFileConverterFactory;
import com.xtopdf.xtopdf.factories.FileConverterFactory;
import com.xtopdf.xtopdf.factories.TxtFileConverterFactory;

@Service
public class FileConversionService {
    @Autowired
    private TxtFileConverterFactory txtFileConverterFactory;
    @Autowired
    private DocxFileConverterFactory docxFileConverterFactory;

    public void convertFile(String inputFile, String outputFile) {
        FileConverterFactory factory = getFactoryForFile(inputFile);

        if(Objects.isNull(factory)) {
            //No converter found for this file type
        } else {
            FileConverter converter = factory.createFileConverter();
            converter.convertToPDF(inputFile, outputFile);
        }
    }

    private FileConverterFactory getFactoryForFile(String inputFile) {
        if(inputFile.endsWith(".txt")){
            return txtFileConverterFactory;
        } else if (inputFile.endsWith(".docx")){
            return docxFileConverterFactory;
        } else {
            return null;
        }
    }
}
