package com.xtopdf.xtopdf.converters;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for getSupportedExtensions() on all FileConverter implementations.
 * Validates: Requirements 1.2, 1.5
 */
class FileConverterExtensionsTest {

    // --- Per-converter extension correctness tests ---

    @ParameterizedTest(name = "{0} returns {1}")
    @MethodSource("converterExtensionMappings")
    void converterReturnCorrectExtensions(String converterName, Set<String> expectedExtensions, FileConverter converter) {
        assertThat(converter.getSupportedExtensions()).isEqualTo(expectedExtensions);
    }

    @ParameterizedTest(name = "{0} extensions all start with a dot")
    @MethodSource("allConverters")
    void allExtensionsStartWithDot(String converterName, FileConverter converter) {
        Set<String> extensions = converter.getSupportedExtensions();
        assertThat(extensions).allSatisfy(ext ->
                assertThat(ext).startsWith(".")
        );
    }

    @ParameterizedTest(name = "{0} returns a non-empty set")
    @MethodSource("allConverters")
    void noConverterReturnsEmptySet(String converterName, FileConverter converter) {
        assertThat(converter.getSupportedExtensions()).isNotEmpty();
    }

    @Test
    void jpegConverterReturnsBothJpgAndJpeg() {
        var converter = createJpegFileConverter();
        assertThat(converter.getSupportedExtensions()).containsExactlyInAnyOrder(".jpg", ".jpeg");
    }

    @Test
    void tsvConverterReturnsBothTsvAndTab() {
        var converter = createTsvFileConverter();
        assertThat(converter.getSupportedExtensions()).containsExactlyInAnyOrder(".tsv", ".tab");
    }

    @Test
    void tiffConverterReturnsBothTiffAndTif() {
        var converter = createTiffFileConverter();
        assertThat(converter.getSupportedExtensions()).containsExactlyInAnyOrder(".tiff", ".tif");
    }

    @Test
    void markdownConverterReturnsBothMdAndMarkdown() {
        var converter = createMarkdownFileConverter();
        assertThat(converter.getSupportedExtensions()).containsExactlyInAnyOrder(".md", ".markdown");
    }

    // --- Data providers ---

    static Stream<Arguments> converterExtensionMappings() {
        return Stream.of(
                Arguments.of("TxtFileConverter", Set.of(".txt"), createTxtFileConverter()),
                Arguments.of("DocxFileConverter", Set.of(".docx"), createDocxFileConverter()),
                Arguments.of("DocFileConverter", Set.of(".doc"), createDocFileConverter()),
                Arguments.of("HtmlFileConverter", Set.of(".html"), createHtmlFileConverter()),
                Arguments.of("JpegFileConverter", Set.of(".jpg", ".jpeg"), createJpegFileConverter()),
                Arguments.of("PngFileConverter", Set.of(".png"), createPngFileConverter()),
                Arguments.of("XlsxFileConverter", Set.of(".xlsx"), createXlsxFileConverter()),
                Arguments.of("XlsFileConverter", Set.of(".xls"), createXlsFileConverter()),
                Arguments.of("CsvFileConverter", Set.of(".csv"), createCsvFileConverter()),
                Arguments.of("TsvFileConverter", Set.of(".tsv", ".tab"), createTsvFileConverter()),
                Arguments.of("BmpFileConverter", Set.of(".bmp"), createBmpFileConverter()),
                Arguments.of("GifFileConverter", Set.of(".gif"), createGifFileConverter()),
                Arguments.of("PptxFileConverter", Set.of(".pptx"), createPptxFileConverter()),
                Arguments.of("PptFileConverter", Set.of(".ppt"), createPptFileConverter()),
                Arguments.of("RtfFileConverter", Set.of(".rtf"), createRtfFileConverter()),
                Arguments.of("SvgFileConverter", Set.of(".svg"), createSvgFileConverter()),
                Arguments.of("TiffFileConverter", Set.of(".tiff", ".tif"), createTiffFileConverter()),
                Arguments.of("MarkdownFileConverter", Set.of(".md", ".markdown"), createMarkdownFileConverter()),
                Arguments.of("OdtFileConverter", Set.of(".odt"), createOdtFileConverter()),
                Arguments.of("OdsFileConverter", Set.of(".ods"), createOdsFileConverter()),
                Arguments.of("OdpFileConverter", Set.of(".odp"), createOdpFileConverter()),
                Arguments.of("XmlFileConverter", Set.of(".xml"), createXmlFileConverter()),
                Arguments.of("JsonFileConverter", Set.of(".json"), createJsonFileConverter()),
                Arguments.of("DxfFileConverter", Set.of(".dxf"), createDxfFileConverter()),
                Arguments.of("DwgFileConverter", Set.of(".dwg"), createDwgFileConverter()),
                Arguments.of("DwtFileConverter", Set.of(".dwt"), createDwtFileConverter()),
                Arguments.of("StepFileConverter", Set.of(".step"), createStepFileConverter()),
                Arguments.of("StpFileConverter", Set.of(".stp"), createStpFileConverter()),
                Arguments.of("IgesFileConverter", Set.of(".iges"), createIgesFileConverter()),
                Arguments.of("IgsFileConverter", Set.of(".igs"), createIgsFileConverter()),
                Arguments.of("StlFileConverter", Set.of(".stl"), createStlFileConverter()),
                Arguments.of("ObjFileConverter", Set.of(".obj"), createObjFileConverter()),
                Arguments.of("ThreeMfFileConverter", Set.of(".3mf"), createThreeMfFileConverter()),
                Arguments.of("WrlFileConverter", Set.of(".wrl"), createWrlFileConverter()),
                Arguments.of("X3dFileConverter", Set.of(".x3d"), createX3dFileConverter()),
                Arguments.of("DwfFileConverter", Set.of(".dwf"), createDwfFileConverter()),
                Arguments.of("DwfxFileConverter", Set.of(".dwfx"), createDwfxFileConverter()),
                Arguments.of("PltFileConverter", Set.of(".plt"), createPltFileConverter()),
                Arguments.of("HpglFileConverter", Set.of(".hpgl"), createHpglFileConverter()),
                Arguments.of("EmfFileConverter", Set.of(".emf"), createEmfFileConverter()),
                Arguments.of("WmfFileConverter", Set.of(".wmf"), createWmfFileConverter())
        );
    }

