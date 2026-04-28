package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend;
import com.xtopdf.xtopdf.services.conversion.document.DocxToPdfService;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Size;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for DocxToPdfService.
 *
 * Validates: Requirements 1.1, 1.2, 1.3, 1.4, 2.6, 5.1, 5.2, 5.3
 */
class DocxToPdfServicePropertyTest {

    private final PdfBackendProvider pdfBackend = new PdfBoxBackend();

    // ---------------------------------------------------------------
    // Property 1: Body element order preservation
    // ---------------------------------------------------------------

    /**
     * Property 1: Body element order preservation
     *
     * For any sequence of body elements (paragraphs and tables in arbitrary order),
     * the DocxToPdfService SHALL render them in the exact order returned by
     * getBodyElements(), such that the text content of the N-th element in the
     * input appears before the text content of the (N+1)-th element in the output.
     *
     * **Validates: Requirements 1.1, 1.2, 1.3, 1.4**
     */
    @Property(tries = 15)
    @Label("Body element order is preserved in PDF output")
    void bodyElementOrderIsPreserved(
            @ForAll("interleavedBodyElements") List<BodyElement> elements) throws IOException {

        if (elements.isEmpty()) {
            return;
        }

        Path tempDir = Files.createTempDirectory("docx-order-test");
        try {
            // Build a DOCX with the given interleaved elements
            XWPFDocument document = new XWPFDocument();
            for (BodyElement elem : elements) {
                if (elem.type() == BodyElementType.PARAGRAPH) {
                    XWPFParagraph p = document.createParagraph();
                    XWPFRun run = p.createRun();
                    run.setText(elem.text());
                } else {
                    // TABLE — single-row, single-cell table with the text
                    var table = document.createTable(1, 1);
                    table.getRow(0).getCell(0).setText(elem.text());
                }
            }

            byte[] docxBytes = toBytes(document);
            MockMultipartFile docxFile = new MockMultipartFile(
                    "file", "order.docx", "application/octet-stream", docxBytes);
            File pdfFile = tempDir.resolve("order.pdf").toFile();

            DocxToPdfService service = new DocxToPdfService(pdfBackend);
            service.convertDocxToPdf(docxFile, pdfFile);

            // Extract text from the generated PDF
            String pdfText;
            try (PDDocument pdf = Loader.loadPDF(pdfFile)) {
                PDFTextStripper stripper = new PDFTextStripper();
                pdfText = stripper.getText(pdf);
            }

            // Verify ordering: for each consecutive pair, the first element's text
            // must appear before the second element's text in the PDF output
            for (int i = 0; i < elements.size() - 1; i++) {
                String current = elements.get(i).text();
                String next = elements.get(i + 1).text();
                int posA = pdfText.indexOf(current);
                int posB = pdfText.indexOf(next);
                assertThat(posA)
                        .as("Element '%s' should appear in PDF", current)
                        .isGreaterThanOrEqualTo(0);
                assertThat(posB)
                        .as("Element '%s' should appear in PDF", next)
                        .isGreaterThanOrEqualTo(0);
                assertThat(posA)
                        .as("'%s' (pos %d) should appear before '%s' (pos %d)", current, posA, next, posB)
                        .isLessThan(posB);
            }
        } finally {
            cleanupTempDir(tempDir);
        }
    }

    // ---------------------------------------------------------------
    // Property 4: Per-run formatting extraction
    // ---------------------------------------------------------------

    /**
     * Property 4: Per-run formatting extraction
     *
     * For any paragraph with multiple runs of different formatting, verify the
     * PDF is produced successfully (the service correctly processes each run).
     *
     * **Validates: Requirements 2.6**
     */
    @Property(tries = 15)
    @Label("Per-run formatting extraction produces valid PDF")
    void perRunFormattingExtractionProducesValidPdf(
            @ForAll("multiRunParagraphs") List<RunSpec> runs) throws IOException {

        if (runs.isEmpty()) {
            return;
        }

        Path tempDir = Files.createTempDirectory("docx-runs-test");
        try {
            XWPFDocument document = new XWPFDocument();
            XWPFParagraph paragraph = document.createParagraph();
            for (RunSpec spec : runs) {
                XWPFRun run = paragraph.createRun();
                run.setText(spec.text());
                run.setBold(spec.bold());
                run.setItalic(spec.italic());
                if (spec.fontSize() > 0) {
                    run.setFontSize(spec.fontSize());
                }
            }

            byte[] docxBytes = toBytes(document);
            MockMultipartFile docxFile = new MockMultipartFile(
                    "file", "runs.docx", "application/octet-stream", docxBytes);
            File pdfFile = tempDir.resolve("runs.pdf").toFile();

            DocxToPdfService service = new DocxToPdfService(pdfBackend);
            service.convertDocxToPdf(docxFile, pdfFile);

            // Verify PDF was created and is valid
            assertThat(pdfFile).exists();
            assertThat(pdfFile.length()).isGreaterThan(0);

            try (PDDocument pdf = Loader.loadPDF(pdfFile)) {
                assertThat(pdf.getNumberOfPages()).isGreaterThan(0);

                // Verify all run texts appear in the PDF
                PDFTextStripper stripper = new PDFTextStripper();
                String pdfText = stripper.getText(pdf);
                for (RunSpec spec : runs) {
                    assertThat(pdfText).contains(spec.text());
                }
            }
        } finally {
            cleanupTempDir(tempDir);
        }
    }

