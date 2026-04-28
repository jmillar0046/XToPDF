package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend;
import com.xtopdf.xtopdf.services.conversion.document.DocxToPdfService;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based test: Page breaks determine PDF page count.
 *
 * <p><b>Validates: Requirements 4.1, 4.2, 4.3</b></p>
 *
 * <p>Property 5: For any DOCX document containing K explicit page breaks
 * (at either the run or paragraph level) and content that fits within a
 * single page between breaks, the resulting PDF shall contain exactly
 * K + 1 pages.</p>
 */
public class PageBreakCountPropertyTest {

    /**
     * Property 5: Page breaks determine PDF page count.
     *
     * Generates documents with 0-5 page breaks at random positions between
     * text paragraphs. Each document has (numBreaks + 1) text paragraphs
     * with page breaks inserted between them. Verifies the resulting PDF
     * has exactly (numBreaks + 1) pages.
     *
     * <b>Validates: Requirements 4.1, 4.2, 4.3</b>
     */
    @Property(tries = 30)
    void pageBreaksDeterminePdfPageCount(
            @ForAll @IntRange(min = 0, max = 5) int numBreaks
    ) throws IOException {
        DocxToPdfService service = new DocxToPdfService(new PdfBoxBackend());

        // Build a DOCX with numBreaks page breaks between text paragraphs.
        // Structure: text, [break, text] * numBreaks
        // This ensures content is rendered before every page break.
        XWPFDocument document = new XWPFDocument();

        // First text paragraph (always present)
        document.createParagraph().createRun().setText("Page 1 content");

        for (int i = 0; i < numBreaks; i++) {
            // Add a page break via a run-level break
            XWPFRun breakRun = document.createParagraph().createRun();
            breakRun.addBreak(BreakType.PAGE);

            // Add content for the next page
            document.createParagraph().createRun()
                    .setText("Page " + (i + 2) + " content");
        }

        byte[] docxBytes;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            document.write(baos);
            docxBytes = baos.toByteArray();
        }

        var docxFile = new MockMultipartFile("file", "pbt-pagebreak.docx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE, docxBytes);
        var pdfFile = File.createTempFile("pbt-pagebreak-", ".pdf");
        pdfFile.deleteOnExit();

        service.convertDocxToPdf(docxFile, pdfFile);

        try (PDDocument pdf = Loader.loadPDF(pdfFile)) {
            int expectedPages = numBreaks + 1;
            assertThat(pdf.getNumberOfPages())
                    .as("DOCX with %d page break(s) should produce %d page(s)",
                            numBreaks, expectedPages)
                    .isEqualTo(expectedPages);
        }
    }
}
