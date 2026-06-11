package com.xtopdf.xtopdf.services.conversion.cad;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

/**
 * Service to convert HPGL Plotter files (PLT) to PDF.
 * Parses HPGL pen-plotter commands and renders vector graphics
 * (lines, circles, arcs, labels) onto a PDF page with proper
 * coordinate scaling and pen color mapping.
 */
@Slf4j
@Service
public class PltToPdfService {

    private static final float PAGE_WIDTH = 595f;
    private static final float PAGE_HEIGHT = 842f;
    private static final float MARGIN = 50f;
    private static final float USABLE_WIDTH = PAGE_WIDTH - 2 * MARGIN;
    private static final float USABLE_HEIGHT = PAGE_HEIGHT - 2 * MARGIN;

    private static final int[][] PEN_COLORS = {
        {0, 0, 0},       // Pen 0: black (default)
        {0, 0, 0},       // Pen 1: black
        {255, 0, 0},     // Pen 2: red
        {0, 128, 0},     // Pen 3: green
        {0, 0, 255},     // Pen 4: blue
        {0, 200, 200},   // Pen 5: cyan
        {200, 0, 200},   // Pen 6: magenta
        {200, 200, 0},   // Pen 7: yellow
        {255, 128, 0},   // Pen 8: orange
    };

    record HpglCommand(String type, float[] params, String labelText) {}

    static class HpglState {
        float currentX = 0;
        float currentY = 0;
        boolean penDown = false;
        int currentPen = 1;
        float penWidth = 0.35f;
    }

    private final PdfBackendProvider pdfBackend;