    static Stream<Arguments> allConverters() {
        return converterExtensionMappings().map(args -> {
            Object[] original = args.get();
            return Arguments.of(original[0], original[2]);
        });
    }

    // --- Factory methods (pass null for service dependencies since we only test getSupportedExtensions) ---

    private static TxtFileConverter createTxtFileConverter() { return new TxtFileConverter(null); }
    private static DocxFileConverter createDocxFileConverter() { return new DocxFileConverter(null); }
    private static DocFileConverter createDocFileConverter() { return new DocFileConverter(null); }
    private static HtmlFileConverter createHtmlFileConverter() { return new HtmlFileConverter(null); }
    private static JpegFileConverter createJpegFileConverter() { return new JpegFileConverter(null); }
    private static PngFileConverter createPngFileConverter() { return new PngFileConverter(null); }
    private static XlsxFileConverter createXlsxFileConverter() { return new XlsxFileConverter(null); }
    private static XlsFileConverter createXlsFileConverter() { return new XlsFileConverter(null); }
    private static CsvFileConverter createCsvFileConverter() { return new CsvFileConverter(null); }
    private static TsvFileConverter createTsvFileConverter() { return new TsvFileConverter(null); }
    private static BmpFileConverter createBmpFileConverter() { return new BmpFileConverter(null); }
    private static GifFileConverter createGifFileConverter() { return new GifFileConverter(null); }
    private static PptxFileConverter createPptxFileConverter() { return new PptxFileConverter(null); }
    private static PptFileConverter createPptFileConverter() { return new PptFileConverter(null); }
    private static RtfFileConverter createRtfFileConverter() { return new RtfFileConverter(null); }
    private static SvgFileConverter createSvgFileConverter() { return new SvgFileConverter(null); }
    private static TiffFileConverter createTiffFileConverter() { return new TiffFileConverter(null); }
    private static MarkdownFileConverter createMarkdownFileConverter() { return new MarkdownFileConverter(null); }
    private static OdtFileConverter createOdtFileConverter() { return new OdtFileConverter(null); }
    private static OdsFileConverter createOdsFileConverter() { return new OdsFileConverter(null); }
    private static OdpFileConverter createOdpFileConverter() { return new OdpFileConverter(null); }
    private static XmlFileConverter createXmlFileConverter() { return new XmlFileConverter(null); }
    private static JsonFileConverter createJsonFileConverter() { return new JsonFileConverter(null); }
    private static DxfFileConverter createDxfFileConverter() { return new DxfFileConverter(null); }
    private static DwgFileConverter createDwgFileConverter() { return new DwgFileConverter(null); }
    private static DwtFileConverter createDwtFileConverter() { return new DwtFileConverter(null); }
    private static StepFileConverter createStepFileConverter() { return new StepFileConverter(null); }
    private static StpFileConverter createStpFileConverter() { return new StpFileConverter(null); }
    private static IgesFileConverter createIgesFileConverter() { return new IgesFileConverter(null); }
    private static IgsFileConverter createIgsFileConverter() { return new IgsFileConverter(null); }
    private static StlFileConverter createStlFileConverter() { return new StlFileConverter(null); }
    private static ObjFileConverter createObjFileConverter() { return new ObjFileConverter(null); }
    private static ThreeMfFileConverter createThreeMfFileConverter() { return new ThreeMfFileConverter(null); }
    private static WrlFileConverter createWrlFileConverter() { return new WrlFileConverter(null); }
    private static X3dFileConverter createX3dFileConverter() { return new X3dFileConverter(null); }
    private static DwfFileConverter createDwfFileConverter() { return new DwfFileConverter(null); }
    private static DwfxFileConverter createDwfxFileConverter() { return new DwfxFileConverter(null); }
    private static PltFileConverter createPltFileConverter() { return new PltFileConverter(null); }
    private static HpglFileConverter createHpglFileConverter() { return new HpglFileConverter(null); }
    private static EmfFileConverter createEmfFileConverter() { return new EmfFileConverter(null); }
    private static WmfFileConverter createWmfFileConverter() { return new WmfFileConverter(null); }
}
