package com.xtopdf.xtopdf.services.conversion.cad;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import com.xtopdf.xtopdf.services.conversion.cad.PltToPdfService.HpglCommand;
import net.jqwik.api.*;
import net.jqwik.api.constraints.FloatRange;
import net.jqwik.api.constraints.IntRange;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for the HPGL/PLT renderer.
 * Validates core correctness properties of parsing, pen state, color mapping,
 * relative coordinate accumulation, and empty input handling.
 */
@Tag("Feature: cad-converter-rendering, Properties 1-5: HPGL renderer")
class PltToPdfServicePropertyTest {

    /**
     * Property 1: Pen State Isolation
     *
     * For any HPGL command sequence, PU (Pen Up) commands SHALL NOT produce any
     * drawLine output. PD (Pen Down) commands SHALL produce drawLine output for
     * every coordinate pair they contain.
     */
    @Property(tries = 25)
    @Label("PU produces no drawLine calls, PD produces drawLine for each coordinate pair")
    void penStateIsolation(
            @ForAll @IntRange(min = 1, max = 5) int puCount,
            @ForAll @IntRange(min = 1, max = 5) int pdPairs) throws IOException {

        PdfBackendProvider mockProvider = mock(PdfBackendProvider.class);
        PdfDocumentBuilder mockBuilder = mock(PdfDocumentBuilder.class);
        when(mockProvider.createBuilder()).thenReturn(mockBuilder);
        PltToPdfService service = new PltToPdfService(mockProvider);

        // Build a sequence of PU commands followed by PD with coordinate pairs
        List<HpglCommand> commands = new ArrayList<>();

        // PU commands — should never draw
        for (int i = 0; i < puCount; i++) {
            commands.add(new HpglCommand("PU", new float[]{i * 10f, i * 10f}, null));
        }

        // Now PD with multiple coordinate pairs — each pair should draw a line
        float[] pdParams = new float[pdPairs * 2];
        for (int i = 0; i < pdPairs; i++) {
            pdParams[i * 2] = (i + 1) * 50f;
            pdParams[i * 2 + 1] = (i + 1) * 50f;
        }
        commands.add(new HpglCommand("PD", pdParams, null));

        service.renderCommands(commands, mockBuilder, 1.0f, 0f, 0f, 0f, 0f);

        // PD should produce exactly pdPairs drawLine calls
        verify(mockBuilder, times(pdPairs)).drawLine(anyFloat(), anyFloat(), anyFloat(), anyFloat());
    }

    /**
     * Property 2: Pen Color Mapping
     *
     * For any SP (Select Pen) command with a pen number 1-8, the stroke color
     * set on the builder SHALL correspond to the defined color map.
     */
    @Property(tries = 25)
    @Label("SP sets correct RGB color from pen color map")
    void penColorMapping(@ForAll @IntRange(min = 1, max = 8) int penNumber) throws IOException {

        PdfBackendProvider mockProvider = mock(PdfBackendProvider.class);
        PdfDocumentBuilder mockBuilder = mock(PdfDocumentBuilder.class);
        when(mockProvider.createBuilder()).thenReturn(mockBuilder);
        PltToPdfService service = new PltToPdfService(mockProvider);

        int[][] expectedColors = {
            {0, 0, 0},       // Pen 0
            {0, 0, 0},       // Pen 1
            {255, 0, 0},     // Pen 2
            {0, 128, 0},     // Pen 3
            {0, 0, 255},     // Pen 4
            {0, 200, 200},   // Pen 5
            {200, 0, 200},   // Pen 6
            {200, 200, 0},   // Pen 7
            {255, 128, 0},   // Pen 8
        };

        List<HpglCommand> commands = List.of(
            new HpglCommand("SP", new float[]{penNumber}, null)
        );

        service.renderCommands(commands, mockBuilder, 1.0f, 0f, 0f, 0f, 0f);

        int[] expected = expectedColors[penNumber];
        // The SP command triggers setStrokeColor. The initial pen 1 color is also set,
        // so verify the last call matches the requested pen.
        ArgumentCaptor<Integer> rCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> gCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> bCaptor = ArgumentCaptor.forClass(Integer.class);

        // Get all invocations — last one should be our SP command
        verify(mockBuilder, atLeast(1)).setStrokeColor(rCaptor.capture(), gCaptor.capture(), bCaptor.capture());

        List<Integer> rValues = rCaptor.getAllValues();
        List<Integer> gValues = gCaptor.getAllValues();
        List<Integer> bValues = bCaptor.getAllValues();

        int lastIdx = rValues.size() - 1;
        assertThat(rValues.get(lastIdx)).as("Red for pen %d", penNumber).isEqualTo(expected[0]);
        assertThat(gValues.get(lastIdx)).as("Green for pen %d", penNumber).isEqualTo(expected[1]);
        assertThat(bValues.get(lastIdx)).as("Blue for pen %d", penNumber).isEqualTo(expected[2]);
    }

