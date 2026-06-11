package com.xtopdf.xtopdf.services.conversion.image;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import com.xtopdf.xtopdf.services.model.EmfFileData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Service to convert Enhanced Metafile files (EMF) to PDF.
 * EMF is a binary vector graphics format for Windows.
 * Attempts to render the metafile as an image; falls back to file statistics text.
 * Uses the PDF backend abstraction layer with Apache PDFBox.
 */
@Slf4j
@Service
public class EmfToPdfService {

    private static final int MIN_RENDER_DIMENSION = 100;
    private static final int MAX_RENDER_DIMENSION = 2000;

    private final PdfBackendProvider pdfBackend;

    public EmfToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }

    public void convertEmfToPdf(MultipartFile inputFile, File pdfFile) throws IOException {
        if (inputFile == null) {
            throw new IOException("Input file must not be null");
        }
        if (pdfFile == null) {
            throw new IOException("Output file must not be null");
        }

        try (PdfDocumentBuilder builder = pdfBackend.createBuilder()) {
            byte[] imageBytes = renderMetafileToImage(inputFile);

            if (imageBytes != null) {
                builder.addImage(imageBytes);
            } else {
                renderFallbackStatistics(builder, inputFile);
            }
            builder.save(pdfFile);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Error converting EMF to PDF", e);
        }
    }

    /**
     * Attempts to render the EMF metafile as a PNG image.
     * Parses the EMF header for dimensions and creates a placeholder rendering
     * with the metafile bounds information.
     *
     * @param file the EMF file to render
     * @return PNG bytes if rendering succeeds, null otherwise
     */
    byte[] renderMetafileToImage(MultipartFile file) {
        try {
            EmfFileData header = parseEmfFile(file);
            if (!header.boundsValid) {
                return null;
            }

            int width = Math.max(Math.abs(header.boundsRight - header.boundsLeft), MIN_RENDER_DIMENSION);
            int height = Math.max(Math.abs(header.boundsBottom - header.boundsTop), MIN_RENDER_DIMENSION);
            width = Math.min(width, MAX_RENDER_DIMENSION);
            height = Math.min(height, MAX_RENDER_DIMENSION);

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, width, height);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(0, 0, width - 1, height - 1);
            g2d.drawString("EMF: " + file.getOriginalFilename(), 10, 20);
            g2d.drawString(String.format("Size: %dx%d", width, height), 10, 40);
            g2d.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            log.warn("EMF rendering failed, using fallback text output");
            return null;
        }
    }

    /**
     * Renders the current text-statistics fallback when image rendering fails.
     */
    void renderFallbackStatistics(PdfDocumentBuilder builder, MultipartFile inputFile) throws IOException {
        EmfFileData emfData;
        try {
            emfData = parseEmfFile(inputFile);
        } catch (IOException e) {
            emfData = new EmfFileData();
        }

        StringBuilder content = new StringBuilder();
        content.append("Enhanced Metafile Analysis\n\n");
        content.append("File: ").append(inputFile.getOriginalFilename()).append("\n");
        content.append("Format: EMF (Enhanced Metafile)\n\n");
        content.append("Metafile Statistics:\n");
        content.append("File Size: ").append(formatSize(inputFile.getSize())).append("\n");
        content.append("Record Count: ").append(emfData.recordCount).append("\n");

        if (emfData.boundsValid) {
            content.append("\nBounds:\n");
            content.append(String.format("  Left: %d, Top: %d\n", emfData.boundsLeft, emfData.boundsTop));
            content.append(String.format("  Right: %d, Bottom: %d\n", emfData.boundsRight, emfData.boundsBottom));
            content.append(String.format("  Width: %d, Height: %d\n",
                    emfData.boundsRight - emfData.boundsLeft,
                    emfData.boundsBottom - emfData.boundsTop));
        }

        content.append("\nNote: This PDF contains metafile statistics. For visual rendering, use image conversion tools or Windows applications.");

        builder.addParagraph(content.toString());
    }

    private EmfFileData parseEmfFile(MultipartFile file) throws IOException {
        EmfFileData data = new EmfFileData();

        byte[] bytes = file.getBytes();
        if (bytes.length < 88) { // Minimum EMF header size
            return data;
        }

        try {
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            buffer.order(ByteOrder.LITTLE_ENDIAN);

            // Read EMR_HEADER (type should be 1)
            int recordType = buffer.getInt();
            if (recordType != 1) {
                return data; // Not a valid EMF file
            }

            // Skip record size
            buffer.getInt();

            // Read bounds rectangle
            data.boundsLeft = buffer.getInt();
            data.boundsTop = buffer.getInt();
            data.boundsRight = buffer.getInt();
            data.boundsBottom = buffer.getInt();
            data.boundsValid = true;

            // Skip frame rectangle (16 bytes)
            buffer.position(buffer.position() + 16);

            // Skip signature (4 bytes)
            buffer.getInt();

            // Skip version (4 bytes)
            buffer.getInt();

            // Skip file size (4 bytes)
            buffer.getInt();

            // Read number of records
            data.recordCount = buffer.getInt();

        } catch (Exception e) {
            // If parsing fails, return what we have
        }

        return data;
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " bytes";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }
}
