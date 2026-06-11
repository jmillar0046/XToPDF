package com.xtopdf.xtopdf.services.conversion.threed;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Service to convert STEP/STP files to PDF.
 * STEP (Standard for the Exchange of Product Data) is a text-based CAD format (ISO 10303).
 * This converter parses the STEP file structure and renders meaningful product/geometry
 * metadata in a formatted PDF layout using headings, indentation, and structured sections.
 */
@Slf4j
@Service
public class StepToPdfService {

    private static final int MAX_ASSEMBLY_DEPTH = 10;

    private final PdfBackendProvider pdfBackend;

    public StepToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }

    // ---------------------------------------------------------------
    // Data models
    // ---------------------------------------------------------------

    record StepProduct(String id, String name) {}

    record StepShape(String id, String name, String geometryType) {}

    record StepAssemblyNode(String parentRef, String childRef, String name) {}

    static class StepMetadata {
        String fileDescription = "";
        String fileName = "";
        String fileSchema = "";
        List<StepProduct> products = new ArrayList<>();
        List<StepShape> shapes = new ArrayList<>();
        List<StepAssemblyNode> assemblyNodes = new ArrayList<>();
        Map<String, Integer> entityCounts = new LinkedHashMap<>();
        int totalEntityCount = 0;
    }

    // ---------------------------------------------------------------
    // Public API
    // ---------------------------------------------------------------

    public void convertStepToPdf(MultipartFile stepFile, File pdfFile) throws IOException {
        try (PdfDocumentBuilder builder = pdfBackend.createBuilder()) {
            StepMetadata metadata = parseStepFile(stepFile);

            if (metadata.totalEntityCount == 0) {
                renderEmptyFileMessage(builder, stepFile.getOriginalFilename());
            } else {
                renderStepMetadata(metadata, builder, stepFile.getOriginalFilename());
            }

            builder.save(pdfFile);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to convert STEP file to PDF", e);
            throw new IOException("Failed to convert STEP file", e);
        }
    }

    // ---------------------------------------------------------------
    // Parsing (indexOf-based, NO regex on user content)
    // ---------------------------------------------------------------

    StepMetadata parseStepFile(MultipartFile file) throws IOException {
        StepMetadata metadata = new StepMetadata();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            parseFromReader(reader, metadata);
        }

        return metadata;
    }

    // Package-private for testing with raw content
    StepMetadata parseStepContent(String content) throws IOException {
        StepMetadata metadata = new StepMetadata();

        try (BufferedReader reader = new BufferedReader(new StringReader(content))) {
            parseFromReader(reader, metadata);
        }

        return metadata;
    }

    private void parseFromReader(BufferedReader reader, StepMetadata metadata) throws IOException {
        String line;
        boolean inHeader = false;
        boolean inData = false;

        while ((line = reader.readLine()) != null) {
            String trimmed = line.trim();

            if (trimmed.equals("HEADER;")) {
                inHeader = true;
                continue;
            } else if (trimmed.equals("ENDSEC;") && inHeader) {
                inHeader = false;
                continue;
            } else if (trimmed.equals("DATA;")) {
                inData = true;
                continue;
            } else if (trimmed.equals("ENDSEC;") && inData) {
                inData = false;
                break;
            }

            if (inHeader) {
                parseHeaderLine(trimmed, metadata);
            } else if (inData && trimmed.startsWith("#")) {
                parseDataLine(trimmed, metadata);
            }
        }
    }

    private void parseHeaderLine(String line, StepMetadata metadata) {
        if (line.startsWith("FILE_DESCRIPTION")) {
            metadata.fileDescription = extractHeaderValue(line);
        } else if (line.startsWith("FILE_NAME")) {
            metadata.fileName = extractHeaderValue(line);
        } else if (line.startsWith("FILE_SCHEMA")) {
            metadata.fileSchema = extractHeaderValue(line);
        }
    }

    /**
     * Extracts the content between the first '(' and last ')' in a header line.
     * Uses indexOf-based extraction, no regex.
     */
    private String extractHeaderValue(String line) {
        int openParen = line.indexOf('(');
        int closeParen = line.lastIndexOf(')');
        if (openParen >= 0 && closeParen > openParen) {
            return line.substring(openParen + 1, closeParen).trim();
        }
        return "";
    }

    private void parseDataLine(String line, StepMetadata metadata) {
        // Entity ID: between # and =
        int hashIdx = line.indexOf('#');
        int equalsIdx = line.indexOf('=', hashIdx);
        if (hashIdx < 0 || equalsIdx < 0) {
            return;
        }

        String entityId = line.substring(hashIdx + 1, equalsIdx).trim();

        // Entity type: between = and (
        int parenIdx = line.indexOf('(', equalsIdx);
        if (parenIdx < 0) {
            return;
        }

        String entityType = line.substring(equalsIdx + 1, parenIdx).trim();

        // Count this entity type
        metadata.entityCounts.merge(entityType, 1, Integer::sum);
        metadata.totalEntityCount++;

        // Special entity handling
        if (entityType.equals("PRODUCT")) {
            parseProductEntity(entityId, line, parenIdx, metadata);
        } else if (entityType.contains("SHAPE_REPRESENTATION")
                || entityType.contains("BREP_SHAPE_REPRESENTATION")
                || entityType.contains("SURFACE_SHAPE_REPRESENTATION")
                || entityType.contains("WIREFRAME_SHAPE_REPRESENTATION")) {
            parseShapeEntity(entityId, entityType, line, parenIdx, metadata);
        } else if (entityType.equals("NEXT_ASSEMBLY_USAGE_OCCURRENCE")) {
            parseAssemblyNode(line, parenIdx, metadata);
        }
    }

    private void parseProductEntity(String entityId, String line, int parenStart, StepMetadata metadata) {
        List<String> params = extractQuotedParams(line, parenStart);
        // PRODUCT('name','description','id',...) — typically name is first param
        String name = params.size() > 0 ? params.get(0) : "";
        String productId = params.size() > 1 ? params.get(1) : entityId;
        metadata.products.add(new StepProduct(productId, name));
    }

    private void parseShapeEntity(String entityId, String entityType, String line, int parenStart, StepMetadata metadata) {
        List<String> params = extractQuotedParams(line, parenStart);
        String name = params.size() > 0 ? params.get(0) : "";
        String geometryType = classifyGeometryType(entityType);
        metadata.shapes.add(new StepShape(entityId, name, geometryType));
    }

    private void parseAssemblyNode(String line, int parenStart, StepMetadata metadata) {
        // NEXT_ASSEMBLY_USAGE_OCCURRENCE('id','name','desc',#parentRef,#childRef,$)
        List<String> quotedParams = extractQuotedParams(line, parenStart);
        List<String> refParams = extractRefParams(line, parenStart);

        String name = quotedParams.size() > 1 ? quotedParams.get(1) : "";
        String parentRef = refParams.size() > 0 ? refParams.get(0) : "";
        String childRef = refParams.size() > 1 ? refParams.get(1) : "";

        if (!parentRef.isEmpty() || !childRef.isEmpty()) {
            metadata.assemblyNodes.add(new StepAssemblyNode(parentRef, childRef, name));
        }
    }

    /**
     * Extracts quoted parameters (text between single quotes) using character walking.
     * No regex used.
     */
    List<String> extractQuotedParams(String line, int startIdx) {
        List<String> params = new ArrayList<>();
        boolean inQuote = false;
        StringBuilder current = new StringBuilder();

        for (int i = startIdx; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\'') {
                if (inQuote) {
                    params.add(current.toString());
                    current.setLength(0);
                    inQuote = false;
                } else {
                    inQuote = true;
                }
            } else if (inQuote) {
                current.append(c);
            }
        }

        return params;
    }

    /**
     * Extracts reference parameters (#NNN) from the parameter section.
     * Uses indexOf-based extraction, no regex.
     */
    List<String> extractRefParams(String line, int startIdx) {
        List<String> refs = new ArrayList<>();

        int i = startIdx;
        while (i < line.length()) {
            int hashIdx = line.indexOf('#', i);
            if (hashIdx < 0) {
                break;
            }
            // Read digits after #
            int end = hashIdx + 1;
            while (end < line.length() && Character.isDigit(line.charAt(end))) {
                end++;
            }
            if (end > hashIdx + 1) {
                refs.add(line.substring(hashIdx + 1, end));
            }
            i = end;
        }

        return refs;
    }

    String classifyGeometryType(String entityType) {
        if (entityType.contains("ADVANCED_BREP") || entityType.contains("BREP")) {
            return "BREP (solid)";
        } else if (entityType.contains("SURFACE")) {
            return "Surface";
        } else if (entityType.contains("WIREFRAME")) {
            return "Wireframe";
        }
        return "General";
    }

    // ---------------------------------------------------------------
    // Rendering
    // ---------------------------------------------------------------

    private void renderEmptyFileMessage(PdfDocumentBuilder builder, String filename) throws IOException {
        builder.addFormattedText("STEP File Analysis", true, false, 16f);
        builder.endParagraph();
        builder.endParagraph();
        builder.addFormattedText("The file ", false, false, 11f);
        builder.addFormattedText(filename != null ? filename : "unknown", false, true, 11f);
        builder.addFormattedText(" could not be analyzed. No parseable entities were found.", false, false, 11f);
        builder.endParagraph();
    }

    private void renderStepMetadata(StepMetadata metadata, PdfDocumentBuilder builder, String filename) throws IOException {
        // Title
        builder.addFormattedText("STEP File Analysis", true, false, 16f);
        builder.endParagraph();
        builder.endParagraph();

        // File Information Section
        renderFileInformation(metadata, builder, filename);

        // Products Section
        if (!metadata.products.isEmpty()) {
            renderProducts(metadata, builder);
        }

        // Geometry Summary Section
        if (!metadata.shapes.isEmpty()) {
            renderShapes(metadata, builder);
        }

        // Assembly Hierarchy Section
        if (!metadata.assemblyNodes.isEmpty()) {
            renderAssemblyTree(metadata, builder);
        }

        // Entity Statistics Section
        renderEntityStatistics(metadata, builder);
    }

    private void renderFileInformation(StepMetadata metadata, PdfDocumentBuilder builder, String filename) throws IOException {
        builder.addFormattedText("File Information", true, false, 14f);
        builder.endParagraph();

        if (filename != null && !filename.isEmpty()) {
            builder.addFormattedText("  File: ", true, false, 11f);
            builder.addFormattedText(filename, false, false, 11f);
            builder.endParagraph();
        }

        builder.addFormattedText("  Format: ", true, false, 11f);
        builder.addFormattedText("STEP (ISO 10303)", false, false, 11f);
        builder.endParagraph();

        if (!metadata.fileDescription.isEmpty()) {
            builder.addFormattedText("  Description: ", true, false, 11f);
            builder.addFormattedText(metadata.fileDescription, false, false, 11f);
            builder.endParagraph();
        }

        if (!metadata.fileName.isEmpty()) {
            builder.addFormattedText("  Original Name: ", true, false, 11f);
            builder.addFormattedText(metadata.fileName, false, false, 11f);
            builder.endParagraph();
        }

        if (!metadata.fileSchema.isEmpty()) {
            builder.addFormattedText("  Schema: ", true, false, 11f);
            builder.addFormattedText(metadata.fileSchema, false, false, 11f);
            builder.endParagraph();
        }

        builder.endParagraph();
    }

    private void renderProducts(StepMetadata metadata, PdfDocumentBuilder builder) throws IOException {
        builder.addFormattedText("Products", true, false, 14f);
        builder.endParagraph();

        for (int i = 0; i < metadata.products.size(); i++) {
            StepProduct product = metadata.products.get(i);
            builder.addFormattedText("  " + (i + 1) + ". " + product.name(), false, false, 11f);
            if (!product.id().isEmpty()) {
                builder.addFormattedText("  (ID: " + product.id() + ")", false, true, 10f, 100, 100, 100);
            }
            builder.endParagraph();
        }

        builder.endParagraph();
    }

    private void renderShapes(StepMetadata metadata, PdfDocumentBuilder builder) throws IOException {
        builder.addFormattedText("Geometry Summary", true, false, 14f);
        builder.endParagraph();

        for (StepShape shape : metadata.shapes) {
            builder.addFormattedText("  \u2022 ", false, false, 11f);
            String label = shape.name().isEmpty() ? "Shape #" + shape.id() : shape.name();
            builder.addFormattedText(label, false, false, 11f);
            builder.addFormattedText("  [" + shape.geometryType() + "]", false, true, 10f, 100, 100, 100);
            builder.endParagraph();
        }

        builder.endParagraph();
    }

    private void renderAssemblyTree(StepMetadata metadata, PdfDocumentBuilder builder) throws IOException {
        builder.addFormattedText("Assembly Hierarchy", true, false, 14f);
        builder.endParagraph();

        // Build adjacency map: parent -> list of children
        Map<String, List<StepAssemblyNode>> childrenMap = new LinkedHashMap<>();
        Set<String> childRefs = new HashSet<>();

        for (StepAssemblyNode node : metadata.assemblyNodes) {
            childrenMap.computeIfAbsent(node.parentRef(), k -> new ArrayList<>()).add(node);
            childRefs.add(node.childRef());
        }

        // Find roots (parents that are never children)
        Set<String> roots = new LinkedHashSet<>();
        for (StepAssemblyNode node : metadata.assemblyNodes) {
            if (!childRefs.contains(node.parentRef())) {
                roots.add(node.parentRef());
            }
        }

        // If no clear roots, use all parents
        if (roots.isEmpty()) {
            roots.addAll(childrenMap.keySet());
        }

        // Render tree depth-first
        for (String root : roots) {
            builder.addFormattedText("  #" + root, false, false, 11f);
            builder.endParagraph();
            renderAssemblyChildren(childrenMap, root, 1, builder, new HashSet<>());
        }

        builder.endParagraph();
    }

    private void renderAssemblyChildren(Map<String, List<StepAssemblyNode>> childrenMap,
                                         String parentRef, int depth,
                                         PdfDocumentBuilder builder,
                                         Set<String> visited) throws IOException {
        if (depth > MAX_ASSEMBLY_DEPTH) {
            return;
        }
        if (visited.contains(parentRef)) {
            return; // Prevent circular references
        }
        visited.add(parentRef);

        List<StepAssemblyNode> children = childrenMap.get(parentRef);
        if (children == null) {
            return;
        }

        String indent = "  ".repeat(depth + 1);
        for (StepAssemblyNode child : children) {
            String label = child.name().isEmpty() ? "#" + child.childRef() : child.name();
            builder.addFormattedText(indent + "\u2514\u2500 " + label, false, false, 11f);
            builder.endParagraph();
            renderAssemblyChildren(childrenMap, child.childRef(), depth + 1, builder, visited);
        }
    }

    private void renderEntityStatistics(StepMetadata metadata, PdfDocumentBuilder builder) throws IOException {
        builder.addFormattedText("Entity Statistics", true, false, 14f);
        builder.endParagraph();

        builder.addFormattedText("  Total Entities: " + metadata.totalEntityCount, true, false, 11f);
        builder.endParagraph();
        builder.endParagraph();

        // Sort entity counts by frequency (descending)
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(metadata.entityCounts.entrySet());
        sorted.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        int displayCount = Math.min(sorted.size(), 30);
        for (int i = 0; i < displayCount; i++) {
            Map.Entry<String, Integer> entry = sorted.get(i);
            builder.addFormattedText("  " + entry.getKey() + ": ", false, false, 10f);
            builder.addFormattedText(String.valueOf(entry.getValue()), false, false, 10f, 100, 100, 100);
            builder.endParagraph();
        }

        if (sorted.size() > 30) {
            builder.addFormattedText("  ... and " + (sorted.size() - 30) + " more entity types", false, true, 10f, 100, 100, 100);
            builder.endParagraph();
        }
    }
}
