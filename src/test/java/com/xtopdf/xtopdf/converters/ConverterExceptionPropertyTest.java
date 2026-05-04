package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;
import net.jqwik.api.*;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

/**
 * Property-based tests for standardized exception handling across all converters.
 * Tests Properties 3 and 4 from the design document.
 */
class ConverterExceptionPropertyTest {

    // ---- Converter descriptors for testing ----

    /**
     * Describes a converter that can be instantiated with a mock service that throws.
     */
    record ConverterDescriptor(
            String formatName,
            FileConverter converter
    ) {
        @Override
        public String toString() {
            return formatName;
        }
    }

    /**
     * Creates a list of converter descriptors where each converter's service mock
     * is configured to throw a RuntimeException (wrapping the given message) when
     * convertToPDF is called. We use RuntimeException because some service methods
     * don't declare checked exceptions, and Mockito can't throw checked exceptions
     * from methods that don't declare them. The standardized converter pattern should
     * catch all Exception types and wrap them in FileConversionException.
     */
    private static List<ConverterDescriptor> createThrowingConverters(String errorMessage) {
        RuntimeException runtimeException = new RuntimeException(errorMessage);
        return List.of(
                createThrowingConverter("DOCX", runtimeException,
                        com.xtopdf.xtopdf.services.conversion.document.DocxToPdfService.class,
                        (svc) -> { try { doThrow(runtimeException).when(svc).convertDocxToPdf(any(), any()); } catch (Exception e) { throw new RuntimeException(e); } },
                        (svc) -> new DocxFileConverter(svc)),
                createThrowingConverter("PNG", runtimeException,
                        com.xtopdf.xtopdf.services.conversion.image.PngToPdfService.class,
                        (svc) -> { try { doThrow(runtimeException).when(svc).convertPngToPdf(any(), any()); } catch (Exception e) { throw new RuntimeException(e); } },
                        (svc) -> new PngFileConverter(svc)),
                createThrowingConverter("CSV", runtimeException,
                        com.xtopdf.xtopdf.services.conversion.spreadsheet.DelimiterSeparatedToPdfService.class,
                        (svc) -> { try { doThrow(runtimeException).when(svc).convertDelimiterSeparatedToPdf(any(), any(), any(char.class)); } catch (Exception e) { throw new RuntimeException(e); } },
                        (svc) -> new DelimiterSeparatedConverter(svc, ',', "CSV", java.util.Set.of(".csv"))),
                createThrowingConverter("BMP", runtimeException,
                        com.xtopdf.xtopdf.services.conversion.image.BmpToPdfService.class,
                        (svc) -> { try { doThrow(runtimeException).when(svc).convertBmpToPdf(any(), any()); } catch (Exception e) { throw new RuntimeException(e); } },
                        (svc) -> new BmpFileConverter(svc)),
                createThrowingConverter("HTML", runtimeException,
                        com.xtopdf.xtopdf.services.conversion.data.HtmlToPdfService.class,
                        (svc) -> { doThrow(runtimeException).when(svc).convertHtmlToPdf(any(), any()); },
                        (svc) -> new HtmlFileConverter(svc)),
                createThrowingConverter("JSON", runtimeException,
                        com.xtopdf.xtopdf.services.conversion.data.JsonToPdfService.class,
                        (svc) -> { try { doThrow(runtimeException).when(svc).convertJsonToPdf(any(), any()); } catch (Exception e) { throw new RuntimeException(e); } },
                        (svc) -> new JsonFileConverter(svc)),
                createThrowingConverter("TXT", runtimeException,
                        com.xtopdf.xtopdf.services.conversion.document.TxtToPdfService.class,
                        (svc) -> { try { doThrow(runtimeException).when(svc).convertTxtToPdf(any(), any()); } catch (Exception e) { throw new RuntimeException(e); } },
                        (svc) -> new TxtFileConverter(svc)),
                createThrowingConverter("SVG", runtimeException,
                        com.xtopdf.xtopdf.services.conversion.image.SvgToPdfService.class,
                        (svc) -> { try { doThrow(runtimeException).when(svc).convertSvgToPdf(any(), any()); } catch (Exception e) { throw new RuntimeException(e); } },
                        (svc) -> new SvgFileConverter(svc)),
                createThrowingConverter("GIF", runtimeException,
                        com.xtopdf.xtopdf.services.conversion.image.GifToPdfService.class,
                        (svc) -> { try { doThrow(runtimeException).when(svc).convertGifToPdf(any(), any()); } catch (Exception e) { throw new RuntimeException(e); } },
                        (svc) -> new GifFileConverter(svc)),
                createThrowingConverter("JPEG", runtimeException,
                        com.xtopdf.xtopdf.services.conversion.image.JpegToPdfService.class,
                        (svc) -> { try { doThrow(runtimeException).when(svc).convertJpegToPdf(any(), any()); } catch (Exception e) { throw new RuntimeException(e); } },
                        (svc) -> new JpegFileConverter(svc)),
                createThrowingConverter("RTF", runtimeException,
                        com.xtopdf.xtopdf.services.conversion.document.RtfToPdfService.class,
                        (svc) -> { try { doThrow(runtimeException).when(svc).convertRtfToPdf(any(), any()); } catch (Exception e) { throw new RuntimeException(e); } },
                        (svc) -> new RtfFileConverter(svc)),
                createThrowingConverter("Markdown", runtimeException,
                        com.xtopdf.xtopdf.services.conversion.document.MarkdownToPdfService.class,
                        (svc) -> { try { doThrow(runtimeException).when(svc).convertMarkdownToPdf(any(), any()); } catch (Exception e) { throw new RuntimeException(e); } },
                        (svc) -> new MarkdownFileConverter(svc))
        );
    }