    @Autowired
    public PltToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }

    public void convertPltToPdf(MultipartFile inputFile, File pdfFile) throws IOException {
        if (inputFile == null) {
            throw new IOException("Input file must not be null");
        }
        if (pdfFile == null) {
            throw new IOException("Output file must not be null");
        }

        String content = new String(inputFile.getBytes(), java.nio.charset.StandardCharsets.US_ASCII);

        List<HpglCommand> commands = parseHpglCommands(content);

        try (PdfDocumentBuilder builder = pdfBackend.createBuilder()) {
            builder.newPage(PAGE_WIDTH, PAGE_HEIGHT);

            if (!hasRenderableCommands(commands)) {
                builder.addText("HPGL/PLT file contains no drawing content.", MARGIN, PAGE_HEIGHT / 2);
                builder.save(pdfFile);
                return;
            }

            // First pass: compute bounding box
            float[] bounds = computeBoundingBox(commands);
            float minX = bounds[0], minY = bounds[1], maxX = bounds[2], maxY = bounds[3];

            float boundsWidth = maxX - minX;
            float boundsHeight = maxY - minY;

            float scale;
            float offsetX;
            float offsetY;

            if (boundsWidth < 0.001f && boundsHeight < 0.001f) {
                // All points at same location — center with default scale
                scale = 1.0f;
                offsetX = PAGE_WIDTH / 2;
                offsetY = PAGE_HEIGHT / 2;
            } else {
                float scaleX = (boundsWidth > 0.001f) ? USABLE_WIDTH / boundsWidth : Float.MAX_VALUE;
                float scaleY = (boundsHeight > 0.001f) ? USABLE_HEIGHT / boundsHeight : Float.MAX_VALUE;
                scale = Math.min(scaleX, scaleY);
                offsetX = MARGIN + (USABLE_WIDTH - boundsWidth * scale) / 2;
                offsetY = MARGIN + (USABLE_HEIGHT - boundsHeight * scale) / 2;
            }

            // Second pass: render
            renderCommands(commands, builder, scale, offsetX, offsetY, minX, minY);

            builder.save(pdfFile);
        }
    }

    /**
     * Parses HPGL commands from raw content using indexOf-based scanning.
     * No regex is used on user content (ReDoS prevention).
     */
    List<HpglCommand> parseHpglCommands(String content) {
        List<HpglCommand> commands = new ArrayList<>();
        int len = content.length();
        int i = 0;

        while (i < len) {
            // Skip whitespace
            while (i < len && Character.isWhitespace(content.charAt(i))) {
                i++;
            }
            if (i >= len) break;

            char c1 = content.charAt(i);

            // Look for two uppercase letters indicating a command
            if (Character.isUpperCase(c1) && i + 1 < len && Character.isUpperCase(content.charAt(i + 1))) {
                String cmdType = content.substring(i, i + 2);
                i += 2;

                // Handle LB (Label) command specially — text until ETX or semicolon
                if ("LB".equals(cmdType)) {
                    StringBuilder labelBuf = new StringBuilder();
                    while (i < len) {
                        char ch = content.charAt(i);
                        if (ch == 0x03 || ch == ';') {
                            i++; // skip terminator
                            break;
                        }
                        labelBuf.append(ch);
                        i++;
                    }
                    commands.add(new HpglCommand("LB", new float[0], labelBuf.toString()));
                    continue;
                }

                // Extract numeric parameters until semicolon or next uppercase letter pair
                List<Float> paramList = new ArrayList<>();
                StringBuilder numBuf = new StringBuilder();

                while (i < len) {
                    char ch = content.charAt(i);

                    if (ch == ';') {
                        // End of command
                        if (!numBuf.isEmpty()) {
                            Float val = parseFloat(numBuf.toString());
                            if (val != null) paramList.add(val);
                            numBuf.setLength(0);
                        }
                        i++;
                        break;
                    } else if (Character.isUpperCase(ch) && i + 1 < len && Character.isUpperCase(content.charAt(i + 1))) {
                        // Next command starts — don't consume it
                        if (!numBuf.isEmpty()) {
                            Float val = parseFloat(numBuf.toString());
                            if (val != null) paramList.add(val);
                            numBuf.setLength(0);
                        }
                        break;
                    } else if (ch == ',' || Character.isWhitespace(ch)) {
                        // Parameter separator
                        if (!numBuf.isEmpty()) {
                            Float val = parseFloat(numBuf.toString());
                            if (val != null) paramList.add(val);
                            numBuf.setLength(0);
                        }
                        i++;
                    } else if (ch == '-' || ch == '+' || ch == '.' || Character.isDigit(ch)) {
                        numBuf.append(ch);
                        i++;
                    } else {
                        // Unknown character — skip
                        i++;
                    }
                }

                // Flush any remaining number
                if (!numBuf.isEmpty()) {
                    Float val = parseFloat(numBuf.toString());
                    if (val != null) paramList.add(val);
                }

                float[] params = new float[paramList.size()];
                for (int p = 0; p < paramList.size(); p++) {
                    params[p] = paramList.get(p);
                }

                commands.add(new HpglCommand(cmdType, params, null));
            } else {
                // Not a recognized command start — skip character
                i++;
            }
        }

        return commands;
    }

    /**
     * Determines if the command list contains any renderable drawing commands.
     */
    boolean hasRenderableCommands(List<HpglCommand> commands) {
        for (HpglCommand cmd : commands) {
            switch (cmd.type()) {
                case "PD", "CI", "AA", "AR", "LB" -> {
                    return true;
                }
                default -> {}
            }
        }
        return false;
    }

    /**
     * Computes the bounding box by walking all commands and tracking min/max coordinates.
     * Returns [minX, minY, maxX, maxY].
     */
    float[] computeBoundingBox(List<HpglCommand> commands) {
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        float curX = 0, curY = 0;
        boolean hasCoords = false;

        for (HpglCommand cmd : commands) {
            switch (cmd.type()) {
                case "PU", "PD", "PA" -> {
                    float[] p = cmd.params();
                    for (int j = 0; j + 1 < p.length; j += 2) {
                        curX = p[j];
                        curY = p[j + 1];
                        minX = Math.min(minX, curX);
                        minY = Math.min(minY, curY);
                        maxX = Math.max(maxX, curX);
                        maxY = Math.max(maxY, curY);
                        hasCoords = true;
                    }
                }
                case "PR" -> {
                    float[] p = cmd.params();
                    for (int j = 0; j + 1 < p.length; j += 2) {
                        curX += p[j];
                        curY += p[j + 1];
                        minX = Math.min(minX, curX);
                        minY = Math.min(minY, curY);
                        maxX = Math.max(maxX, curX);
                        maxY = Math.max(maxY, curY);
                        hasCoords = true;
                    }
                }
                case "CI" -> {
                    float[] p = cmd.params();
                    if (p.length >= 1) {
                        float r = p[0];
                        minX = Math.min(minX, curX - r);
                        minY = Math.min(minY, curY - r);
                        maxX = Math.max(maxX, curX + r);
                        maxY = Math.max(maxY, curY + r);
                        hasCoords = true;
                    }
                }
                case "AA" -> {
                    float[] p = cmd.params();
                    if (p.length >= 3) {
                        float cx = p[0], cy = p[1];
                        float radius = (float) Math.sqrt(
                            (curX - cx) * (curX - cx) + (curY - cy) * (curY - cy));
                        minX = Math.min(minX, cx - radius);
                        minY = Math.min(minY, cy - radius);
                        maxX = Math.max(maxX, cx + radius);
                        maxY = Math.max(maxY, cy + radius);
                        hasCoords = true;
                        // Update position to end of arc
                        float startAngle = (float) Math.toDegrees(Math.atan2(curY - cy, curX - cx));
                        float sweep = p[2];
                        float endAngle = (float) Math.toRadians(startAngle + sweep);
                        curX = cx + radius * (float) Math.cos(endAngle);
                        curY = cy + radius * (float) Math.sin(endAngle);
                    }
                }
                case "AR" -> {
                    float[] p = cmd.params();
                    if (p.length >= 3) {
                        float cx = curX + p[0], cy = curY + p[1];
                        float radius = (float) Math.sqrt(p[0] * p[0] + p[1] * p[1]);
                        minX = Math.min(minX, cx - radius);
                        minY = Math.min(minY, cy - radius);
                        maxX = Math.max(maxX, cx + radius);
                        maxY = Math.max(maxY, cy + radius);
                        hasCoords = true;
                        // Update position to end of arc
                        float startAngle = (float) Math.toDegrees(Math.atan2(curY - cy, curX - cx));
                        float sweep = p[2];
                        float endAngle = (float) Math.toRadians(startAngle + sweep);
                        curX = cx + radius * (float) Math.cos(endAngle);
                        curY = cy + radius * (float) Math.sin(endAngle);
                    }
                }
                case "IN" -> {
                    curX = 0;
                    curY = 0;
                }
                default -> {}
            }
        }

        if (!hasCoords) {
            return new float[]{0, 0, 0, 0};
        }
        return new float[]{minX, minY, maxX, maxY};
    }

    /**
     * Renders commands to the PDF builder with coordinate scaling.
     */
    void renderCommands(List<HpglCommand> commands, PdfDocumentBuilder builder,
                        float scale, float offsetX, float offsetY,
                        float minX, float minY) throws IOException {

        HpglState state = new HpglState();
        applyPenColor(builder, state.currentPen);
        builder.setLineWidth(state.penWidth);

        for (HpglCommand cmd : commands) {
            switch (cmd.type()) {
                case "IN" -> {
                    state.currentX = 0;
                    state.currentY = 0;
                    state.penDown = false;
                    state.currentPen = 1;
                    state.penWidth = 0.35f;
                    applyPenColor(builder, state.currentPen);
                    builder.setLineWidth(state.penWidth);
                }
                case "SP" -> {
                    if (cmd.params().length >= 1) {
                        int pen = Math.max(0, Math.min(8, (int) cmd.params()[0]));
                        state.currentPen = pen;
                        applyPenColor(builder, pen);
                    }
                }
                case "PW" -> {
                    if (cmd.params().length >= 1) {
                        state.penWidth = cmd.params()[0];
                        builder.setLineWidth(state.penWidth);
                    }
                }
                case "PU" -> {
                    state.penDown = false;
                    float[] p = cmd.params();
                    for (int j = 0; j + 1 < p.length; j += 2) {
                        state.currentX = p[j];
                        state.currentY = p[j + 1];
                    }
                }
                case "PD" -> {
                    state.penDown = true;
                    float[] p = cmd.params();
                    for (int j = 0; j + 1 < p.length; j += 2) {
                        float newX = p[j];
                        float newY = p[j + 1];
                        float sx1 = scaleX(state.currentX, minX, scale, offsetX);
                        float sy1 = scaleY(state.currentY, minY, scale, offsetY);
                        float sx2 = scaleX(newX, minX, scale, offsetX);
                        float sy2 = scaleY(newY, minY, scale, offsetY);
                        builder.drawLine(sx1, sy1, sx2, sy2);
                        state.currentX = newX;
                        state.currentY = newY;
                    }
                }
                case "PA" -> {
                    float[] p = cmd.params();
                    for (int j = 0; j + 1 < p.length; j += 2) {
                        float newX = p[j];
                        float newY = p[j + 1];
                        if (state.penDown) {
                            float sx1 = scaleX(state.currentX, minX, scale, offsetX);
                            float sy1 = scaleY(state.currentY, minY, scale, offsetY);
                            float sx2 = scaleX(newX, minX, scale, offsetX);
                            float sy2 = scaleY(newY, minY, scale, offsetY);
                            builder.drawLine(sx1, sy1, sx2, sy2);
                        }
                        state.currentX = newX;
                        state.currentY = newY;
                    }
                }
                case "PR" -> {
                    float[] p = cmd.params();
                    for (int j = 0; j + 1 < p.length; j += 2) {
                        float newX = state.currentX + p[j];
                        float newY = state.currentY + p[j + 1];
                        if (state.penDown) {
                            float sx1 = scaleX(state.currentX, minX, scale, offsetX);
                            float sy1 = scaleY(state.currentY, minY, scale, offsetY);
                            float sx2 = scaleX(newX, minX, scale, offsetX);
                            float sy2 = scaleY(newY, minY, scale, offsetY);
                            builder.drawLine(sx1, sy1, sx2, sy2);
                        }
                        state.currentX = newX;
                        state.currentY = newY;
                    }
                }
                case "CI" -> {
                    float[] p = cmd.params();
                    if (p.length >= 1) {
                        float r = p[0] * scale;
                        float cx = scaleX(state.currentX, minX, scale, offsetX);
                        float cy = scaleY(state.currentY, minY, scale, offsetY);
                        builder.drawCircle(cx, cy, r);
                    }
                }
                case "AA" -> {
                    float[] p = cmd.params();
                    if (p.length >= 3) {
                        float cx = p[0], cy = p[1];
                        float sweep = p[2];
                        float radius = (float) Math.sqrt(
                            (state.currentX - cx) * (state.currentX - cx) +
                            (state.currentY - cy) * (state.currentY - cy));
                        float startAngle = (float) Math.toDegrees(
                            Math.atan2(state.currentY - cy, state.currentX - cx));

                        float scaledCx = scaleX(cx, minX, scale, offsetX);
                        float scaledCy = scaleY(cy, minY, scale, offsetY);
                        float scaledRadius = radius * scale;

                        builder.drawArc(scaledCx, scaledCy, scaledRadius, startAngle, sweep);

                        // Update position to end of arc
                        float endAngle = (float) Math.toRadians(startAngle + sweep);
                        state.currentX = cx + radius * (float) Math.cos(endAngle);
                        state.currentY = cy + radius * (float) Math.sin(endAngle);
                    }
                }
                case "AR" -> {
                    float[] p = cmd.params();
                    if (p.length >= 3) {
                        float cx = state.currentX + p[0];
                        float cy = state.currentY + p[1];
                        float sweep = p[2];
                        float radius = (float) Math.sqrt(p[0] * p[0] + p[1] * p[1]);
                        float startAngle = (float) Math.toDegrees(
                            Math.atan2(state.currentY - cy, state.currentX - cx));

                        float scaledCx = scaleX(cx, minX, scale, offsetX);
                        float scaledCy = scaleY(cy, minY, scale, offsetY);
                        float scaledRadius = radius * scale;

                        builder.drawArc(scaledCx, scaledCy, scaledRadius, startAngle, sweep);

                        // Update position to end of arc
                        float endAngle = (float) Math.toRadians(startAngle + sweep);
                        state.currentX = cx + radius * (float) Math.cos(endAngle);
                        state.currentY = cy + radius * (float) Math.sin(endAngle);
                    }
                }
                case "LB" -> {
                    if (cmd.labelText() != null && !cmd.labelText().isEmpty()) {
                        float sx = scaleX(state.currentX, minX, scale, offsetX);
                        float sy = scaleY(state.currentY, minY, scale, offsetY);
                        builder.addText(cmd.labelText(), sx, sy);
                    }
                }
                default -> {
                    // Unknown command — skip silently
                }
            }
        }
    }

    private float scaleX(float rawX, float minX, float scale, float offsetX) {
        return (rawX - minX) * scale + offsetX;
    }

    private float scaleY(float rawY, float minY, float scale, float offsetY) {
        // Y-flip for PDF coordinate system (origin at bottom-left)
        return PAGE_HEIGHT - ((rawY - minY) * scale + offsetY);
    }

    private void applyPenColor(PdfDocumentBuilder builder, int pen) throws IOException {
        int idx = Math.max(0, Math.min(pen, PEN_COLORS.length - 1));
        int[] rgb = PEN_COLORS[idx];
        builder.setStrokeColor(rgb[0], rgb[1], rgb[2]);
    }

    private static Float parseFloat(String s) {
        try {
            return Float.parseFloat(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
