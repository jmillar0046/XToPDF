package com.xtopdf.xtopdf.services.conversion.image;

import lombok.extern.slf4j.Slf4j;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Service to convert SVG (Scalable Vector Graphics) files to PDF using Apache Batik.
 *
 * <p>Uses Batik's PNGTranscoder to render SVG to a high-resolution PNG image,
 * then embeds the rendered image into a PDF document via PDFBox. This approach
 * supports the full SVG specification including gradients, paths, text, filters,
 * and complex styling.</p>
 *
 * <p><b>Supported Features:</b></p>
 * <ul>
 *   <li>SVG paths, shapes (rect, circle, ellipse, line, polyline, polygon)</li>
 *   <li>Gradients (linear and radial)</li>
 *   <li>Text rendering with fonts</li>
 *   <li>CSS styling (inline and internal stylesheets)</li>
 *   <li>Transforms (translate, rotate, scale, skew)</li>
 *   <li>Filters (blur, drop-shadow, etc.)</li>
 *   <li>Animations converted to static first-frame representation</li>
 * </ul>
 */
@Slf4j
@Service
public class SvgToPdfService {

    private static final float RENDER_DPI = 300f;
    private static final float POINTS_PER_INCH = 72f;

    public void convertSvgToPdf(MultipartFile svgFile, File pdfFile) throws IOException {
        var svgContent = new String(svgFile.getBytes(), StandardCharsets.UTF_8);

        // Strip SVG animations for static rendering
        var staticSvg = stripAnimations(svgContent);

        // Render SVG to PNG using Batik
        byte[] pngData = renderSvgToPng(staticSvg);

        // Embed PNG into PDF using PDFBox
        createPdfFromImage(pngData, pdfFile);

        log.info("PDF created successfully from SVG: {}", pdfFile.getName());
    }

    /**
     * Renders SVG content to a PNG byte array using Apache Batik's PNGTranscoder.
     */
    byte[] renderSvgToPng(String svgContent) throws IOException {
        var sanitized = sanitizeSvg(svgContent);

        try (var svgInput = new StringReader(sanitized);
             var pngOutput = new ByteArrayOutputStream()) {

            var transcoder = new PNGTranscoder();
            transcoder.addTranscodingHint(PNGTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER, 25.4f / RENDER_DPI);
            transcoder.addTranscodingHint(PNGTranscoder.KEY_ALLOW_EXTERNAL_RESOURCES, Boolean.FALSE);

            var input = new TranscoderInput(svgInput);
            var output = new TranscoderOutput(pngOutput);

            transcoder.transcode(input, output);
            pngOutput.flush();

            return pngOutput.toByteArray();
        } catch (Exception e) {
            throw new IOException("Error rendering SVG with Batik: " + e.getMessage(), e);
        }
    }

    /**
     * Sanitizes SVG content by removing DOCTYPE declarations and external entity references
     * to prevent XXE and SSRF attacks.
     */
    String sanitizeSvg(String svgContent) {
        // Remove DOCTYPE declarations including internal subsets (may contain > inside [...])
        String sanitized = svgContent.replaceAll("(?si)<!DOCTYPE[^\\[>]*(\\[[^\\]]*\\])?>", "");
        // Remove parameter entity references (%name;)
        sanitized = sanitized.replaceAll("%[a-zA-Z0-9]+;", "");
        // Remove custom entity references (preserve standard: amp, lt, gt, quot, apos, numeric)
        sanitized = sanitized.replaceAll("&(?!amp;|lt;|gt;|quot;|apos;|#)[a-zA-Z0-9]+;", "");
        return sanitized;
    }

    /**
     * Creates a PDF document with the rendered image embedded on a single page.
     * The page is sized to match the image dimensions at the configured DPI.
     */
    void createPdfFromImage(byte[] imageData, File pdfFile) throws IOException {
        try (var document = new PDDocument()) {
            var image = PDImageXObject.createFromByteArray(document, imageData, "svg-render");

            // Convert pixel dimensions to points at render DPI
            float pageWidth = image.getWidth() * POINTS_PER_INCH / RENDER_DPI;
            float pageHeight = image.getHeight() * POINTS_PER_INCH / RENDER_DPI;

            // Ensure minimum page size (A4 minimum dimensions)
            pageWidth = Math.max(pageWidth, PDRectangle.A4.getWidth());
            pageHeight = Math.max(pageHeight, PDRectangle.A4.getHeight());

            var page = new PDPage(new PDRectangle(pageWidth, pageHeight));
            document.addPage(page);

            try (var contentStream = new PDPageContentStream(document, page)) {
                // Center the image on the page
                float imageWidthPt = image.getWidth() * POINTS_PER_INCH / RENDER_DPI;
                float imageHeightPt = image.getHeight() * POINTS_PER_INCH / RENDER_DPI;
                float x = (pageWidth - imageWidthPt) / 2;
                float y = (pageHeight - imageHeightPt) / 2;

                contentStream.drawImage(image, x, y, imageWidthPt, imageHeightPt);
            }

            document.save(pdfFile);
        }
    }

    /**
     * Strips SVG animation elements (animate, animateTransform, animateMotion, set)
     * to produce a static first-frame representation.
     */
    String stripAnimations(String svgContent) {
        // Remove animation elements for static rendering
        return svgContent
                .replaceAll("<animate[^>]*/>", "")
                .replaceAll("<animate[^>]*>.*?</animate>", "")
                .replaceAll("<animateTransform[^>]*/>", "")
                .replaceAll("<animateTransform[^>]*>.*?</animateTransform>", "")
                .replaceAll("<animateMotion[^>]*/>", "")
                .replaceAll("<animateMotion[^>]*>.*?</animateMotion>", "")
                .replaceAll("<set[^>]*/>", "")
                .replaceAll("<set[^>]*>.*?</set>", "");
    }
}
