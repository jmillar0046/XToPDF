package com.xtopdf.xtopdf.services;

import java.util.Objects;
import com.xtopdf.xtopdf.exceptions.FileConversionException;
import com.xtopdf.xtopdf.factories.BmpFileConverterFactory;
import com.xtopdf.xtopdf.factories.HtmlFileConverterFactory;
import com.xtopdf.xtopdf.factories.JpegFileConverterFactory;
import com.xtopdf.xtopdf.factories.PngFileConverterFactory;
import com.xtopdf.xtopdf.factories.PptxFileConverterFactory;
import com.xtopdf.xtopdf.factories.RtfFileConverterFactory;
import com.xtopdf.xtopdf.factories.SvgFileConverterFactory;
import com.xtopdf.xtopdf.factories.TiffFileConverterFactory;
import com.xtopdf.xtopdf.factories.XlsxFileConverterFactory;
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
    private final HtmlFileConverterFactory htmlFileConverterFactory;
    private final JpegFileConverterFactory jpegFileConverterFactory;
    private final PngFileConverterFactory pngFileConverterFactory;
    private final XlsxFileConverterFactory xlsxFileConverterFactory;
    private final BmpFileConverterFactory bmpFileConverterFactory;
    private final PptxFileConverterFactory pptxFileConverterFactory;
    private final RtfFileConverterFactory rtfFileConverterFactory;
    private final SvgFileConverterFactory svgFileConverterFactory;
    private final TiffFileConverterFactory tiffFileConverterFactory;
    private final PdfMergeService pdfMergeService;

    public void convertFile(MultipartFile inputFile, String outputFile) throws FileConversionException {
        convertFile(inputFile, outputFile, null, null);
    }

    public void convertFile(MultipartFile inputFile, String outputFile, MultipartFile existingPdf, String position) throws FileConversionException {
        FileConverterFactory factory = getFactoryForFile(Objects.requireNonNull(inputFile.getOriginalFilename()));

        if (Objects.nonNull(factory)) {
            FileConverter converter = factory.createFileConverter();
            converter.convertToPDF(inputFile, outputFile);
            
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
        if(inputFile.toLowerCase().endsWith(".txt")){
            return txtFileConverterFactory;
        } else if (inputFile.toLowerCase().endsWith(".docx")){
            return docxFileConverterFactory;
        } else if (inputFile.toLowerCase().endsWith(".html")){
            return htmlFileConverterFactory;
        } else if (inputFile.toLowerCase().endsWith(".jpeg") || inputFile.toLowerCase().endsWith(".jpg")){
            return jpegFileConverterFactory;
        } else if (inputFile.toLowerCase().endsWith(".png")){
            return pngFileConverterFactory;
        } else if (inputFile.toLowerCase().endsWith(".xlsx")){
            return xlsxFileConverterFactory;
        } else if (inputFile.toLowerCase().endsWith(".bmp")){
            return bmpFileConverterFactory;
        } else if (inputFile.toLowerCase().endsWith(".pptx")){
            return pptxFileConverterFactory;
        } else if (inputFile.toLowerCase().endsWith(".rtf")){
            return rtfFileConverterFactory;
        } else if (inputFile.toLowerCase().endsWith(".svg")){
            return svgFileConverterFactory;
        } else if (inputFile.toLowerCase().endsWith(".tiff") || inputFile.toLowerCase().endsWith(".tif")){
            return tiffFileConverterFactory;
        } else {
            log.error("No converter found for file {}", inputFile);
            return null;
        }
    }

}
