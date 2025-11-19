package com.xtopdf.xtopdf.services;

import java.util.Objects;
import com.xtopdf.xtopdf.config.PageNumberConfig;
import com.xtopdf.xtopdf.config.WatermarkConfig;
import com.xtopdf.xtopdf.exceptions.FileConversionException;
import com.xtopdf.xtopdf.factories.BmpFileConverterFactory;
import com.xtopdf.xtopdf.factories.CsvFileConverterFactory;
import com.xtopdf.xtopdf.factories.DocFileConverterFactory;
import com.xtopdf.xtopdf.factories.GifFileConverterFactory;
import com.xtopdf.xtopdf.factories.HtmlFileConverterFactory;
import com.xtopdf.xtopdf.factories.JpegFileConverterFactory;
import com.xtopdf.xtopdf.factories.JsonFileConverterFactory;
import com.xtopdf.xtopdf.factories.MarkdownFileConverterFactory;
import com.xtopdf.xtopdf.factories.OdpFileConverterFactory;
import com.xtopdf.xtopdf.factories.OdsFileConverterFactory;
import com.xtopdf.xtopdf.factories.OdtFileConverterFactory;
import com.xtopdf.xtopdf.factories.PngFileConverterFactory;
import com.xtopdf.xtopdf.factories.PptFileConverterFactory;
import com.xtopdf.xtopdf.factories.PptxFileConverterFactory;
import com.xtopdf.xtopdf.factories.RtfFileConverterFactory;
import com.xtopdf.xtopdf.factories.SvgFileConverterFactory;
import com.xtopdf.xtopdf.factories.TiffFileConverterFactory;
import com.xtopdf.xtopdf.factories.XlsFileConverterFactory;
import com.xtopdf.xtopdf.factories.XlsxFileConverterFactory;
import com.xtopdf.xtopdf.factories.XmlFileConverterFactory;
import com.xtopdf.xtopdf.factories.DxfFileConverterFactory;
import com.xtopdf.xtopdf.factories.DwgFileConverterFactory;
import com.xtopdf.xtopdf.factories.DwtFileConverterFactory;
import com.xtopdf.xtopdf.factories.StepFileConverterFactory;
import com.xtopdf.xtopdf.factories.StpFileConverterFactory;
import com.xtopdf.xtopdf.factories.IgesFileConverterFactory;
import com.xtopdf.xtopdf.factories.IgsFileConverterFactory;
import com.xtopdf.xtopdf.factories.StlFileConverterFactory;
import com.xtopdf.xtopdf.factories.ObjFileConverterFactory;
import com.xtopdf.xtopdf.factories.ThreeMfFileConverterFactory;
import com.xtopdf.xtopdf.factories.WrlFileConverterFactory;
import com.xtopdf.xtopdf.factories.X3dFileConverterFactory;
import com.xtopdf.xtopdf.factories.DwfFileConverterFactory;
import com.xtopdf.xtopdf.factories.DwfxFileConverterFactory;
import com.xtopdf.xtopdf.factories.PltFileConverterFactory;
import com.xtopdf.xtopdf.factories.HpglFileConverterFactory;
import com.xtopdf.xtopdf.factories.EmfFileConverterFactory;
import com.xtopdf.xtopdf.factories.WmfFileConverterFactory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final DocFileConverterFactory docFileConverterFactory;
    private final HtmlFileConverterFactory htmlFileConverterFactory;
    private final JpegFileConverterFactory jpegFileConverterFactory;
    private final PngFileConverterFactory pngFileConverterFactory;
    private final XlsxFileConverterFactory xlsxFileConverterFactory;
    private final XlsFileConverterFactory xlsFileConverterFactory;
    private final CsvFileConverterFactory csvFileConverterFactory;
    private final BmpFileConverterFactory bmpFileConverterFactory;
    private final GifFileConverterFactory gifFileConverterFactory;
    private final PptxFileConverterFactory pptxFileConverterFactory;
    private final PptFileConverterFactory pptFileConverterFactory;
    private final RtfFileConverterFactory rtfFileConverterFactory;
    private final SvgFileConverterFactory svgFileConverterFactory;
    private final TiffFileConverterFactory tiffFileConverterFactory;
    private final MarkdownFileConverterFactory markdownFileConverterFactory;
    private final OdtFileConverterFactory odtFileConverterFactory;
    private final OdsFileConverterFactory odsFileConverterFactory;
    private final OdpFileConverterFactory odpFileConverterFactory;
    private final XmlFileConverterFactory xmlFileConverterFactory;
    private final JsonFileConverterFactory jsonFileConverterFactory;
    private final DxfFileConverterFactory dxfFileConverterFactory;
    private final DwgFileConverterFactory dwgFileConverterFactory;
    private final DwtFileConverterFactory dwtFileConverterFactory;
    private final StepFileConverterFactory stepFileConverterFactory;
    private final StpFileConverterFactory stpFileConverterFactory;
    private final IgesFileConverterFactory igesFileConverterFactory;
    private final IgsFileConverterFactory igsFileConverterFactory;
    private final StlFileConverterFactory stlFileConverterFactory;
    private final ObjFileConverterFactory objFileConverterFactory;
    private final ThreeMfFileConverterFactory threeMfFileConverterFactory;
    private final WrlFileConverterFactory wrlFileConverterFactory;
    private final X3dFileConverterFactory x3dFileConverterFactory;
    private final DwfFileConverterFactory dwfFileConverterFactory;
    private final DwfxFileConverterFactory dwfxFileConverterFactory;
    private final PltFileConverterFactory pltFileConverterFactory;
    private final HpglFileConverterFactory hpglFileConverterFactory;
    private final EmfFileConverterFactory emfFileConverterFactory;
    private final WmfFileConverterFactory wmfFileConverterFactory;
    private final PdfMergeService pdfMergeService;
    private final PageNumberService pageNumberService;
    private final WatermarkService watermarkService;

    public void convertFile(MultipartFile inputFile, String outputFile) throws FileConversionException {
        convertFile(inputFile, outputFile, null, null, PageNumberConfig.disabled(), WatermarkConfig.disabled(), false);
    }

    public void convertFile(MultipartFile inputFile, String outputFile, MultipartFile existingPdf, String position) throws FileConversionException {
        convertFile(inputFile, outputFile, existingPdf, position, PageNumberConfig.disabled(), WatermarkConfig.disabled(), false);
    }

    public void convertFile(MultipartFile inputFile, String outputFile, MultipartFile existingPdf, String position, PageNumberConfig pageNumberConfig) throws FileConversionException {
        convertFile(inputFile, outputFile, existingPdf, position, pageNumberConfig, WatermarkConfig.disabled(), false);
    }

    public void convertFile(MultipartFile inputFile, String outputFile, MultipartFile existingPdf, String position, PageNumberConfig pageNumberConfig, boolean executeMacros) throws FileConversionException {
        convertFile(inputFile, outputFile, existingPdf, position, pageNumberConfig, WatermarkConfig.disabled(), executeMacros);
    }

    public void convertFile(MultipartFile inputFile, String outputFile, MultipartFile existingPdf, String position, PageNumberConfig pageNumberConfig, WatermarkConfig watermarkConfig, boolean executeMacros) throws FileConversionException {
        FileConverterFactory factory = getFactoryForFile(Objects.requireNonNull(inputFile.getOriginalFilename()));

        if (Objects.nonNull(factory)) {
            FileConverter converter = factory.createFileConverter();
            // Convert the file using the conversion method with macro support
            converter.convertToPDF(inputFile, outputFile, executeMacros);
            
            // Add page numbers centrally if enabled
            if (pageNumberConfig.isEnabled()) {
                try {
                    java.io.File outputPdfFile = new java.io.File(outputFile);
                    pageNumberService.addPageNumbers(outputPdfFile, pageNumberConfig);
                } catch (java.io.IOException e) {
                    throw new FileConversionException("Failed to add page numbers: " + e.getMessage());
                }
            }
            
            // Add watermark if enabled
            if (watermarkConfig.isEnabled()) {
                try {
                    java.io.File outputPdfFile = new java.io.File(outputFile);
                    watermarkService.addWatermark(outputPdfFile, watermarkConfig);
                } catch (java.io.IOException e) {
                    throw new FileConversionException("Failed to add watermark: " + e.getMessage());
                }
            }
            
            // If an existing PDF is provided, merge it with the converted PDF
            if (existingPdf != null && !existingPdf.isEmpty()) {
                try {
                    java.io.File outputPdfFile = new java.io.File(outputFile);
                    pdfMergeService.mergePdfs(outputPdfFile, existingPdf, position);
                } catch (java.io.IOException e) {
                    throw new FileConversionException("Failed to merge PDF files: " + e.getMessage());
                }
            }
        } else {
            throw new FileConversionException("Failed to convert file: " + inputFile.getName());
        }
    }

    FileConverterFactory getFactoryForFile(String inputFile) {
        String lowerCaseFileName = inputFile.toLowerCase();
        int dotIndex = lowerCaseFileName.lastIndexOf('.');
        
        if (dotIndex == -1) {
            log.error("No converter found for file {}", inputFile);
            return null;
        }
        
        String extension = lowerCaseFileName.substring(dotIndex);
        
        return switch (extension) {
            case ".txt" -> txtFileConverterFactory;
            case ".docx" -> docxFileConverterFactory;
            case ".doc" -> docFileConverterFactory;
            case ".html" -> htmlFileConverterFactory;
            case ".jpeg", ".jpg" -> jpegFileConverterFactory;
            case ".png" -> pngFileConverterFactory;
            case ".xlsx" -> xlsxFileConverterFactory;
            case ".xls" -> xlsFileConverterFactory;
            case ".csv" -> csvFileConverterFactory;
            case ".bmp" -> bmpFileConverterFactory;
            case ".gif" -> gifFileConverterFactory;
            case ".pptx" -> pptxFileConverterFactory;
            case ".ppt" -> pptFileConverterFactory;
            case ".rtf" -> rtfFileConverterFactory;
            case ".svg" -> svgFileConverterFactory;
            case ".tiff", ".tif" -> tiffFileConverterFactory;
            case ".md", ".markdown" -> markdownFileConverterFactory;
            case ".odt" -> odtFileConverterFactory;
            case ".ods" -> odsFileConverterFactory;
            case ".odp" -> odpFileConverterFactory;
            case ".xml" -> xmlFileConverterFactory;
            case ".json" -> jsonFileConverterFactory;
            case ".dxf" -> dxfFileConverterFactory;
            case ".dwg" -> dwgFileConverterFactory;
            case ".dwt" -> dwtFileConverterFactory;
            case ".step" -> stepFileConverterFactory;
            case ".stp" -> stpFileConverterFactory;
            case ".iges" -> igesFileConverterFactory;
            case ".igs" -> igsFileConverterFactory;
            case ".stl" -> stlFileConverterFactory;
            case ".obj" -> objFileConverterFactory;
            case ".3mf" -> threeMfFileConverterFactory;
            case ".wrl" -> wrlFileConverterFactory;
            case ".x3d" -> x3dFileConverterFactory;
            case ".dwf" -> dwfFileConverterFactory;
            case ".dwfx" -> dwfxFileConverterFactory;
            case ".plt" -> pltFileConverterFactory;
            case ".hpgl" -> hpglFileConverterFactory;
            case ".emf" -> emfFileConverterFactory;
            case ".wmf" -> wmfFileConverterFactory;
            default -> {
                log.error("No converter found for file {}", inputFile);
                yield null;
            }
        };
    }

}
