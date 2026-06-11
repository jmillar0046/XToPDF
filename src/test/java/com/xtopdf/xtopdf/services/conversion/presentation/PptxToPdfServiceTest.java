package com.xtopdf.xtopdf.services.conversion.presentation;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import org.apache.poi.xslf.usermodel.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PptxToPdfServiceTest {

    @Mock
    private PdfBackendProvider pdfBackend;

    @Mock
    private PdfDocumentBuilder builder;

    private PptxToPdfService pptxToPdfService;

    @BeforeEach
    void setUp() throws IOException {
        lenient().when(pdfBackend.createBuilder()).thenReturn(builder);
        pptxToPdfService = new PptxToPdfService(pdfBackend);
    }

    // --- Requirement 2.1: Each slide renders as separate PDF page ---

    @Test
    void eachSlideRendersAsSeparatePdfPage(@TempDir Path tempDir) throws Exception {
        ClassPathResource resource = new ClassPathResource("test-files/test.pptx");
        byte[] pptxData = Files.readAllBytes(resource.getFile().toPath());

        MockMultipartFile pptxFile = new MockMultipartFile(
                "file", "test.pptx",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                pptxData);

        File pdfFile = tempDir.resolve("output.pdf").toFile();

        pptxToPdfService.convertPptxToPdf(pptxFile, pdfFile);

        // newPage should be called at least once (once per slide)
        verify(builder, atLeastOnce()).newPage(anyFloat(), anyFloat());
        verify(builder).save(pdfFile);
    }

    // --- Requirement 2.2: Text shape positioning via anchor coordinates ---

    @Test
    void textShapeRendersFormattedText() throws IOException {
        XSLFTextShape textShape = mock(XSLFTextShape.class);
        XSLFTextParagraph para = mock(XSLFTextParagraph.class);
        XSLFTextRun run = mock(XSLFTextRun.class);

        when(textShape.getTextParagraphs()).thenReturn(List.of(para));
        when(para.getTextRuns()).thenReturn(List.of(run));
        when(run.getRawText()).thenReturn("Hello World");
        when(run.isBold()).thenReturn(false);
        when(run.isItalic()).thenReturn(false);
        when(run.getFontSize()).thenReturn(18.0);

        pptxToPdfService.renderTextShape(builder, textShape);

        verify(builder).addFormattedText("Hello World", false, false, 18f);
        verify(builder).endParagraph();
    }

    // --- Requirement 2.4: Per-run formatting (bold, italic, font size) ---

    @Test
    void boldRunRendersWithBoldStyle() throws IOException {
        XSLFTextShape textShape = mock(XSLFTextShape.class);
        XSLFTextParagraph para = mock(XSLFTextParagraph.class);
        XSLFTextRun run = mock(XSLFTextRun.class);

        when(textShape.getTextParagraphs()).thenReturn(List.of(para));
        when(para.getTextRuns()).thenReturn(List.of(run));
        when(run.getRawText()).thenReturn("Bold text");
        when(run.isBold()).thenReturn(true);
        when(run.isItalic()).thenReturn(false);
        when(run.getFontSize()).thenReturn(14.0);

        pptxToPdfService.renderTextShape(builder, textShape);

        verify(builder).addFormattedText("Bold text", true, false, 14f);
        verify(builder).endParagraph();
    }

    @Test
    void italicRunRendersWithItalicStyle() throws IOException {
        XSLFTextShape textShape = mock(XSLFTextShape.class);
        XSLFTextParagraph para = mock(XSLFTextParagraph.class);
        XSLFTextRun run = mock(XSLFTextRun.class);

        when(textShape.getTextParagraphs()).thenReturn(List.of(para));
        when(para.getTextRuns()).thenReturn(List.of(run));
        when(run.getRawText()).thenReturn("Italic text");
        when(run.isBold()).thenReturn(false);
        when(run.isItalic()).thenReturn(true);
        when(run.getFontSize()).thenReturn(12.0);

        pptxToPdfService.renderTextShape(builder, textShape);

        verify(builder).addFormattedText("Italic text", false, true, 12f);
        verify(builder).endParagraph();
    }

    @Test
    void nullFontSizeDefaultsTo12() throws IOException {
        XSLFTextShape textShape = mock(XSLFTextShape.class);
        XSLFTextParagraph para = mock(XSLFTextParagraph.class);
        XSLFTextRun run = mock(XSLFTextRun.class);

        when(textShape.getTextParagraphs()).thenReturn(List.of(para));
        when(para.getTextRuns()).thenReturn(List.of(run));
        when(run.getRawText()).thenReturn("Default size");
        when(run.isBold()).thenReturn(false);
        when(run.isItalic()).thenReturn(false);
        when(run.getFontSize()).thenReturn(null);

        pptxToPdfService.renderTextShape(builder, textShape);

        verify(builder).addFormattedText("Default size", false, false, 12f);
        verify(builder).endParagraph();
    }

    @Test
    void mixedFormattingAcrossRuns() throws IOException {
        XSLFTextShape textShape = mock(XSLFTextShape.class);
        XSLFTextParagraph para = mock(XSLFTextParagraph.class);
        XSLFTextRun run1 = mock(XSLFTextRun.class);
        XSLFTextRun run2 = mock(XSLFTextRun.class);

        when(textShape.getTextParagraphs()).thenReturn(List.of(para));
        when(para.getTextRuns()).thenReturn(List.of(run1, run2));

        when(run1.getRawText()).thenReturn("Normal ");
        when(run1.isBold()).thenReturn(false);
        when(run1.isItalic()).thenReturn(false);
        when(run1.getFontSize()).thenReturn(12.0);

        when(run2.getRawText()).thenReturn("bold");
        when(run2.isBold()).thenReturn(true);
        when(run2.isItalic()).thenReturn(false);
        when(run2.getFontSize()).thenReturn(16.0);

        pptxToPdfService.renderTextShape(builder, textShape);

        var inOrder = inOrder(builder);
        inOrder.verify(builder).addFormattedText("Normal ", false, false, 12f);
        inOrder.verify(builder).addFormattedText("bold", true, false, 16f);
        inOrder.verify(builder).endParagraph();
    }

    // --- Requirement 2.5: Embedded images render at anchor position ---

    @Test
    void pictureShapeRendersImage() throws IOException {
        XSLFPictureShape picShape = mock(XSLFPictureShape.class);
        XSLFPictureData picData = mock(XSLFPictureData.class);
        byte[] imageBytes = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47};

        when(picShape.getPictureData()).thenReturn(picData);
        when(picData.getData()).thenReturn(imageBytes);

        pptxToPdfService.renderPictureShape(builder, picShape);

        verify(builder).addImage(imageBytes);
    }

    @Test
    void pictureShapeFailureIsHandledGracefully() throws IOException {
        XSLFPictureShape picShape = mock(XSLFPictureShape.class);

        when(picShape.getPictureData()).thenThrow(new RuntimeException("corrupted image"));

        pptxToPdfService.renderPictureShape(builder, picShape);

        verify(builder, never()).addImage(any(byte[].class));
    }

    // --- Requirement 2.6: Empty PPTX produces valid single-page PDF with message ---

    @Test
    void emptyPptxProducesSinglePagePdfWithMessage(@TempDir Path tempDir) throws Exception {
        var emptyPptxBytes = createEmptyPptxBytes();

        MockMultipartFile pptxFile = new MockMultipartFile(
                "file", "empty.pptx",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                emptyPptxBytes);

        File pdfFile = tempDir.resolve("output.pdf").toFile();

        pptxToPdfService.convertPptxToPdf(pptxFile, pdfFile);

        verify(builder).addParagraph("This presentation contains no slides.");
        verify(builder).save(pdfFile);
        verify(builder, never()).newPage(anyFloat(), anyFloat());
    }

    // --- Edge cases ---

    @Test
    void emptyRunTextIsSkipped() throws IOException {
        XSLFTextShape textShape = mock(XSLFTextShape.class);
        XSLFTextParagraph para = mock(XSLFTextParagraph.class);
        XSLFTextRun emptyRun = mock(XSLFTextRun.class);
        XSLFTextRun validRun = mock(XSLFTextRun.class);

        when(textShape.getTextParagraphs()).thenReturn(List.of(para));
        when(para.getTextRuns()).thenReturn(List.of(emptyRun, validRun));

        when(emptyRun.getRawText()).thenReturn("");
        when(validRun.getRawText()).thenReturn("Content");
        when(validRun.isBold()).thenReturn(false);
        when(validRun.isItalic()).thenReturn(false);
        when(validRun.getFontSize()).thenReturn(12.0);

        pptxToPdfService.renderTextShape(builder, textShape);

        verify(builder).addFormattedText("Content", false, false, 12f);
        verify(builder).endParagraph();
    }

    @Test
    void nullRunTextIsSkipped() throws IOException {
        XSLFTextShape textShape = mock(XSLFTextShape.class);
        XSLFTextParagraph para = mock(XSLFTextParagraph.class);
        XSLFTextRun run = mock(XSLFTextRun.class);

        when(textShape.getTextParagraphs()).thenReturn(List.of(para));
        when(para.getTextRuns()).thenReturn(List.of(run));
        when(run.getRawText()).thenReturn(null);

        pptxToPdfService.renderTextShape(builder, textShape);

        verify(builder, never()).addFormattedText(anyString(), anyBoolean(), anyBoolean(), anyFloat());
        verify(builder).endParagraph();
    }

    @Test
    void nullInputFileThrowsIOException(@TempDir Path tempDir) {
        File pdfFile = tempDir.resolve("output.pdf").toFile();
        assertThatThrownBy(() -> pptxToPdfService.convertPptxToPdf(null, pdfFile))
                .isInstanceOf(IOException.class);
    }

    @Test
    void nullOutputFileThrowsIOException() {
        MockMultipartFile pptxFile = new MockMultipartFile(
                "file", "test.pptx",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "test".getBytes());
        assertThatThrownBy(() -> pptxToPdfService.convertPptxToPdf(pptxFile, null))
                .isInstanceOf(IOException.class);
    }

    @Test
    void validPptxFileProducesOutput(@TempDir Path tempDir) throws Exception {
        var realBackend = new com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend();
        var realService = new PptxToPdfService(realBackend);

        ClassPathResource resource = new ClassPathResource("test-files/test.pptx");
        byte[] pptxData = Files.readAllBytes(resource.getFile().toPath());

        MockMultipartFile pptxFile = new MockMultipartFile(
                "file", "test.pptx",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                pptxData);

        File pdfFile = tempDir.resolve("output.pdf").toFile();
        realService.convertPptxToPdf(pptxFile, pdfFile);

        assertThat(pdfFile).exists();
        assertThat(pdfFile.length()).isGreaterThan(0);
    }

    // --- Helper ---

    private byte[] createEmptyPptxBytes() throws IOException {
        var baos = new java.io.ByteArrayOutputStream();
        try (var pptx = new XMLSlideShow()) {
            pptx.write(baos);
        }
        return baos.toByteArray();
    }
}
