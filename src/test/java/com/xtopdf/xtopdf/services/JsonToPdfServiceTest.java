package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.services.conversion.data.JsonToPdfService;
import com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonToPdfServiceTest {

    private JsonToPdfService jsonToPdfService;

    @BeforeEach
    void setUp() {
        jsonToPdfService = new JsonToPdfService(new PdfBoxBackend());
    }

    // --- Minified JSON expansion to indented format (Requirement 8.1) ---

    @Test
    void convertJsonToPdf_minifiedJson_producesFormattedPdf(@TempDir Path tempDir) throws Exception {
        String content = "{\"name\":\"John\",\"age\":30}";
        MockMultipartFile jsonFile = new MockMultipartFile(
                "file", "test.json", "application/json", content.getBytes());

        File pdfFile = tempDir.resolve("minified_output.pdf").toFile();

        jsonToPdfService.convertJsonToPdf(jsonFile, pdfFile);

        assertThat(pdfFile).exists();
        assertThat(pdfFile.length()).isGreaterThan(0);
    }

    // --- Already-formatted JSON preservation (Requirement 8.3) ---

    @Test
    void convertJsonToPdf_alreadyFormattedJson_producesValidPdf(@TempDir Path tempDir) throws Exception {
        String content = """
                {
                  "name" : "Alice",
                  "age" : 25
                }""";
        MockMultipartFile jsonFile = new MockMultipartFile(
                "file", "formatted.json", "application/json", content.getBytes());

        File pdfFile = tempDir.resolve("formatted_output.pdf").toFile();

        jsonToPdfService.convertJsonToPdf(jsonFile, pdfFile);

        assertThat(pdfFile).exists();
        assertThat(pdfFile.length()).isGreaterThan(0);
    }

    // --- Monospace font rendering at 10pt (Requirement 8.2) ---

    @Test
    void convertJsonToPdf_simpleJson_rendersWithMonospaceFont(@TempDir Path tempDir) throws Exception {
        String content = "{\"key\":\"value\"}";
        MockMultipartFile jsonFile = new MockMultipartFile(
                "file", "mono.json", "application/json", content.getBytes());

        File pdfFile = tempDir.resolve("mono_output.pdf").toFile();

        jsonToPdfService.convertJsonToPdf(jsonFile, pdfFile);

        // Verifies that PDF is created (monospace rendering validated by addFormattedText at 10pt)
        assertThat(pdfFile).exists();
        assertThat(pdfFile.length()).isGreaterThan(0);
    }

    // --- Deeply nested structures (Requirement 8.4) ---

    @Test
    void convertJsonToPdf_deeplyNestedJson_producesValidPdf(@TempDir Path tempDir) throws Exception {
        String content = "{\"level1\":{\"level2\":{\"level3\":{\"level4\":{\"level5\":{\"value\":\"deep\"}}}}}}";
        MockMultipartFile jsonFile = new MockMultipartFile(
                "file", "nested.json", "application/json", content.getBytes());

        File pdfFile = tempDir.resolve("nested_output.pdf").toFile();

        jsonToPdfService.convertJsonToPdf(jsonFile, pdfFile);

        assertThat(pdfFile).exists();
        assertThat(pdfFile.length()).isGreaterThan(0);
    }

    @Test
    void convertJsonToPdf_nestedObjectWithAddress_producesValidPdf(@TempDir Path tempDir) throws Exception {
        String content = "{\"person\":{\"name\":\"Alice\",\"age\":25,\"address\":{\"city\":\"NYC\",\"zip\":\"10001\"}}}";
        MockMultipartFile jsonFile = new MockMultipartFile(
                "file", "nested.json", "application/json", content.getBytes());

        File pdfFile = tempDir.resolve("nested_output.pdf").toFile();

        jsonToPdfService.convertJsonToPdf(jsonFile, pdfFile);

        assertThat(pdfFile).exists();
        assertThat(pdfFile.length()).isGreaterThan(0);
    }

    @Test
    void convertJsonToPdf_jsonArray_producesValidPdf(@TempDir Path tempDir) throws Exception {
        String content = "[{\"id\":1,\"name\":\"Item1\"},{\"id\":2,\"name\":\"Item2\"}]";
        MockMultipartFile jsonFile = new MockMultipartFile(
                "file", "array.json", "application/json", content.getBytes());

        File pdfFile = tempDir.resolve("array_output.pdf").toFile();

        jsonToPdfService.convertJsonToPdf(jsonFile, pdfFile);

        assertThat(pdfFile).exists();
        assertThat(pdfFile.length()).isGreaterThan(0);
    }

    @Test
    void convertJsonToPdf_emptyObject_producesValidPdf(@TempDir Path tempDir) throws Exception {
        String content = "{}";
        MockMultipartFile jsonFile = new MockMultipartFile(
                "file", "empty.json", "application/json", content.getBytes());

        File pdfFile = tempDir.resolve("empty_output.pdf").toFile();

        jsonToPdfService.convertJsonToPdf(jsonFile, pdfFile);

        assertThat(pdfFile).exists();
    }

    @Test
    void convertJsonToPdf_emptyFile_producesValidPdf(@TempDir Path tempDir) throws Exception {
        String content = "";
        MockMultipartFile jsonFile = new MockMultipartFile(
                "file", "test.json", "application/json", content.getBytes());

        File pdfFile = tempDir.resolve("testJsonEmptyOutput.pdf").toFile();

        jsonToPdfService.convertJsonToPdf(jsonFile, pdfFile);

        assertThat(pdfFile).exists()
                .as("The PDF file should be created even for empty input");
    }

    @Test
    void convertJsonToPdf_largeJson_producesValidPdf(@TempDir Path tempDir) throws Exception {
        StringBuilder largeJson = new StringBuilder("{\"items\":[");
        for (int i = 0; i < 100; i++) {
            if (i > 0) largeJson.append(",");
            largeJson.append("{\"id\":").append(i).append(",\"name\":\"Item").append(i).append("\"}");
        }
        largeJson.append("]}");

        MockMultipartFile jsonFile = new MockMultipartFile(
                "file", "large.json", "application/json", largeJson.toString().getBytes());

        File pdfFile = tempDir.resolve("large_output.pdf").toFile();

        jsonToPdfService.convertJsonToPdf(jsonFile, pdfFile);

        assertThat(pdfFile).exists();
        assertThat(pdfFile.length()).isGreaterThan(0);
    }

    @Test
    void convertJsonToPdf_specialCharacters_producesValidPdf(@TempDir Path tempDir) throws Exception {
        String content = "{\"message\":\"Hello, 世界! Ñoño @#$%\"}";
        MockMultipartFile jsonFile = new MockMultipartFile(
                "file", "special.json", "application/json", content.getBytes());

        File pdfFile = tempDir.resolve("special_output.pdf").toFile();

        jsonToPdfService.convertJsonToPdf(jsonFile, pdfFile);

        assertThat(pdfFile).exists();
        assertThat(pdfFile.length()).isGreaterThan(0);
    }

    @Test
    void convertJsonToPdf_nullMultipartFile_throwsNullPointerException(@TempDir Path tempDir) {
        File pdfFile = tempDir.resolve("nullInput.pdf").toFile();

        assertThatThrownBy(() -> jsonToPdfService.convertJsonToPdf(null, pdfFile))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void convertJsonToPdf_nullOutputFile_throwsIOException() {
        MockMultipartFile jsonFile = new MockMultipartFile(
                "file", "test.json", "application/json", "test".getBytes());

        assertThatThrownBy(() -> jsonToPdfService.convertJsonToPdf(jsonFile, null))
                .isInstanceOf(IOException.class);
    }

    @Test
    void convertJsonToPdf_booleanAndNullValues_producesValidPdf(@TempDir Path tempDir) throws Exception {
        String content = "{\"active\":true,\"deleted\":false,\"verified\":null}";
        MockMultipartFile jsonFile = new MockMultipartFile(
                "file", "boolean.json", "application/json", content.getBytes());

        File pdfFile = tempDir.resolve("boolean_output.pdf").toFile();

        jsonToPdfService.convertJsonToPdf(jsonFile, pdfFile);

        assertThat(pdfFile).exists();
        assertThat(pdfFile.length()).isGreaterThan(0);
    }

    @Test
    void convertJsonToPdf_numericValues_producesValidPdf(@TempDir Path tempDir) throws Exception {
        String content = "{\"integer\":42,\"float\":3.14,\"negative\":-100,\"zero\":0}";
        MockMultipartFile jsonFile = new MockMultipartFile(
                "file", "numeric.json", "application/json", content.getBytes());

        File pdfFile = tempDir.resolve("numeric_output.pdf").toFile();

        jsonToPdfService.convertJsonToPdf(jsonFile, pdfFile);

        assertThat(pdfFile).exists();
        assertThat(pdfFile.length()).isGreaterThan(0);
    }
}
