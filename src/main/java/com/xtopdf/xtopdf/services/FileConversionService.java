package com.xtopdf.xtopdf.services;

import java.util.Objects;

import com.xtopdf.xtopdf.converters.ConverterRegistry;
import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.dto.ConversionParameters;
import com.xtopdf.xtopdf.exceptions.FileConversionException;
import com.xtopdf.xtopdf.services.model.ConversionRuntimeException;
import com.xtopdf.xtopdf.services.operations.PageNumberService;
import com.xtopdf.xtopdf.services.operations.PdfMergeService;
import com.xtopdf.xtopdf.services.operations.WatermarkService;
import com.xtopdf.xtopdf.services.orchestration.ContainerOrchestrationService;
import com.xtopdf.xtopdf.validation.FileContentValidator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Service
@Slf4j
public class FileConversionService {
    private final ConverterRegistry converterRegistry;
    private final FileContentValidator contentValidator;
    private final PdfMergeService pdfMergeService;
    private final PageNumberService pageNumberService;
    private final WatermarkService watermarkService;
    private final ContainerOrchestrationService containerOrchestrationService;

    /**
     * Converts a file to PDF using the parameters specified in the ConversionParameters object.
     *
     * @param params the conversion parameters
     * @throws FileConversionException if the conversion fails
     */
    public void convertFile(ConversionParameters params) throws FileConversionException {
        if (params.inputFile() == null) {
            throw new FileConversionException("Input file is required");
        }
        if (params.outputFile() == null) {
            throw new FileConversionException("Output file path is required");
        }

        String fileName = Objects.requireNonNull(params.inputFile().getOriginalFilename());
        String extension = extractExtension(fileName);

        contentValidator.validate(params.inputFile(), extension);
        FileConverter converter = converterRegistry.getConverter(extension);

        // Define the conversion logic as a Runnable
        Runnable conversionLogic = () -> {
            try {
                converter.convertToPDF(params.inputFile(), params.outputFile(), params.executeMacros());

                // Add page numbers centrally if enabled
                if (params.pageNumberConfig().isEnabled()) {
                    try {
                        java.io.File outputPdfFile = new java.io.File(params.outputFile());
                        pageNumberService.addPageNumbers(outputPdfFile, params.pageNumberConfig());
                    } catch (java.io.IOException e) {
                        throw new ConversionRuntimeException(
                                new FileConversionException("Failed to add page numbers to " + fileName + ": " + e.getMessage(), e));
                    }
                }

                // Add watermark if enabled
                if (params.watermarkConfig().isEnabled()) {
                    try {
                        java.io.File outputPdfFile = new java.io.File(params.outputFile());
                        watermarkService.addWatermark(outputPdfFile, params.watermarkConfig());
                    } catch (java.io.IOException e) {
                        throw new ConversionRuntimeException(
                                new FileConversionException("Failed to add watermark to " + fileName + ": " + e.getMessage(), e));
                    }
                }

                // If an existing PDF is provided, merge it with the converted PDF
                if (params.existingPdf() != null && !params.existingPdf().isEmpty()) {
                    try {
                        java.io.File outputPdfFile = new java.io.File(params.outputFile());
                        pdfMergeService.mergePdfs(outputPdfFile, params.existingPdf(), params.position());
                    } catch (java.io.IOException e) {
                        throw new ConversionRuntimeException(
                                new FileConversionException("Failed to merge PDF files for " + fileName + ": " + e.getMessage(), e));
                    }
                }
            } catch (ConversionRuntimeException e) {
                throw e;
            } catch (Exception e) {
                log.error("Unexpected error during conversion of {}: {}", fileName, e.getMessage(), e);
                throw new ConversionRuntimeException(
                        new FileConversionException("Unexpected error converting " + fileName + ": " + e.getMessage(), e));
            }
        };

        // Execute conversion either in container or locally
        try {
            containerOrchestrationService.executeInContainer(params.inputFile(), params.outputFile(), conversionLogic);
        } catch (ConversionRuntimeException e) {
            throw e.getFileConversionException();
        } catch (RuntimeException e) {
            log.error("Unexpected runtime error during conversion of {}: {}", fileName, e.getMessage(), e);
            throw new FileConversionException("Unexpected error converting " + fileName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Extracts the file extension from a filename, including the leading dot.
     * Returns the extension in lowercase.
     * Rejects dot-files (files where the only dot is at index 0, like .gitignore).
     */
    private String extractExtension(String fileName) throws FileConversionException {
        String lowerCaseFileName = fileName.toLowerCase();
        int dotIndex = lowerCaseFileName.lastIndexOf('.');
        if (dotIndex <= 0) {
            throw new FileConversionException("Unsupported file format: " + fileName);
        }
        return lowerCaseFileName.substring(dotIndex);
    }
}
