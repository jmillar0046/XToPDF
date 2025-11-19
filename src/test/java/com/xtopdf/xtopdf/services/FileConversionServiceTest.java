package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.config.PageNumberConfig;
import com.xtopdf.xtopdf.exceptions.FileConversionException;
import com.xtopdf.xtopdf.factories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileConversionServiceTest {

    // Existing mocks
    @Mock private TxtFileConverterFactory txtFileConverterFactory;
    @Mock private DocxFileConverterFactory docxFileConverterFactory;
    @Mock private DocFileConverterFactory docFileConverterFactory;
    @Mock private HtmlFileConverterFactory htmlFileConverterFactory;
    @Mock private JpegFileConverterFactory jpegFileConverterFactory;
    @Mock private PngFileConverterFactory pngFileConverterFactory;
    @Mock private XlsxFileConverterFactory xlsxFileConverterFactory;
    @Mock private XlsFileConverterFactory xlsFileConverterFactory;
    @Mock private CsvFileConverterFactory csvFileConverterFactory;
    @Mock private BmpFileConverterFactory bmpFileConverterFactory;
    @Mock private GifFileConverterFactory gifFileConverterFactory;
    @Mock private PptxFileConverterFactory pptxFileConverterFactory;
    @Mock private PptFileConverterFactory pptFileConverterFactory;
    @Mock private RtfFileConverterFactory rtfFileConverterFactory;
    @Mock private SvgFileConverterFactory svgFileConverterFactory;
    @Mock private TiffFileConverterFactory tiffFileConverterFactory;
    @Mock private MarkdownFileConverterFactory markdownFileConverterFactory;
    @Mock private OdtFileConverterFactory odtFileConverterFactory;
    @Mock private OdsFileConverterFactory odsFileConverterFactory;
    @Mock private OdpFileConverterFactory odpFileConverterFactory;
    @Mock private XmlFileConverterFactory xmlFileConverterFactory;
    @Mock private JsonFileConverterFactory jsonFileConverterFactory;
    @Mock private DxfFileConverterFactory dxfFileConverterFactory;
    @Mock private DwgFileConverterFactory dwgFileConverterFactory;
    
    // New mocks for CAD formats
    @Mock private DwtFileConverterFactory dwtFileConverterFactory;
    @Mock private StepFileConverterFactory stepFileConverterFactory;
    @Mock private StpFileConverterFactory stpFileConverterFactory;
    @Mock private IgesFileConverterFactory igesFileConverterFactory;
    @Mock private IgsFileConverterFactory igsFileConverterFactory;
    @Mock private XTFileConverterFactory xtFileConverterFactory;
    @Mock private XBFileConverterFactory xbFileConverterFactory;
    @Mock private StlFileConverterFactory stlFileConverterFactory;
    @Mock private ObjFileConverterFactory objFileConverterFactory;
    @Mock private SldprtFileConverterFactory sldprtFileConverterFactory;
    @Mock private SldasmFileConverterFactory sldasmFileConverterFactory;
    @Mock private SlddrawFileConverterFactory slddrawFileConverterFactory;
    @Mock private CatpartFileConverterFactory catpartFileConverterFactory;
    @Mock private CatproductFileConverterFactory catproductFileConverterFactory;
    @Mock private CatdrawingFileConverterFactory catdrawingFileConverterFactory;
    @Mock private PrtFileConverterFactory prtFileConverterFactory;
    @Mock private AsmFileConverterFactory asmFileConverterFactory;
    @Mock private DrwFileConverterFactory drwFileConverterFactory;
    @Mock private IptFileConverterFactory iptFileConverterFactory;
    @Mock private IamFileConverterFactory iamFileConverterFactory;
    @Mock private IdwFileConverterFactory idwFileConverterFactory;
    @Mock private IpnFileConverterFactory ipnFileConverterFactory;
    @Mock private F3dFileConverterFactory f3dFileConverterFactory;
    @Mock private F3zFileConverterFactory f3zFileConverterFactory;
    @Mock private SkpFileConverterFactory skpFileConverterFactory;
    @Mock private DgnFileConverterFactory dgnFileConverterFactory;
    @Mock private ThreeDmFileConverterFactory threeDmFileConverterFactory;
    @Mock private RvtFileConverterFactory rvtFileConverterFactory;
    @Mock private RfaFileConverterFactory rfaFileConverterFactory;
    @Mock private JtFileConverterFactory jtFileConverterFactory;
    @Mock private ThreeMfFileConverterFactory threeMfFileConverterFactory;
    @Mock private WrlFileConverterFactory wrlFileConverterFactory;
    @Mock private X3dFileConverterFactory x3dFileConverterFactory;
    @Mock private DwfFileConverterFactory dwfFileConverterFactory;
    @Mock private DwfxFileConverterFactory dwfxFileConverterFactory;
    @Mock private PltFileConverterFactory pltFileConverterFactory;
    @Mock private HpglFileConverterFactory hpglFileConverterFactory;
    @Mock private EmfFileConverterFactory emfFileConverterFactory;
    @Mock private WmfFileConverterFactory wmfFileConverterFactory;
    
    @Mock private PdfMergeService pdfMergeService;
    @Mock private PageNumberService pageNumberService;
    @Mock private WatermarkService watermarkService;
    @Mock private FileConverter mockConverter;

    private FileConversionService fileConversionService;

    @BeforeEach
    void setUp() {
        fileConversionService = new FileConversionService(
            txtFileConverterFactory, docxFileConverterFactory, docFileConverterFactory, htmlFileConverterFactory,
            jpegFileConverterFactory, pngFileConverterFactory, xlsxFileConverterFactory, xlsFileConverterFactory,
            csvFileConverterFactory, bmpFileConverterFactory, gifFileConverterFactory, pptxFileConverterFactory,
            pptFileConverterFactory, rtfFileConverterFactory, svgFileConverterFactory, tiffFileConverterFactory,
            markdownFileConverterFactory, odtFileConverterFactory, odsFileConverterFactory, odpFileConverterFactory,
            xmlFileConverterFactory, jsonFileConverterFactory, dxfFileConverterFactory, dwgFileConverterFactory,
            dwtFileConverterFactory, stepFileConverterFactory, stpFileConverterFactory, igesFileConverterFactory,
            igsFileConverterFactory, xtFileConverterFactory, xbFileConverterFactory, stlFileConverterFactory,
            objFileConverterFactory, sldprtFileConverterFactory, sldasmFileConverterFactory, slddrawFileConverterFactory,
            catpartFileConverterFactory, catproductFileConverterFactory, catdrawingFileConverterFactory, prtFileConverterFactory,
            asmFileConverterFactory, drwFileConverterFactory, iptFileConverterFactory, iamFileConverterFactory,
            idwFileConverterFactory, ipnFileConverterFactory, f3dFileConverterFactory, f3zFileConverterFactory,
            skpFileConverterFactory, dgnFileConverterFactory, threeDmFileConverterFactory, rvtFileConverterFactory,
            rfaFileConverterFactory, jtFileConverterFactory, threeMfFileConverterFactory, wrlFileConverterFactory,
            x3dFileConverterFactory, dwfFileConverterFactory, dwfxFileConverterFactory, pltFileConverterFactory,
            hpglFileConverterFactory, emfFileConverterFactory, wmfFileConverterFactory,
            pdfMergeService, pageNumberService, watermarkService
        );
    }

    @Test
    void testConvertFile_TxtFile() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "content".getBytes());
        when(txtFileConverterFactory.createFileConverter()).thenReturn(mockConverter);
        
        fileConversionService.convertFile(inputFile, "output.pdf");
        
        verify(mockConverter).convertToPDF(any(), eq("output.pdf"), eq(false));
    }
    
    @Test
    void testConvertFile_StepFile() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("file", "test.step", MediaType.APPLICATION_OCTET_STREAM_VALUE, "content".getBytes());
        when(stepFileConverterFactory.createFileConverter()).thenReturn(mockConverter);
        
        fileConversionService.convertFile(inputFile, "output.pdf");
        
        verify(mockConverter).convertToPDF(any(), eq("output.pdf"), eq(false));
    }
    
    @Test
    void testConvertFile_StlFile() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("file", "test.stl", MediaType.APPLICATION_OCTET_STREAM_VALUE, "content".getBytes());
        when(stlFileConverterFactory.createFileConverter()).thenReturn(mockConverter);
        
        fileConversionService.convertFile(inputFile, "output.pdf");
        
        verify(mockConverter).convertToPDF(any(), eq("output.pdf"), eq(false));
    }

    @ParameterizedTest
    @CsvSource({
        "test.txt,txt", "test.docx,docx", "test.xlsx,xlsx", "test.pptx,pptx",
        "test.step,step", "test.stl,stl", "test.obj,obj", "test.dwt,dwt",
        "test.sldprt,sldprt", "test.catpart,catpart", "test.ipt,ipt",
        "test.f3d,f3d", "test.skp,skp", "test.rvt,rvt", "test.jt,jt",
        "test.3dm,3dm", "test.dwf,dwf", "test.plt,plt", "test.emf,emf"
    })
    void testGetFactoryForFile_ValidExtensions(String filename, String format) {
        var factory = fileConversionService.getFactoryForFile(filename);
        // Should return a non-null factory
        assert(factory != null);
    }
    
    @Test
    void testGetFactoryForFile_UnknownExtension() {
        var factory = fileConversionService.getFactoryForFile("test.unknown");
        assertEquals(null, factory);
    }
    
    @Test
    void testConvertFile_NoExtension() {
        MockMultipartFile inputFile = new MockMultipartFile("file", "testfile", MediaType.TEXT_PLAIN_VALUE, "content".getBytes());
        
        assertThrows(FileConversionException.class, () -> {
            fileConversionService.convertFile(inputFile, "output.pdf");
        });
    }
}