    @FunctionalInterface
    interface ServiceConfigurer<S> {
        void configure(S service);
    }

    @FunctionalInterface
    interface ConverterFactory<S> {
        FileConverter create(S service);
    }

    private static <S> ConverterDescriptor createThrowingConverter(
            String formatName,
            Exception exceptionToThrow,
            Class<S> serviceClass,
            ServiceConfigurer<S> configurer,
            ConverterFactory<S> factory) {
        S mockService = mock(serviceClass);
        configurer.configure(mockService);
        FileConverter converter = factory.create(mockService);
        return new ConverterDescriptor(formatName, converter);
    }

    // ---- Property 3: Converter Exception Wrapping ----

    /**
     * Property 3: Converter Exception Wrapping — For any FileConverter implementation
     * and for any exception thrown during conversion (IOException or otherwise), the
     * converter SHALL wrap it in a FileConversionException whose message contains both
     * the file format name and the original exception message.
     *
     * **Validates: Requirements 2.1, 2.4, 2.5**
     */
    @Property(tries = 25)
    @Tag("Feature: repo-efficiency-improvements, Property 3: Converter Exception Wrapping")
    void converterWrapsExceptionInFileConversionExceptionWithFormatAndMessage(
            @ForAll("exceptionMessages") String errorMessage,
            @ForAll("converterIndices") int converterIndex) {

        List<ConverterDescriptor> descriptors = createThrowingConverters(errorMessage);
        ConverterDescriptor descriptor = descriptors.get(converterIndex);

        MockMultipartFile inputFile = new MockMultipartFile(
                "file", "test.dat",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "test content".getBytes());

        Throwable thrown = catchThrowable(() ->
                descriptor.converter().convertToPDF(inputFile, "/tmp/output.pdf"));

        assertThat(thrown)
                .as("Converter %s should throw FileConversionException", descriptor.formatName())
                .isInstanceOf(FileConversionException.class);

        assertThat(thrown.getMessage())
                .as("Exception message from %s converter should contain format name", descriptor.formatName())
                .containsIgnoringCase(descriptor.formatName());

        assertThat(thrown.getMessage())
                .as("Exception message from %s converter should contain original error message", descriptor.formatName())
                .contains(errorMessage);

        assertThat(thrown.getCause())
                .as("Exception from %s converter should preserve original cause", descriptor.formatName())
                .isNotNull();
    }

    // ---- Providers ----

    @Provide
    Arbitrary<String> exceptionMessages() {
        return Arbitraries.oneOf(
                Arbitraries.of(
                        "File not found",
                        "Permission denied",
                        "Disk full",
                        "Stream closed",
                        "Connection reset",
                        "Broken pipe",
                        "Corrupt file data",
                        "Out of memory"
                ),
                Arbitraries.strings()
                        .withCharRange('a', 'z')
                        .ofMinLength(1)
                        .ofMaxLength(50)
        );
    }

    @Provide
    Arbitrary<Integer> converterIndices() {
        // 12 converters in our representative subset
        return Arbitraries.integers().between(0, 11);
    }

    // ---- Property 4: Converter Null Parameter Validation ----