    // ---------------------------------------------------------------
    // Property 6: Bullet list prefix
    // ---------------------------------------------------------------

    /**
     * Property 6: Bullet list prefix
     *
     * For any paragraph identified as a bullet list item, the output text
     * should start with "• " followed by the original text content.
     *
     * Note: Since programmatically creating bullet lists in XWPFDocument is
     * complex (requires numbering XML), we test the service's list detection
     * logic by verifying that paragraphs with bullet numbering format produce
     * output containing the bullet character.
     *
     * **Validates: Requirements 5.1**
     */
    @Property(tries = 10)
    @Label("Bullet list items are prefixed with bullet character")
    void bulletListItemsArePrefixed(
            @ForAll("bulletListTexts") List<String> texts) throws IOException {

        if (texts.isEmpty()) {
            return;
        }

        Path tempDir = Files.createTempDirectory("docx-bullet-test");
        try {
            // Create a DOCX with bullet list paragraphs
            // We use the numbering XML approach to create proper bullet lists
            XWPFDocument document = createDocxWithBulletList(texts);

            byte[] docxBytes = toBytes(document);
            MockMultipartFile docxFile = new MockMultipartFile(
                    "file", "bullets.docx", "application/octet-stream", docxBytes);
            File pdfFile = tempDir.resolve("bullets.pdf").toFile();

            DocxToPdfService service = new DocxToPdfService(pdfBackend);
            service.convertDocxToPdf(docxFile, pdfFile);

            assertThat(pdfFile).exists();
            assertThat(pdfFile.length()).isGreaterThan(0);

            try (PDDocument pdf = Loader.loadPDF(pdfFile)) {
                PDFTextStripper stripper = new PDFTextStripper();
                String pdfText = stripper.getText(pdf);

                // Each bullet item text should appear in the PDF
                for (String text : texts) {
                    assertThat(pdfText).contains(text);
                }

                // The bullet character should appear in the PDF
                // Count bullet occurrences — should be at least as many as list items
                long bulletCount = pdfText.chars().filter(c -> c == '•').count();
                assertThat(bulletCount)
                        .as("PDF should contain at least %d bullet characters", texts.size())
                        .isGreaterThanOrEqualTo(texts.size());
            }
        } finally {
            cleanupTempDir(tempDir);
        }
    }

    // ---------------------------------------------------------------
    // Property 7: Numbered list sequential numbering
    // ---------------------------------------------------------------

    /**
     * Property 7: Numbered list sequential numbering
     *
     * For N consecutive list paragraphs sharing the same numbering ID with
     * decimal format, the numbers 1 through N are assigned sequentially.
     *
     * **Validates: Requirements 5.2, 5.3**
     */
    @Property(tries = 10)
    @Label("Numbered list items are sequentially numbered")
    void numberedListItemsAreSequential(
            @ForAll("numberedListTexts") List<String> texts) throws IOException {

        if (texts.isEmpty()) {
            return;
        }

        Path tempDir = Files.createTempDirectory("docx-numbered-test");
        try {
            XWPFDocument document = createDocxWithNumberedList(texts);

            byte[] docxBytes = toBytes(document);
            MockMultipartFile docxFile = new MockMultipartFile(
                    "file", "numbered.docx", "application/octet-stream", docxBytes);
            File pdfFile = tempDir.resolve("numbered.pdf").toFile();

            DocxToPdfService service = new DocxToPdfService(pdfBackend);
            service.convertDocxToPdf(docxFile, pdfFile);

            assertThat(pdfFile).exists();
            assertThat(pdfFile.length()).isGreaterThan(0);

            try (PDDocument pdf = Loader.loadPDF(pdfFile)) {
                PDFTextStripper stripper = new PDFTextStripper();
                String pdfText = stripper.getText(pdf);

                // Each numbered item should appear with its sequential number
                for (int i = 0; i < texts.size(); i++) {
                    String expectedPrefix = (i + 1) + ".";
                    assertThat(pdfText)
                            .as("PDF should contain number prefix '%s'", expectedPrefix)
                            .contains(expectedPrefix);
                    assertThat(pdfText).contains(texts.get(i));
                }
            }
        } finally {
            cleanupTempDir(tempDir);
        }
    }

