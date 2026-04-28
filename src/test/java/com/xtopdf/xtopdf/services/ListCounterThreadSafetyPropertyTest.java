package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend;
import com.xtopdf.xtopdf.services.conversion.document.DocxToPdfService;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for thread-safe list counter isolation.
 *
 * **Validates: Requirements 1.1, 1.2, 1.3, 1.4**
 */
class ListCounterThreadSafetyPropertyTest {

    private final PdfBackendProvider pdfBackend = new PdfBoxBackend();

    /**
     * Property 1: Thread-safe list numbering under concurrency
     *
     * For any two DOCX documents each containing a numbered list of N and M items
     * respectively, when converted concurrently on the same DocxToPdfService singleton
     * instance, each resulting PDF SHALL contain correctly sequential numbering
     * (1 through N, and 1 through M) independent of the other conversion, with no
     * cross-contamination of list counter state.
     *
     * **Validates: Requirements 1.1, 1.2, 1.3, 1.4**
     */
    @Property(tries = 20)
    @Label("Thread-safe list numbering under concurrency")
    void threadSafeListNumberingUnderConcurrency(
            @ForAll @IntRange(min = 2, max = 10) int listSize1,
            @ForAll @IntRange(min = 2, max = 10) int listSize2) throws Exception {

        // Single shared service instance (simulates Spring singleton)
        DocxToPdfService service = new DocxToPdfService(pdfBackend);

        // Generate list item texts for each document
        List<String> texts1 = new ArrayList<>();
        for (int i = 0; i < listSize1; i++) {
            texts1.add("DocA_Item" + (i + 1));
        }
        List<String> texts2 = new ArrayList<>();
        for (int i = 0; i < listSize2; i++) {
            texts2.add("DocB_Item" + (i + 1));
        }

        // Create DOCX files
        byte[] docx1Bytes = toBytes(createDocxWithNumberedList(texts1));
        byte[] docx2Bytes = toBytes(createDocxWithNumberedList(texts2));

        Path tempDir = Files.createTempDirectory("thread-safety-test");
        try {
            File pdfFile1 = tempDir.resolve("concurrent1.pdf").toFile();
            File pdfFile2 = tempDir.resolve("concurrent2.pdf").toFile();

            MockMultipartFile docxFile1 = new MockMultipartFile(
                    "file", "list1.docx", "application/octet-stream", docx1Bytes);
            MockMultipartFile docxFile2 = new MockMultipartFile(
                    "file", "list2.docx", "application/octet-stream", docx2Bytes);

            // Run 2 concurrent conversions on the SAME service instance
            ExecutorService executor = Executors.newFixedThreadPool(2);
            try {
                Future<?> future1 = executor.submit(() -> {
                    try {
                        service.convertDocxToPdf(docxFile1, pdfFile1);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                Future<?> future2 = executor.submit(() -> {
                    try {
                        service.convertDocxToPdf(docxFile2, pdfFile2);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

                // Wait for both to complete
                future1.get();
                future2.get();
            } finally {
                executor.shutdown();
            }

            // Extract text from both PDFs
            String pdfText1 = extractPdfText(pdfFile1);
            String pdfText2 = extractPdfText(pdfFile2);

            // Verify PDF 1 has correct sequential numbering 1..listSize1
            List<Integer> numbers1 = extractNumberPrefixes(pdfText1);
            assertThat(numbers1)
                    .as("PDF 1 should have sequential numbering 1 through %d", listSize1)
                    .hasSize(listSize1);
            for (int i = 0; i < listSize1; i++) {
                assertThat(numbers1.get(i))
                        .as("PDF 1 item %d should be numbered %d", i, i + 1)
                        .isEqualTo(i + 1);
            }

            // Verify PDF 2 has correct sequential numbering 1..listSize2
            List<Integer> numbers2 = extractNumberPrefixes(pdfText2);
            assertThat(numbers2)
                    .as("PDF 2 should have sequential numbering 1 through %d", listSize2)
                    .hasSize(listSize2);
            for (int i = 0; i < listSize2; i++) {
                assertThat(numbers2.get(i))
                        .as("PDF 2 item %d should be numbered %d", i, i + 1)
                        .isEqualTo(i + 1);
            }

            // Verify each PDF contains its own text items
            for (String text : texts1) {
                assertThat(pdfText1).contains(text);
            }
            for (String text : texts2) {
                assertThat(pdfText2).contains(text);
            }

        } finally {
            cleanupTempDir(tempDir);
        }
    }

    // ---------------------------------------------------------------
    // Helper methods
    // ---------------------------------------------------------------

    /**
     * Extracts number prefixes (e.g., "1.", "2.", "3.") from PDF text and returns
     * them as a sorted list of integers in the order they appear.
     */
    private List<Integer> extractNumberPrefixes(String pdfText) {
        List<Integer> numbers = new ArrayList<>();
        Pattern pattern = Pattern.compile("(\\d+)\\.");
        Matcher matcher = pattern.matcher(pdfText);
        while (matcher.find()) {
            numbers.add(Integer.parseInt(matcher.group(1)));
        }
        return numbers;
    }

    private String extractPdfText(File pdfFile) throws IOException {
        try (PDDocument pdf = Loader.loadPDF(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(pdf);
        }
    }

    private byte[] toBytes(XWPFDocument document) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            document.write(baos);
            return baos.toByteArray();
        }
    }

    /**
     * Creates a DOCX document with numbered list items using numbering XML.
     */
    private XWPFDocument createDocxWithNumberedList(List<String> texts) throws IOException {
        XWPFDocument document = new XWPFDocument();

        var numbering = document.createNumbering();
        var abstractNumId = numbering.addAbstractNum(createDecimalAbstractNum());
        var numId = numbering.addNum(abstractNumId);

        for (String text : texts) {
            XWPFParagraph paragraph = document.createParagraph();
            paragraph.setNumID(numId);
            paragraph.getCTP().getPPr().getNumPr()
                    .addNewIlvl().setVal(BigInteger.ZERO);
            XWPFRun run = paragraph.createRun();
            run.setText(text);
        }

        return document;
    }

    /**
     * Creates an abstract numbering definition for decimal numbered lists.
     */
    private org.apache.poi.xwpf.usermodel.XWPFAbstractNum createDecimalAbstractNum() {
        var ctAbstractNum = org.openxmlformats.schemas.wordprocessingml.x2006.main.CTAbstractNum.Factory.newInstance();
        ctAbstractNum.setAbstractNumId(BigInteger.ZERO);
        var lvl = ctAbstractNum.addNewLvl();
        lvl.setIlvl(BigInteger.ZERO);
        var numFmt = lvl.addNewNumFmt();
        numFmt.setVal(org.openxmlformats.schemas.wordprocessingml.x2006.main.STNumberFormat.DECIMAL);
        var lvlText = lvl.addNewLvlText();
        lvlText.setVal("%1.");
        var start = lvl.addNewStart();
        start.setVal(BigInteger.ONE);
        return new org.apache.poi.xwpf.usermodel.XWPFAbstractNum(ctAbstractNum);
    }

    private void cleanupTempDir(Path tempDir) {
        try {
            Files.walk(tempDir)
                    .sorted((a, b) -> -a.compareTo(b))
                    .forEach(path -> {
                        try { Files.deleteIfExists(path); } catch (IOException e) { /* ignore */ }
                    });
        } catch (IOException e) {
            // ignore cleanup errors
        }
    }
}
