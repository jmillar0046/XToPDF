package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.factories.*;
import net.jqwik.api.*;
import org.junit.jupiter.api.Tag;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for TSV file routing in FileConversionService.
 * Tests case-insensitive extension handling.
 */
class FileConversionServiceTsvPropertyTest {

    @Property(tries = 100)
    @Tag("Feature: tsv-file-support, Property 4: Case-Insensitive File Extension Routing")
    void caseInsensitiveRoutingForTsv(@ForAll("tsvExtensions") String extension) {
        // Create service with mocked factories
        TsvFileConverterFactory tsvFactory = mock(TsvFileConverterFactory.class);
        FileConversionService service = createServiceWithTsvFactory(tsvFactory);
        
        // Test with random case variation
        String filename = "test" + extension;
        FileConverterFactory factory = service.getFactoryForFile(filename);
        
        // Verify TSV factory is returned
        assertSame(tsvFactory, factory, 
                "Should return TsvFileConverterFactory for extension: " + extension);
    }

    @Provide
    Arbitrary<String> tsvExtensions() {
        // Generate random case variations of .tsv and .tab
        return Arbitraries.of(".tsv", ".tab", ".TSV", ".TAB", ".Tsv", ".Tab", 
                              ".tSv", ".tAb", ".TsV", ".TaB", ".tsV", ".taB");
    }

    @Property(tries = 100)
    @Tag("Feature: tsv-file-support, Property 4: Case-Insensitive File Extension Routing")
    void nonTsvExtensionsDoNotRoutToTsvFactory(@ForAll("nonTsvExtensions") String extension) {
        // Create service with mocked factories
        TsvFileConverterFactory tsvFactory = mock(TsvFileConverterFactory.class);
        FileConversionService service = createServiceWithTsvFactory(tsvFactory);
        
        // Test with non-TSV extension
        String filename = "test" + extension;
        FileConverterFactory factory = service.getFactoryForFile(filename);
        
        // Verify TSV factory is NOT returned
        if (factory != null) {
            assertNotSame(tsvFactory, factory,
                    "Should not return TsvFileConverterFactory for extension: " + extension);
        }
    }

    @Provide
    Arbitrary<String> nonTsvExtensions() {
        return Arbitraries.of(".txt", ".csv", ".pdf", ".doc", ".docx", ".xls", ".xlsx",
                              ".json", ".xml", ".html", ".md", ".rtf");
    }

    private FileConversionService createServiceWithTsvFactory(TsvFileConverterFactory tsvFactory) {
        // Create all required mocks
        TxtFileConverterFactory txtFactory = mock(TxtFileConverterFactory.class);
        DocxFileConverterFactory docxFactory = mock(DocxFileConverterFactory.class);
        DocFileConverterFactory docFactory = mock(DocFileConverterFactory.class);
        HtmlFileConverterFactory htmlFactory = mock(HtmlFileConverterFactory.class);
        JpegFileConverterFactory jpegFactory = mock(JpegFileConverterFactory.class);
        PngFileConverterFactory pngFactory = mock(PngFileConverterFactory.class);
        XlsxFileConverterFactory xlsxFactory = mock(XlsxFileConverterFactory.class);
        XlsFileConverterFactory xlsFactory = mock(XlsFileConverterFactory.class);
        CsvFileConverterFactory csvFactory = mock(CsvFileConverterFactory.class);
        BmpFileConverterFactory bmpFactory = mock(BmpFileConverterFactory.class);
        GifFileConverterFactory gifFactory = mock(GifFileConverterFactory.class);
        PptxFileConverterFactory pptxFactory = mock(PptxFileConverterFactory.class);
        PptFileConverterFactory pptFactory = mock(PptFileConverterFactory.class);
        RtfFileConverterFactory rtfFactory = mock(RtfFileConverterFactory.class);
        SvgFileConverterFactory svgFactory = mock(SvgFileConverterFactory.class);
        TiffFileConverterFactory tiffFactory = mock(TiffFileConverterFactory.class);
        MarkdownFileConverterFactory mdFactory = mock(MarkdownFileConverterFactory.class);
        OdtFileConverterFactory odtFactory = mock(OdtFileConverterFactory.class);
        OdsFileConverterFactory odsFactory = mock(OdsFileConverterFactory.class);
        OdpFileConverterFactory odpFactory = mock(OdpFileConverterFactory.class);
        XmlFileConverterFactory xmlFactory = mock(XmlFileConverterFactory.class);
        JsonFileConverterFactory jsonFactory = mock(JsonFileConverterFactory.class);
        DxfFileConverterFactory dxfFactory = mock(DxfFileConverterFactory.class);
        DwgFileConverterFactory dwgFactory = mock(DwgFileConverterFactory.class);
        DwtFileConverterFactory dwtFactory = mock(DwtFileConverterFactory.class);
        StepFileConverterFactory stepFactory = mock(StepFileConverterFactory.class);
        StpFileConverterFactory stpFactory = mock(StpFileConverterFactory.class);
        IgesFileConverterFactory igesFactory = mock(IgesFileConverterFactory.class);
        IgsFileConverterFactory igsFactory = mock(IgsFileConverterFactory.class);
        StlFileConverterFactory stlFactory = mock(StlFileConverterFactory.class);
        ObjFileConverterFactory objFactory = mock(ObjFileConverterFactory.class);
        ThreeMfFileConverterFactory threeMfFactory = mock(ThreeMfFileConverterFactory.class);
        WrlFileConverterFactory wrlFactory = mock(WrlFileConverterFactory.class);
        X3dFileConverterFactory x3dFactory = mock(X3dFileConverterFactory.class);
        DwfFileConverterFactory dwfFactory = mock(DwfFileConverterFactory.class);
        DwfxFileConverterFactory dwfxFactory = mock(DwfxFileConverterFactory.class);
        PltFileConverterFactory pltFactory = mock(PltFileConverterFactory.class);
        HpglFileConverterFactory hpglFactory = mock(HpglFileConverterFactory.class);
        EmfFileConverterFactory emfFactory = mock(EmfFileConverterFactory.class);
        WmfFileConverterFactory wmfFactory = mock(WmfFileConverterFactory.class);
        PdfMergeService pdfMergeService = mock(PdfMergeService.class);
        PageNumberService pageNumberService = mock(PageNumberService.class);
        WatermarkService watermarkService = mock(WatermarkService.class);
        ContainerOrchestrationService containerService = mock(ContainerOrchestrationService.class);

        return new FileConversionService(
                txtFactory, docxFactory, docFactory, htmlFactory,
                jpegFactory, pngFactory, xlsxFactory, xlsFactory,
                csvFactory, tsvFactory, bmpFactory, gifFactory, pptxFactory,
                pptFactory, rtfFactory, svgFactory, tiffFactory,
                mdFactory, odtFactory, odsFactory, odpFactory,
                xmlFactory, jsonFactory, dxfFactory, dwgFactory,
                dwtFactory, stepFactory, stpFactory, igesFactory,
                igsFactory, stlFactory, objFactory,
                threeMfFactory, wrlFactory, x3dFactory,
                dwfFactory, dwfxFactory, pltFactory,
                hpglFactory, emfFactory, wmfFactory,
                pdfMergeService, pageNumberService, watermarkService, containerService
        );
    }
}
