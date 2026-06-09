package com.xtopdf.xtopdf.services;

import java.util.Objects;
import java.util.concurrent.*;

import com.xtopdf.xtopdf.config.MetricsConfiguration.ConversionMetrics;
import com.xtopdf.xtopdf.converters.ConverterRegistry;
import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.dto.ConversionParameters;
import com.xtopdf.xtopdf.exceptions.ConversionTimeoutException;
import com.xtopdf.xtopdf.exceptions.FileConversionException;
import com.xtopdf.xtopdf.services.model.ConversionRuntimeException;
import com.xtopdf.xtopdf.services.operations.PageNumberService;
import com.xtopdf.xtopdf.services.operations.PdfMergeService;
import com.xtopdf.xtopdf.services.operations.WatermarkService;
import com.xtopdf.xtopdf.services.orchestration.ContainerOrchestrationService;
import com.xtopdf.xtopdf.validation.FileContentValidator;
import io.micrometer.core.instrument.Timer;
import io.micrometer.observation.annotation.Observed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class FileConversionService {
    private final ConverterRegistry converterRegistry;
    private final FileContentValidator contentValidator;
    private final PdfMergeService pdfMergeService;
    private final PageNumberService pageNumberService;
    private final WatermarkService watermarkService;
    private final ContainerOrchestrationService containerOrchestrationService;
    private final ConversionMetrics conversionMetrics;
    private final int timeoutSeconds;

    public FileConversionService(
            ConverterRegistry converterRegistry,
            FileContentValidator contentValidator,
            PdfMergeService pdfMergeService,
            PageNumberService pageNumberService,
            WatermarkService watermarkService,
            ContainerOrchestrationService containerOrchestrationService,
            ConversionMetrics conversionMetrics,
            @Value("${xtopdf.conversion.timeout-seconds:300}") int timeoutSeconds) {
        this.converterRegistry = converterRegistry;
        this.contentValidator = contentValidator;
        this.pdfMergeService = pdfMergeService;
        this.pageNumberService = pageNumberService;
        this.watermarkService = watermarkService;
        this.containerOrchestrationService = containerOrchestrationService;
        this.conversionMetrics = conversionMetrics;
        this.timeoutSeconds = timeoutSeconds;
    }

    /**
     * Converts a file to PDF using the parameters specified in the ConversionParameters object.
     *
     * @param params the conversion parameters
     * @throws FileConversionException if the conversion fails
     */
    @Observed(name = "file.conversion", contextualName = "convert-file",
            lowCardinalityKeyValues = {"operation", "convertFile"})
    public void convertFile(ConversionParameters params) throws FileConversionException {
        if (params.inputFile() == null) {
            throw new FileConversionException("Input file is required");
        }
        if (params.outputFile() == null) {
            throw new FileConversionException("Output file path is required");
        }

        String fileName = Objects.requireNonNull(params.inputFile().getOriginalFilename());
        String extension = extractExtension(fileName);
        String format = extension.startsWith(".") ? extension.substring(1) : extension;

        // Record metrics
        conversionMetrics.incrementRequestCount(format);
        conversionMetrics.recordFileSize(params.inputFile().getSize());
        Timer.Sample timerSample = conversionMetrics.startTimer();

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

        // Execute conversion either in container or locally, with timeout
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> future = executor.submit(() -> {
            try {
                containerOrchestrationService.executeInContainer(params.inputFile(), params.outputFile(), conversionLogic);
            } catch (ConversionRuntimeException e) {
                throw e;
            } catch (FileConversionException e) {
                throw new ConversionRuntimeException(e);
            } catch (RuntimeException e) {
                log.error("Unexpected runtime error during conversion of {}: {}", fileName, e.getMessage(), e);
                throw new ConversionRuntimeException(
                        new FileConversionException("Unexpected error converting " + fileName + ": " + e.getMessage(), e));
            }
        });

        try {
            future.get(timeoutSeconds, TimeUnit.SECONDS);
            conversionMetrics.stopTimer(timerSample, format);
        } catch (TimeoutException e) {
            future.cancel(true);
            conversionMetrics.incrementErrorCount(format, "timeout");
            log.error("Conversion timed out after {}s for file: {}", timeoutSeconds, fileName);
            throw new ConversionTimeoutException(
                    "Conversion of " + fileName + " timed out after " + timeoutSeconds + " seconds", e);
        } catch (ExecutionException e) {
            conversionMetrics.incrementErrorCount(format, "execution_error");
            var cause = e.getCause();
            if (cause instanceof ConversionRuntimeException cre) {
                throw cre.getFileConversionException();
            } else if (cause instanceof RuntimeException re) {
                throw new FileConversionException("Unexpected error converting " + fileName + ": " + re.getMessage(), re);
            }
            throw new FileConversionException("Unexpected error converting " + fileName + ": " + cause.getMessage(), cause);
        } catch (InterruptedException e) {
            conversionMetrics.incrementErrorCount(format, "interrupted");
            Thread.currentThread().interrupt();
            throw new FileConversionException("Conversion of " + fileName + " was interrupted", e);
        } finally {
            executor.shutdownNow();
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
