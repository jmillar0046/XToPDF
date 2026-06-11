package com.xtopdf.xtopdf.services.conversion.cad;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Service to convert Design Web Format files (DWF) to PDF.
 * DWF is a ZIP-based format containing W2D drawing streams, images, and metadata.
 *
 * <p>Strategy:
 * <ol>
 *   <li>Open the DWF file as a ZIP archive and catalog all entries</li>
 *   <li>Extract any embedded images (PNG, JPG, BMP, TIFF) and render them</li>
 *   <li>If no images found, render enhanced package statistics</li>
 * </ol>
 */
@Slf4j
@Service
public class DwfToPdfService {

    private static final float PAGE_WIDTH = 595f;
    private static final float PAGE_HEIGHT = 842f;
    private static final float MARGIN = 50f;
    private static final long MAX_IMAGE_SIZE = 10_000_000L; // 10MB cap
    private static final long MAX_XML_SIZE = 1_000_000L;    // 1MB cap

    private final PdfBackendProvider pdfBackend;

    @Autowired
    public DwfToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }

    // --- Data models ---

    static class DwfPackageInfo {
        List<DwfEntry> entries = new ArrayList<>();
        List<byte[]> embeddedImages = new ArrayList<>();
        Map<String, String> metadata = new LinkedHashMap<>();
        boolean hasDrawingContent = false;
    }

    record DwfEntry(String name, long size, String type) {}

    // --- Public conversion method ---

    public void convertDwfToPdf(MultipartFile inputFile, File pdfFile) throws IOException {
        if (inputFile == null) {
            throw new IOException("Input file must not be null");
        }
        if (pdfFile == null) {
            throw new IOException("Output file must not be null");
        }

        DwfPackageInfo packageInfo = parseDwfPackage(inputFile);

        try (PdfDocumentBuilder builder = pdfBackend.createBuilder()) {
            if (!packageInfo.embeddedImages.isEmpty()) {
                renderEmbeddedImages(builder, packageInfo);
            } else {
                renderEnhancedStatistics(builder, inputFile, packageInfo);
            }
            builder.save(pdfFile);
        }
    }

    // --- Package parsing ---

    DwfPackageInfo parseDwfPackage(MultipartFile file) throws IOException {
        DwfPackageInfo info = new DwfPackageInfo();

        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                String nameLower = name.toLowerCase();
                long size = entry.getSize();
                String type = classifyEntry(nameLower);

                info.entries.add(new DwfEntry(name, size >= 0 ? size : 0, type));

                if (isImageFile(nameLower) && !entry.isDirectory()) {
                    byte[] data = readZipEntryData(zis);
                    if (data.length > 0 && data.length < MAX_IMAGE_SIZE) {
                        info.embeddedImages.add(data);
                        info.hasDrawingContent = true;
                    }
                } else if (nameLower.endsWith(".xml") && !entry.isDirectory()) {
                    byte[] data = readZipEntryData(zis);
                    if (data.length > 0 && data.length < MAX_XML_SIZE) {
                        extractXmlMetadata(new String(data), info.metadata);
                    }
                }
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Error parsing DWF package, using basic statistics: {}", e.getClass().getSimpleName());
        }

        return info;
    }

    // --- Rendering ---

    private void renderEmbeddedImages(PdfDocumentBuilder builder, DwfPackageInfo packageInfo) throws IOException {
        for (byte[] imageData : packageInfo.embeddedImages) {
            builder.newPage(PAGE_WIDTH, PAGE_HEIGHT);
            builder.addImage(imageData);
        }
    }

    void renderEnhancedStatistics(PdfDocumentBuilder builder, MultipartFile inputFile, DwfPackageInfo packageInfo) throws IOException {
        builder.newPage(PAGE_WIDTH, PAGE_HEIGHT);

        // Title
        builder.addFormattedText("DWF Package Analysis", true, false, 18f);
        builder.endParagraph();
        builder.endParagraph();

        // File info
        builder.addFormattedText("File Information", true, false, 14f);
        builder.endParagraph();

        String fileName = inputFile.getOriginalFilename() != null ? inputFile.getOriginalFilename() : "unknown";
        builder.addFormattedText("  Name: " + fileName, false, false, 11f);
        builder.endParagraph();
        builder.addFormattedText("  Format: DWF (Design Web Format)", false, false, 11f);
        builder.endParagraph();
        builder.addFormattedText("  Entries: " + packageInfo.entries.size(), false, false, 11f);
        builder.endParagraph();
        builder.endParagraph();

        // Metadata section (if found)
        if (!packageInfo.metadata.isEmpty()) {
            builder.addFormattedText("Metadata", true, false, 14f);
            builder.endParagraph();
            for (Map.Entry<String, String> meta : packageInfo.metadata.entrySet()) {
                builder.addFormattedText("  " + meta.getKey() + ": " + meta.getValue(), false, false, 11f);
                builder.endParagraph();
            }
            builder.endParagraph();
        }

        // Package contents
        if (packageInfo.entries.isEmpty()) {
            builder.addFormattedText("Package is empty \u2014 no content entries found.", false, true, 11f);
            builder.endParagraph();
        } else {
            builder.addFormattedText("Package Contents", true, false, 14f);
            builder.endParagraph();

            int displayLimit = Math.min(packageInfo.entries.size(), 50);
            for (int i = 0; i < displayLimit; i++) {
                DwfEntry entry = packageInfo.entries.get(i);
                String sizeStr = entry.size() > 0 ? formatSize(entry.size()) : "unknown size";
                builder.addFormattedText("  \u2022 " + entry.name() + " [" + entry.type() + ", " + sizeStr + "]", false, false, 10f);
                builder.endParagraph();
            }
            if (packageInfo.entries.size() > displayLimit) {
                builder.addFormattedText("  ... and " + (packageInfo.entries.size() - displayLimit) + " more entries", false, true, 10f);
                builder.endParagraph();
            }
        }

        builder.endParagraph();
        builder.addFormattedText("Note: For full DWF rendering, use Autodesk Design Review or specialized DWF tools.", false, true, 10f);
        builder.endParagraph();
    }

    // --- Utility methods ---

    String classifyEntry(String nameLower) {
        if (nameLower.endsWith(".w2d")) {
            return "Drawing (W2D)";
        } else if (isImageFile(nameLower)) {
            return "Image";
        } else if (nameLower.endsWith(".xml")) {
            return "XML Metadata";
        } else if (nameLower.endsWith(".ttf") || nameLower.endsWith(".otf")) {
            return "Font";
        } else {
            return "Other";
        }
    }

    private boolean isImageFile(String nameLower) {
        return nameLower.endsWith(".png")
            || nameLower.endsWith(".jpg")
            || nameLower.endsWith(".jpeg")
            || nameLower.endsWith(".bmp")
            || nameLower.endsWith(".tiff")
            || nameLower.endsWith(".tif");
    }

    byte[] readZipEntryData(ZipInputStream zis) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int bytesRead;
        long totalRead = 0;
        while ((bytesRead = zis.read(buffer)) != -1) {
            totalRead += bytesRead;
            if (totalRead > MAX_IMAGE_SIZE) {
                return new byte[0];
            }
            baos.write(buffer, 0, bytesRead);
        }
        return baos.toByteArray();
    }

    private void extractXmlMetadata(String xmlContent, Map<String, String> metadata) {
        extractTagValue(xmlContent, "Title", metadata);
        extractTagValue(xmlContent, "Author", metadata);
        extractTagValue(xmlContent, "Subject", metadata);
        extractTagValue(xmlContent, "Creator", metadata);
        extractTagValue(xmlContent, "CreationDate", metadata);
        extractTagValue(xmlContent, "Description", metadata);
    }

    private void extractTagValue(String xml, String tagName, Map<String, String> metadata) {
        // Case-insensitive search for the tag
        String xmlLower = xml.toLowerCase();
        String openTagLower = "<" + tagName.toLowerCase() + ">";
        String closeTagLower = "</" + tagName.toLowerCase() + ">";
        int startIdx = xmlLower.indexOf(openTagLower);
        if (startIdx >= 0) {
            int valueStart = startIdx + openTagLower.length();
            int endIdx = xmlLower.indexOf(closeTagLower, valueStart);
            if (endIdx > valueStart) {
                String value = xml.substring(valueStart, endIdx).trim();
                if (!value.isEmpty() && value.length() < 500) {
                    metadata.put(tagName, value);
                }
            }
        }
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }
}
