package com.xtopdf.xtopdf.services.conversion.cad;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend;
import com.xtopdf.xtopdf.services.conversion.cad.PltToPdfService.HpglCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PltToPdfServiceTest {

    @Mock
    private PdfBackendProvider mockBackendProvider;

    @Mock
    private PdfDocumentBuilder mockBuilder;

    private PltToPdfService service;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        when(mockBackendProvider.createBuilder()).thenReturn(mockBuilder);
        service = new PltToPdfService(mockBackendProvider);
    }

    // --- Parsing Tests ---

    @Test
    void parseHpglCommands_simplePuPdPaSequence() {
        String content = "IN;SP1;PU100,200;PD300,400,500,600;PA700,800;";
        List<HpglCommand> commands = service.parseHpglCommands(content);

        assertThat(commands).hasSize(5);
        assertThat(commands.get(0).type()).isEqualTo("IN");
        assertThat(commands.get(1).type()).isEqualTo("SP");
        assertThat(commands.get(1).params()).containsExactly(1f);
        assertThat(commands.get(2).type()).isEqualTo("PU");
        assertThat(commands.get(2).params()).containsExactly(100f, 200f);
        assertThat(commands.get(3).type()).isEqualTo("PD");
        assertThat(commands.get(3).params()).containsExactly(300f, 400f, 500f, 600f);
        assertThat(commands.get(4).type()).isEqualTo("PA");
        assertThat(commands.get(4).params()).containsExactly(700f, 800f);
    }

    @Test
    void parseHpglCommands_labelWithEtxTermination() {
        String content = "LBHello World\003;";
        List<HpglCommand> commands = service.parseHpglCommands(content);

        assertThat(commands).hasSize(1);
        assertThat(commands.get(0).type()).isEqualTo("LB");
        assertThat(commands.get(0).labelText()).isEqualTo("Hello World");
    }

    @Test
    void parseHpglCommands_labelWithSemicolonTermination() {
        String content = "LBTest Label;PU0,0;";
        List<HpglCommand> commands = service.parseHpglCommands(content);

        assertThat(commands).hasSize(2);
        assertThat(commands.get(0).type()).isEqualTo("LB");
        assertThat(commands.get(0).labelText()).isEqualTo("Test Label");
        assertThat(commands.get(1).type()).isEqualTo("PU");
    }

    @Test
    void parseHpglCommands_relativeCoordinates() {
        String content = "PU100,100;PD;PR50,50,100,0;";
        List<HpglCommand> commands = service.parseHpglCommands(content);

        assertThat(commands).hasSize(3);
        assertThat(commands.get(2).type()).isEqualTo("PR");
        assertThat(commands.get(2).params()).containsExactly(50f, 50f, 100f, 0f);
    }

    @Test
    void parseHpglCommands_circleCommand() {
        String content = "PU500,500;CI200;";
        List<HpglCommand> commands = service.parseHpglCommands(content);

        assertThat(commands).hasSize(2);
        assertThat(commands.get(1).type()).isEqualTo("CI");
        assertThat(commands.get(1).params()).containsExactly(200f);
    }

    @Test
    void parseHpglCommands_arcAbsoluteAndRelative() {
        String content = "PU100,100;AA500,500,90;AR50,50,-45;";
        List<HpglCommand> commands = service.parseHpglCommands(content);

        assertThat(commands).hasSize(3);
        assertThat(commands.get(1).type()).isEqualTo("AA");
        assertThat(commands.get(1).params()).containsExactly(500f, 500f, 90f);
        assertThat(commands.get(2).type()).isEqualTo("AR");
        assertThat(commands.get(2).params()).containsExactly(50f, 50f, -45f);
    }

    @Test
    void parseHpglCommands_penWidth() {
        String content = "PW0.5;PD100,100;";
        List<HpglCommand> commands = service.parseHpglCommands(content);

        assertThat(commands).hasSize(2);
        assertThat(commands.get(0).type()).isEqualTo("PW");
        assertThat(commands.get(0).params()[0]).isCloseTo(0.5f, within(0.01f));
    }

    @Test
    void parseHpglCommands_commandsWithoutSemicolons() {
        String content = "IN SP2 PU100,200 PD300,400";
        List<HpglCommand> commands = service.parseHpglCommands(content);

        assertThat(commands).hasSize(4);
        assertThat(commands.get(0).type()).isEqualTo("IN");
        assertThat(commands.get(1).type()).isEqualTo("SP");
        assertThat(commands.get(2).type()).isEqualTo("PU");
        assertThat(commands.get(3).type()).isEqualTo("PD");
    }

    @Test
    void parseHpglCommands_emptyInput() {
        List<HpglCommand> commands = service.parseHpglCommands("");
        assertThat(commands).isEmpty();
    }

    @Test
    void parseHpglCommands_whitespaceOnly() {
        List<HpglCommand> commands = service.parseHpglCommands("   \n\t  ");
        assertThat(commands).isEmpty();
    }

    @Test
    void parseHpglCommands_negativeCoordinates() {
        String content = "PD-100,-200,300,-400;";
        List<HpglCommand> commands = service.parseHpglCommands(content);

        assertThat(commands).hasSize(1);
        assertThat(commands.get(0).params()).containsExactly(-100f, -200f, 300f, -400f);
    }

    // --- Bounding Box Tests ---

    @Test
    void computeBoundingBox_simplePdCommands() {
        List<HpglCommand> commands = List.of(
            new HpglCommand("PU", new float[]{100, 200}, null),
            new HpglCommand("PD", new float[]{300, 400, 500, 600}, null)
        );

        float[] bounds = service.computeBoundingBox(commands);

        assertThat(bounds[0]).as("minX").isEqualTo(100f);
        assertThat(bounds[1]).as("minY").isEqualTo(200f);
        assertThat(bounds[2]).as("maxX").isEqualTo(500f);
        assertThat(bounds[3]).as("maxY").isEqualTo(600f);
    }

    @Test
    void computeBoundingBox_relativeCoordinates() {
        List<HpglCommand> commands = List.of(
            new HpglCommand("PU", new float[]{100, 100}, null),
            new HpglCommand("PR", new float[]{50, 50, 100, 0}, null)
        );

        float[] bounds = service.computeBoundingBox(commands);

        assertThat(bounds[0]).as("minX").isEqualTo(100f);
        assertThat(bounds[1]).as("minY").isEqualTo(100f);
        assertThat(bounds[2]).as("maxX").isEqualTo(250f);
        assertThat(bounds[3]).as("maxY").isEqualTo(150f);
    }

    @Test
    void computeBoundingBox_circleExpandsBounds() {
        List<HpglCommand> commands = List.of(
            new HpglCommand("PU", new float[]{500, 500}, null),
            new HpglCommand("CI", new float[]{200}, null)
        );

        float[] bounds = service.computeBoundingBox(commands);

        assertThat(bounds[0]).as("minX").isEqualTo(300f);
        assertThat(bounds[1]).as("minY").isEqualTo(300f);
        assertThat(bounds[2]).as("maxX").isEqualTo(700f);
        assertThat(bounds[3]).as("maxY").isEqualTo(700f);
    }

    @Test
    void computeBoundingBox_noCoordinates() {
        List<HpglCommand> commands = List.of(
            new HpglCommand("IN", new float[0], null),
            new HpglCommand("SP", new float[]{2}, null)
        );

        float[] bounds = service.computeBoundingBox(commands);

        assertThat(bounds).containsExactly(0f, 0f, 0f, 0f);
    }

    // --- Pen State Tests ---

    @Test
    void renderCommands_puDoesNotDraw() throws IOException {
        List<HpglCommand> commands = List.of(
            new HpglCommand("PU", new float[]{100, 200}, null),
            new HpglCommand("PU", new float[]{300, 400}, null),
            new HpglCommand("PU", new float[]{500, 600}, null)
        );

        service.renderCommands(commands, mockBuilder, 1.0f, 0f, 0f, 0f, 0f);

        verify(mockBuilder, never()).drawLine(anyFloat(), anyFloat(), anyFloat(), anyFloat());
    }

    @Test
    void renderCommands_pdDrawsLines() throws IOException {
        List<HpglCommand> commands = List.of(
            new HpglCommand("PU", new float[]{0, 0}, null),
            new HpglCommand("PD", new float[]{100, 0, 100, 100}, null)
        );

        service.renderCommands(commands, mockBuilder, 1.0f, 0f, 0f, 0f, 0f);

        verify(mockBuilder, times(2)).drawLine(anyFloat(), anyFloat(), anyFloat(), anyFloat());
    }

    @Test
    void renderCommands_paDrawsWhenPenDown() throws IOException {
        List<HpglCommand> commands = List.of(
            new HpglCommand("PU", new float[]{0, 0}, null),
            new HpglCommand("PD", new float[]{}, null),  // pen down, no coordinates
            new HpglCommand("PA", new float[]{100, 100, 200, 200}, null)
        );

        service.renderCommands(commands, mockBuilder, 1.0f, 0f, 0f, 0f, 0f);

        verify(mockBuilder, times(2)).drawLine(anyFloat(), anyFloat(), anyFloat(), anyFloat());
    }

    @Test
    void renderCommands_paDoesNotDrawWhenPenUp() throws IOException {
        List<HpglCommand> commands = List.of(
            new HpglCommand("PU", new float[]{0, 0}, null),
            new HpglCommand("PA", new float[]{100, 100, 200, 200}, null)
        );

        service.renderCommands(commands, mockBuilder, 1.0f, 0f, 0f, 0f, 0f);

        verify(mockBuilder, never()).drawLine(anyFloat(), anyFloat(), anyFloat(), anyFloat());
    }

    // --- Scale Factor Tests ---

    @Test
    void scaleFactor_wideDrawing() {
        // Drawing is 1000 wide, 100 tall → scale limited by width
        List<HpglCommand> commands = List.of(
            new HpglCommand("PD", new float[]{0, 0, 1000, 100}, null)
        );

        float[] bounds = service.computeBoundingBox(commands);
        float boundsWidth = bounds[2] - bounds[0];
        float boundsHeight = bounds[3] - bounds[1];

        float scaleX = 495f / boundsWidth;
        float scaleY = 742f / boundsHeight;
        float scale = Math.min(scaleX, scaleY);

        assertThat(scale).as("Wide drawing limited by width")
            .isCloseTo(scaleX, within(0.001f));
    }

    @Test
    void scaleFactor_tallDrawing() {
        // Drawing is 100 wide, 1000 tall → scale limited by height
        List<HpglCommand> commands = List.of(
            new HpglCommand("PD", new float[]{0, 0, 100, 1000}, null)
        );

        float[] bounds = service.computeBoundingBox(commands);
        float boundsWidth = bounds[2] - bounds[0];
        float boundsHeight = bounds[3] - bounds[1];

        float scaleX = 495f / boundsWidth;
        float scaleY = 742f / boundsHeight;
        float scale = Math.min(scaleX, scaleY);

        assertThat(scale).as("Tall drawing limited by height")
            .isCloseTo(scaleY, within(0.001f));
    }

    @Test
    void scaleFactor_squareDrawing() {
        // Drawing is 500x500 — should use width-based scale since USABLE_WIDTH < USABLE_HEIGHT
        List<HpglCommand> commands = List.of(
            new HpglCommand("PD", new float[]{0, 0, 500, 500}, null)
        );

        float[] bounds = service.computeBoundingBox(commands);
        float boundsWidth = bounds[2] - bounds[0];
        float boundsHeight = bounds[3] - bounds[1];

        float scaleX = 495f / boundsWidth;
        float scaleY = 742f / boundsHeight;
        float scale = Math.min(scaleX, scaleY);

        // Square: scaleX = 495/500 = 0.99, scaleY = 742/500 = 1.484
        // min is scaleX
        assertThat(scale).as("Square drawing limited by narrower dimension")
            .isCloseTo(scaleX, within(0.001f));
    }

    // --- Circle and Arc Rendering ---

    @Test
    void renderCommands_circleRendersAtCurrentPosition() throws IOException {
        List<HpglCommand> commands = List.of(
            new HpglCommand("PU", new float[]{200, 200}, null),
            new HpglCommand("CI", new float[]{50}, null)
        );

        service.renderCommands(commands, mockBuilder, 1.0f, 0f, 0f, 0f, 0f);

        ArgumentCaptor<Float> cxCaptor = ArgumentCaptor.forClass(Float.class);
        ArgumentCaptor<Float> cyCaptor = ArgumentCaptor.forClass(Float.class);
        ArgumentCaptor<Float> rCaptor = ArgumentCaptor.forClass(Float.class);
        verify(mockBuilder).drawCircle(cxCaptor.capture(), cyCaptor.capture(), rCaptor.capture());

        assertThat(rCaptor.getValue()).as("circle radius").isCloseTo(50f, within(0.01f));
    }

    @Test
    void renderCommands_arcAbsolute() throws IOException {
        List<HpglCommand> commands = List.of(
            new HpglCommand("PU", new float[]{200, 100}, null),
            new HpglCommand("AA", new float[]{200, 200, 90}, null)
        );

        service.renderCommands(commands, mockBuilder, 1.0f, 0f, 0f, 0f, 0f);

        verify(mockBuilder).drawArc(anyFloat(), anyFloat(), anyFloat(), anyFloat(), eq(90f));
    }

    @Test
    void renderCommands_arcRelative() throws IOException {
        List<HpglCommand> commands = List.of(
            new HpglCommand("PU", new float[]{100, 100}, null),
            new HpglCommand("AR", new float[]{50, 0, -90}, null)
        );

        service.renderCommands(commands, mockBuilder, 1.0f, 0f, 0f, 0f, 0f);

        verify(mockBuilder).drawArc(anyFloat(), anyFloat(), anyFloat(), anyFloat(), eq(-90f));
    }

    // --- SP (Select Pen) Tests ---

    @Test
    void renderCommands_spChangesColor() throws IOException {
        List<HpglCommand> commands = List.of(
            new HpglCommand("SP", new float[]{3}, null),
            new HpglCommand("PD", new float[]{100, 100}, null)
        );

        service.renderCommands(commands, mockBuilder, 1.0f, 0f, 0f, 0f, 0f);

        // Pen 3 is green {0, 128, 0}
        verify(mockBuilder).setStrokeColor(0, 128, 0);
    }

    @Test
    void renderCommands_spClampsToValidRange() throws IOException {
        List<HpglCommand> commands = List.of(
            new HpglCommand("SP", new float[]{99}, null)
        );

        service.renderCommands(commands, mockBuilder, 1.0f, 0f, 0f, 0f, 0f);

        // Should clamp to pen 8: {255, 128, 0}
        verify(mockBuilder).setStrokeColor(255, 128, 0);
    }

    // --- Empty Input Tests ---

    @Test
    void convertPltToPdf_emptyInputProducesMessage() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
            "file", "empty.plt", MediaType.APPLICATION_OCTET_STREAM_VALUE, "".getBytes());
        File pdfFile = tempDir.resolve("output.pdf").toFile();

        service.convertPltToPdf(file, pdfFile);

        verify(mockBuilder).addText(eq("HPGL/PLT file contains no drawing content."), anyFloat(), anyFloat());
        verify(mockBuilder).save(pdfFile);
    }

    @Test
    void convertPltToPdf_onlyNonRenderableCommandsProducesMessage() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
            "file", "init-only.plt", MediaType.APPLICATION_OCTET_STREAM_VALUE,
            "IN;SP2;PU100,200;".getBytes());
        File pdfFile = tempDir.resolve("output.pdf").toFile();

        service.convertPltToPdf(file, pdfFile);

        verify(mockBuilder).addText(eq("HPGL/PLT file contains no drawing content."), anyFloat(), anyFloat());
    }

    // --- PR (Relative Coordinate) Accumulation ---

    @Test
    void computeBoundingBox_prAccumulatesCorrectly() {
        List<HpglCommand> commands = List.of(
            new HpglCommand("PU", new float[]{100, 100}, null),
            new HpglCommand("PR", new float[]{50, 0}, null),
            new HpglCommand("PR", new float[]{0, 50}, null),
            new HpglCommand("PR", new float[]{-25, -25}, null)
        );

        float[] bounds = service.computeBoundingBox(commands);

        // Start at (100,100), then (150,100), then (150,150), then (125,125)
        assertThat(bounds[0]).as("minX").isEqualTo(100f);
        assertThat(bounds[1]).as("minY").isEqualTo(100f);
        assertThat(bounds[2]).as("maxX").isEqualTo(150f);
        assertThat(bounds[3]).as("maxY").isEqualTo(150f);
    }

    // --- Integration Test with real PdfBoxBackend ---

    @Test
    void convertPltToPdf_integrationWithPdfBoxBackend() throws Exception {
        PltToPdfService realService = new PltToPdfService(new PdfBoxBackend());
        String content = "IN;SP1;PU0,0;PD100,0,100,100,0,100,0,0;PU;";

        MockMultipartFile pltFile = new MockMultipartFile(
            "file", "test.plt", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        File pdfFile = tempDir.resolve("integration.pdf").toFile();

        realService.convertPltToPdf(pltFile, pdfFile);

        assertThat(pdfFile).exists();
        assertThat(pdfFile.length()).as("PDF should have content").isGreaterThan(0);
    }

    @Test
    void convertPltToPdf_integrationWithCirclesAndArcs() throws Exception {
        PltToPdfService realService = new PltToPdfService(new PdfBoxBackend());
        String content = "IN;SP2;PU500,500;CI200;AA500,600,45;AR50,50,-30;";

        MockMultipartFile pltFile = new MockMultipartFile(
            "file", "arcs.plt", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        File pdfFile = tempDir.resolve("arcs.pdf").toFile();

        realService.convertPltToPdf(pltFile, pdfFile);

        assertThat(pdfFile).exists();
        assertThat(pdfFile.length()).isGreaterThan(0);
    }

    @Test
    void convertPltToPdf_integrationWithLabels() throws Exception {
        PltToPdfService realService = new PltToPdfService(new PdfBoxBackend());
        String content = "IN;SP1;PA1000,1000;LBHello World\003;PU;";

        MockMultipartFile pltFile = new MockMultipartFile(
            "file", "labels.plt", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        File pdfFile = tempDir.resolve("labels.pdf").toFile();

        realService.convertPltToPdf(pltFile, pdfFile);

        assertThat(pdfFile).exists();
        assertThat(pdfFile.length()).isGreaterThan(0);
    }

    @Test
    void convertPltToPdf_integrationEmptyFile() throws Exception {
        PltToPdfService realService = new PltToPdfService(new PdfBoxBackend());

        MockMultipartFile pltFile = new MockMultipartFile(
            "file", "empty.plt", MediaType.APPLICATION_OCTET_STREAM_VALUE, "".getBytes());
        File pdfFile = tempDir.resolve("empty.pdf").toFile();

        realService.convertPltToPdf(pltFile, pdfFile);

        assertThat(pdfFile).exists();
        assertThat(pdfFile.length()).isGreaterThan(0);
    }
}