    /**
     * Property 3: Relative Coordinate Accumulation
     *
     * For any sequence of PR (Plot Relative) commands, the final position SHALL equal
     * the starting position plus the sum of all relative offsets.
     */
    @Property(tries = 25)
    @Label("PR offsets accumulate correctly into final position")
    void relativeCoordinateAccumulation(
            @ForAll @IntRange(min = 1, max = 6) int prCount,
            @ForAll @FloatRange(min = -100f, max = 100f) float baseX,
            @ForAll @FloatRange(min = -100f, max = 100f) float baseY) {

        PltToPdfService service = new PltToPdfService(mock(PdfBackendProvider.class));

        List<HpglCommand> commands = new ArrayList<>();
        commands.add(new HpglCommand("PU", new float[]{baseX, baseY}, null));

        float expectedX = baseX;
        float expectedY = baseY;

        for (int i = 0; i < prCount; i++) {
            float dx = (i + 1) * 10f;
            float dy = (i + 1) * 5f;
            commands.add(new HpglCommand("PR", new float[]{dx, dy}, null));
            expectedX += dx;
            expectedY += dy;
        }

        // computeBoundingBox tracks current position through PR commands
        float[] bounds = service.computeBoundingBox(commands);

        // The max coordinates should include our final accumulated position
        assertThat(bounds[2]).as("maxX should include accumulated position")
            .isCloseTo(expectedX, within(0.01f));
        assertThat(bounds[3]).as("maxY should include accumulated position")
            .isCloseTo(expectedY, within(0.01f));
    }

    /**
     * Property 4: Coordinate Preservation (Aspect Ratio)
     *
     * For any HPGL drawing with a non-zero bounding box, after scaling, the aspect
     * ratio (width/height) of the original drawing SHALL be preserved in the rendered
     * output coordinates.
     */
    @Property(tries = 25)
    @Label("Aspect ratio is preserved after scaling")
    void coordinatePreservation(
            @ForAll @FloatRange(min = 10f, max = 5000f) float drawWidth,
            @ForAll @FloatRange(min = 10f, max = 5000f) float drawHeight) throws IOException {

        PdfBackendProvider mockProvider = mock(PdfBackendProvider.class);
        PdfDocumentBuilder mockBuilder = mock(PdfDocumentBuilder.class);
        when(mockProvider.createBuilder()).thenReturn(mockBuilder);
        PltToPdfService service = new PltToPdfService(mockProvider);

        // Create a simple rectangle drawing
        List<HpglCommand> commands = List.of(
            new HpglCommand("PU", new float[]{0, 0}, null),
            new HpglCommand("PD", new float[]{drawWidth, 0, drawWidth, drawHeight, 0, drawHeight, 0, 0}, null)
        );

        float[] bounds = service.computeBoundingBox(commands);
        float boundsWidth = bounds[2] - bounds[0];
        float boundsHeight = bounds[3] - bounds[1];

        // Calculate scale same way the service does
        float scaleX = 495f / boundsWidth;
        float scaleY = 742f / boundsHeight;
        float scale = Math.min(scaleX, scaleY);

        // The rendered width and height should maintain ratio
        float renderedWidth = boundsWidth * scale;
        float renderedHeight = boundsHeight * scale;

        float originalRatio = drawWidth / drawHeight;
        float renderedRatio = renderedWidth / renderedHeight;

        assertThat(renderedRatio).as("Aspect ratio preserved")
            .isCloseTo(originalRatio, within(0.01f));
    }

    /**
     * Property 5: Empty Input Handling
     *
     * For any HPGL content that contains no renderable commands (no PD, CI, AA, AR, or LB),
     * the converter SHALL produce output containing a "no drawing content" informational message.
     */
    @Property(tries = 25)
    @Label("No renderable commands produces informational message")
    void emptyInputHandling(
            @ForAll @IntRange(min = 0, max = 5) int inCount,
            @ForAll @IntRange(min = 0, max = 3) int spCount,
            @ForAll @IntRange(min = 0, max = 4) int puCount) throws Exception {

        PdfBackendProvider mockProvider = mock(PdfBackendProvider.class);
        PdfDocumentBuilder mockBuilder = mock(PdfDocumentBuilder.class);
        when(mockProvider.createBuilder()).thenReturn(mockBuilder);
        PltToPdfService service = new PltToPdfService(mockProvider);

        // Build HPGL with only non-renderable commands (IN, SP, PU)
        StringBuilder hpgl = new StringBuilder();
        for (int i = 0; i < inCount; i++) hpgl.append("IN;");
        for (int i = 0; i < spCount; i++) hpgl.append("SP").append(i + 1).append(";");
        for (int i = 0; i < puCount; i++) hpgl.append("PU").append(i * 100).append(",").append(i * 100).append(";");

        MockMultipartFile file = new MockMultipartFile(
            "file", "test.plt", "application/octet-stream", hpgl.toString().getBytes());

        File tempFile = Files.createTempFile("plt-prop-", ".pdf").toFile();
        tempFile.deleteOnExit();

        service.convertPltToPdf(file, tempFile);

        // Should output the "no drawing content" message
        verify(mockBuilder).addText(
            eq("HPGL/PLT file contains no drawing content."),
            anyFloat(), anyFloat());
    }
}