    /**
     * Creates all converter instances with null service dependencies.
     * Null checks should happen BEFORE any service call, so null services are fine.
     */
    private static List<ConverterDescriptor> createConvertersWithNullServices() {
        return List.of(
                new ConverterDescriptor("DOCX", new DocxFileConverter(null)),
                new ConverterDescriptor("PNG", new PngFileConverter(null)),
                new ConverterDescriptor("CSV", new DelimiterSeparatedConverter(null, ',', "CSV", Set.of(".csv"))),
                new ConverterDescriptor("BMP", new BmpFileConverter(null)),
                new ConverterDescriptor("HTML", new HtmlFileConverter(null)),
                new ConverterDescriptor("JSON", new JsonFileConverter(null)),
                new ConverterDescriptor("TXT", new TxtFileConverter(null)),
                new ConverterDescriptor("SVG", new SvgFileConverter(null)),
                new ConverterDescriptor("GIF", new GifFileConverter(null)),
                new ConverterDescriptor("JPEG", new JpegFileConverter(null)),
                new ConverterDescriptor("RTF", new RtfFileConverter(null)),
                new ConverterDescriptor("Markdown", new MarkdownFileConverter(null)),
                new ConverterDescriptor("DOC", new DocFileConverter(null)),
                new ConverterDescriptor("TIFF", new TiffFileConverter(null)),
                new ConverterDescriptor("TSV", new DelimiterSeparatedConverter(null, '\t', "TSV", Set.of(".tsv", ".tab"))),
                new ConverterDescriptor("ODS", new OdsFileConverter(null)),
                new ConverterDescriptor("ODT", new OdtFileConverter(null)),
                new ConverterDescriptor("PPTX", new PptxFileConverter(null)),
                new ConverterDescriptor("XLS", new XlsFileConverter(null)),
                new ConverterDescriptor("XLSX", new XlsxFileConverter(null)),
                new ConverterDescriptor("DWG", new DwgFileConverter(null)),
                new ConverterDescriptor("EMF", new EmfFileConverter(null)),
                new ConverterDescriptor("WMF", new WmfFileConverter(null)),
                new ConverterDescriptor("ODP", new OdpFileConverter(null)),
                new ConverterDescriptor("PPT", new PptFileConverter(null)),
                new ConverterDescriptor("XML", new XmlFileConverter(null)),
                new ConverterDescriptor("DXF", new DxfFileConverter(null)),
                new ConverterDescriptor("DWF", new DwfFileConverter(null)),
                new ConverterDescriptor("DWFX", new DwfxFileConverter(null)),
                new ConverterDescriptor("DWT", new DwtFileConverter(null)),
                new ConverterDescriptor("HPGL", new HpglFileConverter(null)),
                new ConverterDescriptor("IGES", new IgesFileConverter(null)),
                new ConverterDescriptor("IGS", new IgsFileConverter(null)),
                new ConverterDescriptor("OBJ", new ObjFileConverter(null)),
                new ConverterDescriptor("PLT", new PltFileConverter(null)),
                new ConverterDescriptor("STEP", new StepFileConverter(null)),
                new ConverterDescriptor("STL", new StlFileConverter(null)),
                new ConverterDescriptor("STP", new StpFileConverter(null)),
                new ConverterDescriptor("3MF", new ThreeMfFileConverter(null)),
                new ConverterDescriptor("WRL", new WrlFileConverter(null)),
                new ConverterDescriptor("X3D", new X3dFileConverter(null))
        );
    }

    private static final List<ConverterDescriptor> ALL_CONVERTERS = createConvertersWithNullServices();

    /**
     * Property 4: Converter Null Parameter Validation — For any FileConverter
     * implementation, passing a null inputFile SHALL throw a FileConversionException
     * with message "Input file must not be null".
     *
     * **Validates: Requirements 2.2, 2.3**
     */
    @Property(tries = 25)
    @Tag("Feature: repo-efficiency-improvements, Property 4: Converter Null Parameter Validation")
    void nullInputFileThrowsFileConversionException(
            @ForAll("allConverterIndices") int converterIndex) {

        ConverterDescriptor descriptor = ALL_CONVERTERS.get(converterIndex);

        Throwable thrown = catchThrowable(() ->
                descriptor.converter().convertToPDF(null, "/tmp/output.pdf"));

        assertThat(thrown)
                .as("Converter %s should throw FileConversionException for null input", descriptor.formatName())
                .isInstanceOf(FileConversionException.class);

        assertThat(thrown.getMessage())
                .as("Converter %s should have correct null input message", descriptor.formatName())
                .isEqualTo("Input file must not be null");
    }

    /**
     * Property 4 (continued): For any FileConverter implementation, passing a null
     * outputFile SHALL throw a FileConversionException with message
     * "Output file path must not be null".
     *
     * **Validates: Requirements 2.2, 2.3**
     */
    @Property(tries = 25)
    @Tag("Feature: repo-efficiency-improvements, Property 4: Converter Null Parameter Validation")
    void nullOutputFileThrowsFileConversionException(
            @ForAll("allConverterIndices") int converterIndex) {

        ConverterDescriptor descriptor = ALL_CONVERTERS.get(converterIndex);

        MockMultipartFile inputFile = new MockMultipartFile(
                "file", "test.dat",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "test content".getBytes());

        Throwable thrown = catchThrowable(() ->
                descriptor.converter().convertToPDF(inputFile, null));

        assertThat(thrown)
                .as("Converter %s should throw FileConversionException for null output", descriptor.formatName())
                .isInstanceOf(FileConversionException.class);

        assertThat(thrown.getMessage())
                .as("Converter %s should have correct null output message", descriptor.formatName())
                .isEqualTo("Output file path must not be null");
    }

    @Provide
    Arbitrary<Integer> allConverterIndices() {
        return Arbitraries.integers().between(0, ALL_CONVERTERS.size() - 1);
    }
}
