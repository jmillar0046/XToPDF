package com.xtopdf.xtopdf.services.conversion.document;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class DocToPdfServiceTest {

    @Mock
    private PdfBackendProvider pdfBackend;

    @Mock
    private PdfDocumentBuilder builder;

    private DocToPdfService docToPdfService;

    @BeforeEach
    void setUp() throws IOException {
        lenient().when(pdfBackend.createBuilder()).thenReturn(builder);
        docToPdfService = new DocToPdfService(pdfBackend);
    }

    // --- Tests for character run formatting ---

    @Test
    void boldCharacterRunRendersWithBoldStyle() throws IOException {
        Paragraph para = mock(Paragraph.class);
        CharacterRun run = mock(CharacterRun.class);

        when(para.numCharacterRuns()).thenReturn(1);
        when(para.getCharacterRun(0)).thenReturn(run);
        when(run.text()).thenReturn("Bold text");
        when(run.isBold()).thenReturn(true);
        when(run.isItalic()).thenReturn(false);

        docToPdfService.renderParagraphRuns(builder, para, 12f);

        verify(builder).addFormattedText("Bold text", true, false, 12f);
        verify(builder).endParagraph();
    }

    @Test
    void italicCharacterRunRendersWithItalicStyle() throws IOException {
        Paragraph para = mock(Paragraph.class);
        CharacterRun run = mock(CharacterRun.class);

        when(para.numCharacterRuns()).thenReturn(1);
        when(para.getCharacterRun(0)).thenReturn(run);
        when(run.text()).thenReturn("Italic text");
        when(run.isBold()).thenReturn(false);
        when(run.isItalic()).thenReturn(true);

        docToPdfService.renderParagraphRuns(builder, para, 12f);

        verify(builder).addFormattedText("Italic text", false, true, 12f);
        verify(builder).endParagraph();
    }

    @Test
    void underlineCharacterRunRendersCorrectly() throws IOException {
        Paragraph para = mock(Paragraph.class);
        CharacterRun run = mock(CharacterRun.class);

        when(para.numCharacterRuns()).thenReturn(1);
        when(para.getCharacterRun(0)).thenReturn(run);
        when(run.text()).thenReturn("Underlined text");
        when(run.isBold()).thenReturn(false);
        when(run.isItalic()).thenReturn(false);

        docToPdfService.renderParagraphRuns(builder, para, 12f);

        verify(builder).addFormattedText("Underlined text", false, false, 12f);
        verify(builder).endParagraph();
    }

    // --- Tests for heading style detection ---

    @Test
    void headingStyle1MapsToFontSize24() {
        Paragraph para = mock(Paragraph.class);
        org.apache.poi.hwpf.model.StyleSheet styles = mock(org.apache.poi.hwpf.model.StyleSheet.class);
        org.apache.poi.hwpf.model.StyleDescription styleDesc = mock(org.apache.poi.hwpf.model.StyleDescription.class);

        when(para.getStyleIndex()).thenReturn((short)1);
        when(styles.getStyleDescription(1)).thenReturn(styleDesc);
        when(styleDesc.getName()).thenReturn("Heading 1");

        float fontSize = docToPdfService.detectHeadingSize(para, styles);

        assertThat(fontSize).isEqualTo(24f);
    }

    @Test
    void headingStyle2MapsToFontSize20() {
        Paragraph para = mock(Paragraph.class);
        org.apache.poi.hwpf.model.StyleSheet styles = mock(org.apache.poi.hwpf.model.StyleSheet.class);
        org.apache.poi.hwpf.model.StyleDescription styleDesc = mock(org.apache.poi.hwpf.model.StyleDescription.class);

        when(para.getStyleIndex()).thenReturn((short)2);
        when(styles.getStyleDescription(2)).thenReturn(styleDesc);
        when(styleDesc.getName()).thenReturn("Heading 2");

        float fontSize = docToPdfService.detectHeadingSize(para, styles);

        assertThat(fontSize).isEqualTo(20f);
    }

    @Test
    void headingStyle3MapsToFontSize16() {
        Paragraph para = mock(Paragraph.class);
        org.apache.poi.hwpf.model.StyleSheet styles = mock(org.apache.poi.hwpf.model.StyleSheet.class);
        org.apache.poi.hwpf.model.StyleDescription styleDesc = mock(org.apache.poi.hwpf.model.StyleDescription.class);

        when(para.getStyleIndex()).thenReturn((short)3);
        when(styles.getStyleDescription(3)).thenReturn(styleDesc);
        when(styleDesc.getName()).thenReturn("heading 3");

        float fontSize = docToPdfService.detectHeadingSize(para, styles);

        assertThat(fontSize).isEqualTo(16f);
    }

    @Test
    void headingStyle4MapsToFontSize14() {
        Paragraph para = mock(Paragraph.class);
        org.apache.poi.hwpf.model.StyleSheet styles = mock(org.apache.poi.hwpf.model.StyleSheet.class);
        org.apache.poi.hwpf.model.StyleDescription styleDesc = mock(org.apache.poi.hwpf.model.StyleDescription.class);

        when(para.getStyleIndex()).thenReturn((short)4);
        when(styles.getStyleDescription(4)).thenReturn(styleDesc);
        when(styleDesc.getName()).thenReturn("Heading 4");

        float fontSize = docToPdfService.detectHeadingSize(para, styles);

        assertThat(fontSize).isEqualTo(14f);
    }

    @Test
    void headingStyle5MapsToFontSize13() {
        Paragraph para = mock(Paragraph.class);
        org.apache.poi.hwpf.model.StyleSheet styles = mock(org.apache.poi.hwpf.model.StyleSheet.class);
        org.apache.poi.hwpf.model.StyleDescription styleDesc = mock(org.apache.poi.hwpf.model.StyleDescription.class);

        when(para.getStyleIndex()).thenReturn((short)5);
        when(styles.getStyleDescription(5)).thenReturn(styleDesc);
        when(styleDesc.getName()).thenReturn("Heading 5");

        float fontSize = docToPdfService.detectHeadingSize(para, styles);

        assertThat(fontSize).isEqualTo(13f);
    }

    @Test
    void headingStyle6MapsToFontSize12() {
        Paragraph para = mock(Paragraph.class);
        org.apache.poi.hwpf.model.StyleSheet styles = mock(org.apache.poi.hwpf.model.StyleSheet.class);
        org.apache.poi.hwpf.model.StyleDescription styleDesc = mock(org.apache.poi.hwpf.model.StyleDescription.class);

        when(para.getStyleIndex()).thenReturn((short)6);
        when(styles.getStyleDescription(6)).thenReturn(styleDesc);
        when(styleDesc.getName()).thenReturn("Heading 6");

        float fontSize = docToPdfService.detectHeadingSize(para, styles);

        assertThat(fontSize).isEqualTo(12f);
    }

    @Test
    void normalStyleDefaultsToFontSize12() {
        Paragraph para = mock(Paragraph.class);
        org.apache.poi.hwpf.model.StyleSheet styles = mock(org.apache.poi.hwpf.model.StyleSheet.class);
        org.apache.poi.hwpf.model.StyleDescription styleDesc = mock(org.apache.poi.hwpf.model.StyleDescription.class);

        when(para.getStyleIndex()).thenReturn((short)0);
        when(styles.getStyleDescription(0)).thenReturn(styleDesc);
        when(styleDesc.getName()).thenReturn("Normal");

        float fontSize = docToPdfService.detectHeadingSize(para, styles);

        assertThat(fontSize).isEqualTo(12f);
    }

    @Test
    void nullStyleDescriptionDefaultsToFontSize12() {
        Paragraph para = mock(Paragraph.class);
        org.apache.poi.hwpf.model.StyleSheet styles = mock(org.apache.poi.hwpf.model.StyleSheet.class);

        when(para.getStyleIndex()).thenReturn((short)99);
        when(styles.getStyleDescription(99)).thenReturn(null);

        float fontSize = docToPdfService.detectHeadingSize(para, styles);

        assertThat(fontSize).isEqualTo(12f);
    }

    @Test
    void nullStyleNameDefaultsToFontSize12() {
        Paragraph para = mock(Paragraph.class);
        org.apache.poi.hwpf.model.StyleSheet styles = mock(org.apache.poi.hwpf.model.StyleSheet.class);
        org.apache.poi.hwpf.model.StyleDescription styleDesc = mock(org.apache.poi.hwpf.model.StyleDescription.class);

        when(para.getStyleIndex()).thenReturn((short)10);
        when(styles.getStyleDescription(10)).thenReturn(styleDesc);
        when(styleDesc.getName()).thenReturn(null);

        float fontSize = docToPdfService.detectHeadingSize(para, styles);

        assertThat(fontSize).isEqualTo(12f);
    }

    // --- Tests for mixed formatting across consecutive paragraphs ---

    @Test
    void mixedFormattingAcrossConsecutiveRuns() throws IOException {
        Paragraph para = mock(Paragraph.class);
        CharacterRun run1 = mock(CharacterRun.class);
        CharacterRun run2 = mock(CharacterRun.class);
        CharacterRun run3 = mock(CharacterRun.class);

        when(para.numCharacterRuns()).thenReturn(3);
        when(para.getCharacterRun(0)).thenReturn(run1);
        when(para.getCharacterRun(1)).thenReturn(run2);
        when(para.getCharacterRun(2)).thenReturn(run3);

        when(run1.text()).thenReturn("Normal ");
        when(run1.isBold()).thenReturn(false);
        when(run1.isItalic()).thenReturn(false);

        when(run2.text()).thenReturn("bold ");
        when(run2.isBold()).thenReturn(true);
        when(run2.isItalic()).thenReturn(false);

        when(run3.text()).thenReturn("italic");
        when(run3.isBold()).thenReturn(false);
        when(run3.isItalic()).thenReturn(true);

        docToPdfService.renderParagraphRuns(builder, para, 14f);

        var inOrder = inOrder(builder);
        inOrder.verify(builder).addFormattedText("Normal", false, false, 14f);
        inOrder.verify(builder).addFormattedText("bold", true, false, 14f);
        inOrder.verify(builder).addFormattedText("italic", false, true, 14f);
        inOrder.verify(builder).endParagraph();
    }

    @Test
    void emptyRunsAreSkipped() throws IOException {
        Paragraph para = mock(Paragraph.class);
        CharacterRun emptyRun = mock(CharacterRun.class);
        CharacterRun validRun = mock(CharacterRun.class);

        when(para.numCharacterRuns()).thenReturn(2);
        when(para.getCharacterRun(0)).thenReturn(emptyRun);
        when(para.getCharacterRun(1)).thenReturn(validRun);

        when(emptyRun.text()).thenReturn("   ");
        when(validRun.text()).thenReturn("Content");
        when(validRun.isBold()).thenReturn(false);
        when(validRun.isItalic()).thenReturn(false);

        docToPdfService.renderParagraphRuns(builder, para, 12f);

        verify(builder, never()).addFormattedText(eq(""), anyBoolean(), anyBoolean(), anyFloat());
        verify(builder).addFormattedText("Content", false, false, 12f);
        verify(builder).endParagraph();
    }

    @Test
    void nullRunTextIsHandledGracefully() throws IOException {
        Paragraph para = mock(Paragraph.class);
        CharacterRun run = mock(CharacterRun.class);

        when(para.numCharacterRuns()).thenReturn(1);
        when(para.getCharacterRun(0)).thenReturn(run);
        when(run.text()).thenReturn(null);

        docToPdfService.renderParagraphRuns(builder, para, 12f);

        verify(builder, never()).addFormattedText(anyString(), anyBoolean(), anyBoolean(), anyFloat());
        verify(builder).endParagraph();
    }

    @Test
    void controlCharactersAreStrippedFromRunText() throws IOException {
        Paragraph para = mock(Paragraph.class);
        CharacterRun run = mock(CharacterRun.class);

        when(para.numCharacterRuns()).thenReturn(1);
        when(para.getCharacterRun(0)).thenReturn(run);
        when(run.text()).thenReturn("Hello\u0007World");
        when(run.isBold()).thenReturn(false);
        when(run.isItalic()).thenReturn(false);

        docToPdfService.renderParagraphRuns(builder, para, 12f);

        verify(builder).addFormattedText("HelloWorld", false, false, 12f);
        verify(builder).endParagraph();
    }

    // --- Tests for static heading size helper ---

    @Test
    void staticHeadingLevelMappingIsCorrect() {
        assertThat(DocToPdfService.detectHeadingSize(1)).isEqualTo(24f);
        assertThat(DocToPdfService.detectHeadingSize(2)).isEqualTo(20f);
        assertThat(DocToPdfService.detectHeadingSize(3)).isEqualTo(16f);
        assertThat(DocToPdfService.detectHeadingSize(4)).isEqualTo(14f);
        assertThat(DocToPdfService.detectHeadingSize(5)).isEqualTo(13f);
        assertThat(DocToPdfService.detectHeadingSize(6)).isEqualTo(12f);
        assertThat(DocToPdfService.detectHeadingSize(0)).isEqualTo(12f);
        assertThat(DocToPdfService.detectHeadingSize(7)).isEqualTo(12f);
    }

    // --- Error handling tests ---

    @Test
    void nullMultipartFileThrowsException() {
        File pdfFile = new File(System.getProperty("java.io.tmpdir") + "/nullInput.pdf");
        assertThatThrownBy(() -> docToPdfService.convertDocToPdf(null, pdfFile))
                .isInstanceOf(Exception.class);
    }

    @Test
    void invalidDocFileThrowsException() {
        var docFile = new MockMultipartFile("file", "test.doc", "application/msword",
                "Not a valid DOC file".getBytes());
        File pdfFile = new File(System.getProperty("java.io.tmpdir") + "/invalid.pdf");

        assertThatThrownBy(() -> docToPdfService.convertDocToPdf(docFile, pdfFile))
                .isInstanceOf(Exception.class);
    }

    @Test
    void emptyDocFileThrowsException() {
        var docFile = new MockMultipartFile("file", "test.doc", "application/msword", new byte[0]);
        File pdfFile = new File(System.getProperty("java.io.tmpdir") + "/empty.pdf");

        assertThatThrownBy(() -> docToPdfService.convertDocToPdf(docFile, pdfFile))
                .isInstanceOf(Exception.class);
    }
}
