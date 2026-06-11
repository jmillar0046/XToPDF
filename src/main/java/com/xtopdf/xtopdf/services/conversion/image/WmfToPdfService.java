package com.xtopdf.xtopdf.services.conversion.image;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import com.xtopdf.xtopdf.services.model.WmfFileData;
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
 * Service to convert Windows Metafile files (WMF) to PDF.
 * WMF is a binary vector graphics format for Windows.
 * Attempts to render the metafile as an image; falls back to file statistics text.
 * Uses the PDF backend abstraction layer with Apache PDFBox.
 */
@Slf4j
@Service
public class WmfToPdfService {

    private static final int MIN_RENDER_DIMENSION = 100;
    private static final int MAX_RENDER_DIMENSION = 2000;

    private final PdfBackendProvider pdfBackend;

    public WmfToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }

    public void convertWmfToPdf(MultipartFile inputFile, File pdfFile) throws IOException {
        if (inputFile == null) {
            throw new IOException("Input file must not be null");
        }
        if (pdfFile == null) {
            throw new IOException("Output file must not be null");
        }

        try (PdfDocumentBuilder builder = pdfBackend.createBuilder()) {
            byte[] imageBytes = renderWmfToImage(inputFile);

            if (imageBytes != null) {
                builder.addImage(imageBytes);
            } else {
                renderFallbackStatistics(builder, inputFile);
            }
            builder.save(pdfFile);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Error converting WMF to PDF", e);
        }
    }

    /**
     * Attempts to render the WMF metafile as a PNG image.
     * Parses the WMF header for dimensions and creates a placeholder rendering
     * with the metafile bounds information.
     *
     * @param file the WMF file to render
     * @return PNG bytes if rendering succeeds, null otherwise
     */
    byte[] renderWmfToImage(MultipartFile file) {
        try {
            WmfFileData header = parseWmfFile(file);
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
            g2d.drawString("WMF: " + file.getOriginalFilename(), 10, 20);
            g2d.drawString(String.format("Size: %dx%d", width, height), 10, 40);
            g2d.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            log.warn("WMF rendering failed, using fallback text output");
            return null;
        }
    }

    /**
     * Renders the current text-statistics fallback when image rendering fails.
     */
    void renderFallbackStatistics(PdfDocumentBuilder builder, MultipartFile inputFile) throws IOException {
        WmfFileData wmfData;
        try {
            wmfData = parseWmfFile(inputFile);
        } catch (IOException e) {
            wmfData = new WmfFileData();
        }

        StringBuilder content = new StringBuilder();
        content.append("Windows Metafile Analysis\n\n");
        content.append("File: ").append(inputFile.getOriginalFilename()).append("\n");
        content.append("Format: WMF (Windows Metafile)\n");
        content.append("Type: ").append(wmfData.isPlaceable ? "Placeable" : "Standard").append("\n\n");
        content.append("Metafile Statistics:\n");
        content.append("File Size: ").append(formatSize(inputFile.getSize())).append("\n");

        if (wmfData.isPlaceable && wmfData.boundsValid) {
            content.append("\nBounds:\n");
            content.append(String.format("  Left: %d, Top: %d\n", wmfData.boundsLeft, wmfData.boundsTop));
            content.append(String.format("  Right: %d, Bottom: %d\n", wmfData.boundsRight, wmfData.boundsBottom));
            content.append(String.format("  Width: %d, Height: %d\n",
                    wmfData.boundsRight - wmfData.boundsLeft,
                    wmfData.boundsBottom - wmfData.boundsTop));
        }

        if (wmfData.maxRecordSize > 0) {
            content.append("\nMax Record Size: ").append(wmfData.maxRecordSize).append(" words\n");
        }

        content.append("\nNote: This PDF contains metafile statistics. For visual rendering, use image conversion tools or Windows applications.");

        builder.addParagraph(content.toString());
    }

    private WmfFileData parseWmfFile(MultipartFile file) throws IOException {
        WmfFileData data = new WmfFileData();

        byte[] bytes = file.getBytes();
        if (bytes.length < 18) { // Minimum WMF header size
            return data;
        }

        try {
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            buffer.order(ByteOrder.LITTLE_ENDIAN);

            // Check for placeable metafile header (magic number 0x9AC6CDD7)
            int magic = buffer.getInt();
            if (magic == 0x9AC6CDD7) {
                data.isPlaceable = true;

                // Skip handle (2 bytes)
                buffer.getShort();

                // Read bounding rectangle
                data.boundsLeft = buffer.getShort();
                data.boundsTop = buffer.getShort();
                data.boundsRight = buffer.getShort();
                data.boundsBottom = buffer.getShort();
                data.boundsValid = true;

                // Skip inch (2 bytes) and reserved (4 bytes) and checksum (2 bytes)
                buffer.position(buffer.position() + 8);
            } else {
                // Standard WMF, rewind
                buffer.position(0);
            }

            // Read standard WMF header
            int fileType = buffer.getShort() & 0xFFFF;
            int headerSize = buffer.getShort() & 0xFFFF;

            // Skip version (2 bytes)
            buffer.getShort();

            // Read file size in words
            int fileSizeWords = buffer.getInt();

            // Skip number of objects (2 bytes)
            buffer.getShort();

            // Read max record size
            data.maxRecordSize = buffer.getInt();

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
