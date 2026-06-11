package com.xtopdf.xtopdf.services.conversion.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Service for converting JSON files to PDF.
 * Parses JSON via Jackson ObjectMapper and renders pretty-printed output
 * in monospace font (10pt) using the PDF backend abstraction layer.
 */
@Slf4j
@Service
public class JsonToPdfService {

    private final PdfBackendProvider pdfBackend;
    private final ObjectMapper objectMapper;

    public JsonToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
        this.objectMapper = new ObjectMapper();
    }

    public void convertJsonToPdf(MultipartFile jsonFile, File pdfFile) throws IOException {
        if (jsonFile == null) {
            throw new NullPointerException("Input file must not be null");
        }
        if (pdfFile == null) {
            throw new IOException("Output file must not be null");
        }

        String rawJson = new String(jsonFile.getBytes(), StandardCharsets.UTF_8);

        String prettyJson;
        if (rawJson.isBlank()) {
            // Empty/blank input — produce an empty PDF gracefully
            prettyJson = "";
        } else {
            try {
                // Parse then pretty-print (normalizes minified JSON to 2-space indented format)
                Object jsonTree = objectMapper.readValue(rawJson, Object.class);
                prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonTree);
            } catch (Exception e) {
                log.warn("Failed to parse JSON, writing raw content to PDF");
                prettyJson = rawJson;
            }
        }

        try (PdfDocumentBuilder builder = pdfBackend.createBuilder()) {
            if (prettyJson.isEmpty()) {
                builder.addParagraph("");
            } else {
                for (String line : prettyJson.split("\n")) {
                    builder.addFormattedText(line + "\n", false, false, 10f);
                    builder.endParagraph();
                }
            }
            builder.save(pdfFile);
        }
    }
}
