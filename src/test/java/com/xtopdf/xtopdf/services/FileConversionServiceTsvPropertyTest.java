package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.converters.ConverterRegistry;
import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.dto.ConversionParameters;
import com.xtopdf.xtopdf.exceptions.FileConversionException;
import com.xtopdf.xtopdf.services.operations.PageNumberService;
import com.xtopdf.xtopdf.services.operations.PdfMergeService;
import com.xtopdf.xtopdf.services.operations.WatermarkService;
import com.xtopdf.xtopdf.services.orchestration.ContainerOrchestrationService;
import com.xtopdf.xtopdf.validation.FileContentValidator;
import net.jqwik.api.*;
import org.junit.jupiter.api.Tag;
import org.springframework.mock.web.MockMultipartFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for TSV file routing in FileConversionService.
 * Tests case-insensitive extension handling via ConverterRegistry delegation.
 */
class FileConversionServiceTsvPropertyTest {

    @Property(tries = 25)
    @Tag("Feature: tsv-file-support, Property 4: Case-Insensitive File Extension Routing")
    void caseInsensitiveRoutingForTsv(@ForAll("tsvExtensions") String extension) throws Exception {
        // Create service with mocked dependencies
        ConverterRegistry registry = mock(ConverterRegistry.class);
        FileConverter tsvConverter = mock(FileConverter.class);
        FileContentValidator contentValidator = mock(FileContentValidator.class);
        ContainerOrchestrationService containerService = mock(ContainerOrchestrationService.class);

        // Container service executes locally
        doAnswer(invocation -> {
            Runnable logic = invocation.getArgument(2);
            logic.run();
            return null;
        }).when(containerService).executeInContainer(any(), any(), any());

        // The registry should be called with the lowercased extension
        String expectedExtension = extension.toLowerCase();
        when(registry.getConverter(expectedExtension)).thenReturn(tsvConverter);

        FileConversionService service = new FileConversionService(
                registry, contentValidator,
                mock(PdfMergeService.class), mock(PageNumberService.class),
                mock(WatermarkService.class), containerService,
                new com.xtopdf.xtopdf.config.MetricsConfiguration.ConversionMetrics(
                        new io.micrometer.core.instrument.simple.SimpleMeterRegistry()), 300
        );

        MockMultipartFile inputFile = new MockMultipartFile(
                "file", "test" + extension, "text/plain", "col1\tcol2\nval1\tval2".getBytes());
        ConversionParameters params = ConversionParameters.of(inputFile, "/output/test.pdf");

        service.convertFile(params);

        // Verify the registry was called with the lowercased extension
        verify(registry).getConverter(expectedExtension);
        verify(tsvConverter).convertToPDF(eq(inputFile), eq("/output/test.pdf"), eq(false));
    }

    @Provide
    Arbitrary<String> tsvExtensions() {
        // Generate random case variations of .tsv and .tab
        return Arbitraries.of(".tsv", ".tab", ".TSV", ".TAB", ".Tsv", ".Tab",
                ".tSv", ".tAb", ".TsV", ".TaB", ".tsV", ".taB");
    }

    @Property(tries = 25)
    @Tag("Feature: tsv-file-support, Property 4: Case-Insensitive File Extension Routing")
    void nonTsvExtensionsDoNotRouteToTsvConverter(@ForAll("nonTsvExtensions") String extension) throws Exception {
        // Create service with mocked dependencies
        ConverterRegistry registry = mock(ConverterRegistry.class);
        FileConverter tsvConverter = mock(FileConverter.class);
        FileConverter otherConverter = mock(FileConverter.class);
        FileContentValidator contentValidator = mock(FileContentValidator.class);
        ContainerOrchestrationService containerService = mock(ContainerOrchestrationService.class);

        // Container service executes locally
        doAnswer(invocation -> {
            Runnable logic = invocation.getArgument(2);
            logic.run();
            return null;
        }).when(containerService).executeInContainer(any(), any(), any());

        String expectedExtension = extension.toLowerCase();
        when(registry.getConverter(expectedExtension)).thenReturn(otherConverter);

        FileConversionService service = new FileConversionService(
                registry, contentValidator,
                mock(PdfMergeService.class), mock(PageNumberService.class),
                mock(WatermarkService.class), containerService,
                new com.xtopdf.xtopdf.config.MetricsConfiguration.ConversionMetrics(
                        new io.micrometer.core.instrument.simple.SimpleMeterRegistry()), 300
        );

        MockMultipartFile inputFile = new MockMultipartFile(
                "file", "test" + extension, "text/plain", "content".getBytes());
        ConversionParameters params = ConversionParameters.of(inputFile, "/output/test.pdf");

        service.convertFile(params);

        // Verify the converter used is NOT the TSV converter
        verify(tsvConverter, never()).convertToPDF(any(), any(), anyBoolean());
        verify(otherConverter).convertToPDF(eq(inputFile), eq("/output/test.pdf"), eq(false));
    }

    @Provide
    Arbitrary<String> nonTsvExtensions() {
        return Arbitraries.of(".txt", ".csv", ".pdf", ".doc", ".docx", ".xls", ".xlsx",
                ".json", ".xml", ".html", ".md", ".rtf");
    }
}
