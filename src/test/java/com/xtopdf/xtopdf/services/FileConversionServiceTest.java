package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.config.PageNumberConfig;
import com.xtopdf.xtopdf.config.WatermarkConfig;
import com.xtopdf.xtopdf.converters.ConverterRegistry;
import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.dto.ConversionParameters;
import com.xtopdf.xtopdf.exceptions.FileConversionException;
import com.xtopdf.xtopdf.services.operations.PageNumberService;
import com.xtopdf.xtopdf.services.operations.PdfMergeService;
import com.xtopdf.xtopdf.services.operations.WatermarkService;
import com.xtopdf.xtopdf.services.orchestration.ContainerOrchestrationService;
import com.xtopdf.xtopdf.validation.FileContentValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileConversionServiceTest {

    @Mock private ConverterRegistry converterRegistry;
    @Mock private FileContentValidator contentValidator;
    @Mock private PdfMergeService pdfMergeService;
    @Mock private PageNumberService pageNumberService;
    @Mock private WatermarkService watermarkService;
    @Mock private ContainerOrchestrationService containerOrchestrationService;
    @Mock private FileConverter mockConverter;

    private FileConversionService fileConversionService;

    @BeforeEach
    void setUp() throws Exception {
        // Setup container orchestration to execute locally by default
        lenient().doAnswer(invocation -> {
            Runnable logic = invocation.getArgument(2);
            logic.run();
            return null;
        }).when(containerOrchestrationService).executeInContainer(any(), any(), any());

        fileConversionService = new FileConversionService(
                converterRegistry,
                contentValidator,
                pdfMergeService,
                pageNumberService,
                watermarkService,
                containerOrchestrationService
        );
    }

    // --- Tests for convertFile(ConversionParameters) delegating to ConverterRegistry ---

    @Test
    void convertFile_delegatesToConverterRegistryForConverterLookup() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile(
                "file", "report.docx", "application/octet-stream", "content".getBytes());
        ConversionParameters params = ConversionParameters.of(inputFile, "/output/report.pdf");

        when(converterRegistry.getConverter(".docx")).thenReturn(mockConverter);

        fileConversionService.convertFile(params);

        verify(converterRegistry).getConverter(".docx");
        verify(mockConverter).convertToPDF(eq(inputFile), eq("/output/report.pdf"), eq(false));
    }

    @Test
    void convertFile_extractsExtensionCorrectlyForVariousFormats() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile(
                "file", "image.PNG", "image/png", "content".getBytes());
        ConversionParameters params = ConversionParameters.of(inputFile, "/output/image.pdf");

        when(converterRegistry.getConverter(".png")).thenReturn(mockConverter);

        fileConversionService.convertFile(params);

        // Extension should be lowercased before lookup
        verify(converterRegistry).getConverter(".png");
    }

    // --- Tests for FileContentValidator being called before conversion ---

    @Test
    void convertFile_callsFileContentValidatorBeforeConversion() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", "content".getBytes());
        ConversionParameters params = ConversionParameters.of(inputFile, "/output/photo.pdf");

        when(converterRegistry.getConverter(".jpg")).thenReturn(mockConverter);

        fileConversionService.convertFile(params);

        // Verify validator is called
        verify(contentValidator).validate(inputFile, ".jpg");

        // Verify ordering: validate is called before getConverter
        InOrder inOrder = inOrder(contentValidator, converterRegistry, mockConverter);
        inOrder.verify(contentValidator).validate(inputFile, ".jpg");
        inOrder.verify(converterRegistry).getConverter(".jpg");
        inOrder.verify(mockConverter).convertToPDF(eq(inputFile), eq("/output/photo.pdf"), eq(false));
    }

    // --- Tests for null inputFile ---

    @Test
    void convertFile_nullInputFile_throwsFileConversionExceptionWithRequiredMessage() {
        ConversionParameters params = new ConversionParameters(
                null, "/output/file.pdf", null, null,
                PageNumberConfig.disabled(), WatermarkConfig.disabled(), false);

        assertThatThrownBy(() -> fileConversionService.convertFile(params))
                .isInstanceOf(FileConversionException.class)
                .hasMessage("Input file is required");
    }

    // --- Tests for null outputFile ---

    @Test
    void convertFile_nullOutputFile_throwsFileConversionExceptionWithRequiredMessage() {
        MockMultipartFile inputFile = new MockMultipartFile(
                "file", "test.txt", "text/plain", "content".getBytes());
        ConversionParameters params = new ConversionParameters(
                inputFile, null, null, null,
                PageNumberConfig.disabled(), WatermarkConfig.disabled(), false);

        assertThatThrownBy(() -> fileConversionService.convertFile(params))
                .isInstanceOf(FileConversionException.class)
                .hasMessage("Output file path is required");
    }

    // --- Tests for unsupported extension propagation ---

    @Test
    void convertFile_unsupportedExtension_propagatesFileConversionExceptionFromRegistry() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile(
                "file", "data.xyz", "application/octet-stream", "content".getBytes());
        ConversionParameters params = ConversionParameters.of(inputFile, "/output/data.pdf");

        when(converterRegistry.getConverter(".xyz"))
                .thenThrow(new FileConversionException("Unsupported file format: .xyz"));

        assertThatThrownBy(() -> fileConversionService.convertFile(params))
                .isInstanceOf(FileConversionException.class)
                .hasMessage("Unsupported file format: .xyz");
    }

    // --- Tests for conversion pipeline (page numbers, watermark, merge) ---

    @Test
    void convertFile_withPageNumbersEnabled_addsPageNumbers() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile(
                "file", "doc.txt", "text/plain", "content".getBytes());
        PageNumberConfig pageConfig = PageNumberConfig.builder().enabled(true).build();
        ConversionParameters params = new ConversionParameters(
                inputFile, "/output/doc.pdf", null, null,
                pageConfig, WatermarkConfig.disabled(), false);

        when(converterRegistry.getConverter(".txt")).thenReturn(mockConverter);

        fileConversionService.convertFile(params);

        verify(pageNumberService).addPageNumbers(any(java.io.File.class), eq(pageConfig));
    }

    @Test
    void convertFile_withWatermarkEnabled_addsWatermark() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile(
                "file", "doc.txt", "text/plain", "content".getBytes());
        WatermarkConfig watermarkConfig = WatermarkConfig.builder().enabled(true).text("DRAFT").build();
        ConversionParameters params = new ConversionParameters(
                inputFile, "/output/doc.pdf", null, null,
                PageNumberConfig.disabled(), watermarkConfig, false);

        when(converterRegistry.getConverter(".txt")).thenReturn(mockConverter);

        fileConversionService.convertFile(params);

        verify(watermarkService).addWatermark(any(java.io.File.class), eq(watermarkConfig));
    }

    @Test
    void convertFile_withExistingPdf_mergesPdfs() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile(
                "file", "doc.txt", "text/plain", "content".getBytes());
        MockMultipartFile existingPdf = new MockMultipartFile(
                "existing", "existing.pdf", "application/pdf", "pdf-content".getBytes());
        ConversionParameters params = new ConversionParameters(
                inputFile, "/output/doc.pdf", existingPdf, "back",
                PageNumberConfig.disabled(), WatermarkConfig.disabled(), false);

        when(converterRegistry.getConverter(".txt")).thenReturn(mockConverter);

        fileConversionService.convertFile(params);

        verify(pdfMergeService).mergePdfs(any(java.io.File.class), eq(existingPdf), eq("back"));
    }

    @Test
    void convertFile_withExecuteMacros_passesExecuteMacrosToConverter() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile(
                "file", "spreadsheet.xlsx", "application/octet-stream", "content".getBytes());
        ConversionParameters params = new ConversionParameters(
                inputFile, "/output/spreadsheet.pdf", null, null,
                PageNumberConfig.disabled(), WatermarkConfig.disabled(), true);

        when(converterRegistry.getConverter(".xlsx")).thenReturn(mockConverter);

        fileConversionService.convertFile(params);

        verify(mockConverter).convertToPDF(eq(inputFile), eq("/output/spreadsheet.pdf"), eq(true));
    }

    // --- Tests for backward-compatible deprecated methods ---

    @Test
    void convertFile_deprecatedSimpleMethod_delegatesToNewMethod() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile(
                "file", "test.txt", "text/plain", "content".getBytes());

        when(converterRegistry.getConverter(".txt")).thenReturn(mockConverter);

        fileConversionService.convertFile(inputFile, "output.pdf");

        verify(converterRegistry).getConverter(".txt");
        verify(mockConverter).convertToPDF(eq(inputFile), eq("output.pdf"), eq(false));
    }

    @Test
    void convertFile_deprecatedFullMethod_delegatesToNewMethod() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile(
                "file", "test.txt", "text/plain", "content".getBytes());
        MockMultipartFile existingPdf = new MockMultipartFile(
                "existing", "existing.pdf", "application/pdf", "pdf".getBytes());
        PageNumberConfig pageConfig = PageNumberConfig.disabled();
        WatermarkConfig watermarkConfig = WatermarkConfig.disabled();

        when(converterRegistry.getConverter(".txt")).thenReturn(mockConverter);

        fileConversionService.convertFile(inputFile, "output.pdf", existingPdf, "front",
                pageConfig, watermarkConfig, false);

        verify(converterRegistry).getConverter(".txt");
        verify(mockConverter).convertToPDF(eq(inputFile), eq("output.pdf"), eq(false));
    }
}
