package com.xtopdf.xtopdf.services;

import java.io.File;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.factories.DocxFileConverterFactory;
import com.xtopdf.xtopdf.factories.FileConverterFactory;
import com.xtopdf.xtopdf.factories.TxtFileConverterFactory;

@AllArgsConstructor
@Service
@Slf4j
public class FileConversionService {
    private final TxtFileConverterFactory txtFileConverterFactory;
    private final DocxFileConverterFactory docxFileConverterFactory;

    public void convertFile(File inputFile, String outputFile) {
        FileConverterFactory factory = getFactoryForFile(inputFile.getAbsolutePath());

        if (Objects.nonNull(factory)) {
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
            log.error("No converter found for file {}", inputFile);
            return null;
        }
    }

}
