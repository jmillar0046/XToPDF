package com.xtopdf.xtopdf.services;

import java.io.File;
import java.util.Objects;

import com.xtopdf.xtopdf.exceptions.FileConversionException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.factories.DocxFileConverterFactory;
import com.xtopdf.xtopdf.factories.FileConverterFactory;
import com.xtopdf.xtopdf.factories.TxtFileConverterFactory;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Service
@Slf4j
public class FileConversionService {
    private final TxtFileConverterFactory txtFileConverterFactory;
    private final DocxFileConverterFactory docxFileConverterFactory;

    public void convertFile(MultipartFile inputFile, String outputFile) throws FileConversionException {
        FileConverterFactory factory = getFactoryForFile(Objects.requireNonNull(inputFile.getOriginalFilename()));

        if (Objects.nonNull(factory)) {
            FileConverter converter = factory.createFileConverter();
            converter.convertToPDF(inputFile, outputFile);
        } else {
            throw new FileConversionException("Failed to convert file: " + inputFile.getName());
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