    // ---------------------------------------------------------------
    // Arbitraries / Generators
    // ---------------------------------------------------------------

    enum BodyElementType { PARAGRAPH, TABLE }

    record BodyElement(BodyElementType type, String text) {}

    record RunSpec(String text, boolean bold, boolean italic, int fontSize) {}

    @Provide
    Arbitrary<List<BodyElement>> interleavedBodyElements() {
        // Generate a list of types, then assign unique text to each element
        return Arbitraries.of(BodyElementType.PARAGRAPH, BodyElementType.TABLE)
                .list().ofMinSize(2).ofMaxSize(8)
                .map(types -> {
                    java.util.List<BodyElement> result = new java.util.ArrayList<>();
                    for (int i = 0; i < types.size(); i++) {
                        // Use unique text per element to avoid indexOf ambiguity
                        result.add(new BodyElement(types.get(i), "Elem" + (i + 1) + "x" + (i * 7 + 3)));
                    }
                    return result;
                });
    }

    @Provide
    Arbitrary<List<RunSpec>> multiRunParagraphs() {
        Arbitrary<RunSpec> run = Combinators.combine(
                Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(15),
                Arbitraries.of(true, false),
                Arbitraries.of(true, false),
                Arbitraries.of(0, 10, 14, 18, 24)
        ).as(RunSpec::new);

        return run.list().ofMinSize(1).ofMaxSize(5);
    }

    @Provide
    Arbitrary<List<String>> bulletListTexts() {
        return Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(20)
                .list().ofMinSize(1).ofMaxSize(5);
    }

    @Provide
    Arbitrary<List<String>> numberedListTexts() {
        return Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(20)
                .list().ofMinSize(1).ofMaxSize(5);
    }

    // ---------------------------------------------------------------
    // Helper methods
    // ---------------------------------------------------------------

    private byte[] toBytes(XWPFDocument document) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            document.write(baos);
            return baos.toByteArray();
        }
    }

    /**
     * Creates a DOCX document with bullet list items using numbering XML.
     */
    private XWPFDocument createDocxWithBulletList(List<String> texts) throws IOException {
        XWPFDocument document = new XWPFDocument();

        // Create numbering for bullet list
        var numbering = document.createNumbering();
        var abstractNumId = numbering.addAbstractNum(createBulletAbstractNum());
        var numId = numbering.addNum(abstractNumId);

        for (String text : texts) {
            XWPFParagraph paragraph = document.createParagraph();
            paragraph.setNumID(numId);
            // Set ilvl so getNumFmt() can resolve the numbering format
            paragraph.getCTP().getPPr().getNumPr()
                    .addNewIlvl().setVal(java.math.BigInteger.ZERO);
            XWPFRun run = paragraph.createRun();
            run.setText(text);
        }

        return document;
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
            // Set ilvl so getNumFmt() can resolve the numbering format
            paragraph.getCTP().getPPr().getNumPr()
                    .addNewIlvl().setVal(java.math.BigInteger.ZERO);
            XWPFRun run = paragraph.createRun();
            run.setText(text);
        }

        return document;
    }

    /**
     * Creates an abstract numbering definition for bullet lists.
     */
    private org.apache.poi.xwpf.usermodel.XWPFAbstractNum createBulletAbstractNum() {
        var ctAbstractNum = org.openxmlformats.schemas.wordprocessingml.x2006.main.CTAbstractNum.Factory.newInstance();
        ctAbstractNum.setAbstractNumId(java.math.BigInteger.ZERO);
        var lvl = ctAbstractNum.addNewLvl();
        lvl.setIlvl(java.math.BigInteger.ZERO);
        var numFmt = lvl.addNewNumFmt();
        numFmt.setVal(org.openxmlformats.schemas.wordprocessingml.x2006.main.STNumberFormat.BULLET);
        var lvlText = lvl.addNewLvlText();
        lvlText.setVal("•");
        return new org.apache.poi.xwpf.usermodel.XWPFAbstractNum(ctAbstractNum);
    }

    /**
     * Creates an abstract numbering definition for decimal numbered lists.
     */
    private org.apache.poi.xwpf.usermodel.XWPFAbstractNum createDecimalAbstractNum() {
        var ctAbstractNum = org.openxmlformats.schemas.wordprocessingml.x2006.main.CTAbstractNum.Factory.newInstance();
        ctAbstractNum.setAbstractNumId(java.math.BigInteger.ZERO);
        var lvl = ctAbstractNum.addNewLvl();
        lvl.setIlvl(java.math.BigInteger.ZERO);
        var numFmt = lvl.addNewNumFmt();
        numFmt.setVal(org.openxmlformats.schemas.wordprocessingml.x2006.main.STNumberFormat.DECIMAL);
        var lvlText = lvl.addNewLvlText();
        lvlText.setVal("%1.");
        var start = lvl.addNewStart();
        start.setVal(java.math.BigInteger.ONE);
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
