package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

public abstract class AbstractFileConverter implements FileConverter {

    @Override
    public final void convertToPDF(MultipartFile inputFile, String outputFile) throws FileConversionException {
        if (inputFile == null) {
            throw new FileConversionException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new FileConversionException("Output file path must not be null");
        }
        try {
            doConvert(inputFile, outputFile);
        } catch (FileConversionException e) {
            throw e; // Pass through without double-wrapping
        } catch (Exception e) {
            throw new FileConversionException(
                "Error converting " + getFormatName() + " to PDF: " + e.getMessage(), e);
        }
    }

    @Override
    public void convertToPDF(MultipartFile inputFile, String outputFile, boolean executeMacros) throws FileConversionException {
        // Default delegates to no-macro version; override in macro-capable converters (e.g., Excel)
        convertToPDF(inputFile, outputFile);
    }

    protected abstract void doConvert(MultipartFile inputFile, String outputFile) throws Exception;

    protected abstract String getFormatName();

    @Override
    public abstract Set<String> getSupportedExtensions();
}
