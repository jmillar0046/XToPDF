package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.*;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class JsonToPdfServiceTest {

    private JsonToPdfService jsonToPdfService;

    @BeforeEach
    void setUp() {
        // Use PDFBox backend for testing
        jsonToPdfService = new JsonToPdfService(new PdfBoxBackend());
    }

    @Test
    void testConvertJsonToPdf_SimpleJson_Success(@TempDir Path tempDir) throws Exception {
        String content = "{\"name\":\"John\",\"age\":30}";
        MockMultipartFile jsonFile = new MockMultipartFile(
                "file", 
                "test.json", 
                "application/json", 
                content.getBytes()
        );

        File pdfFile = tempDir.resolve("testJsonOutput.pdf").toFile();

        jsonToPdfService.convertJsonToPdf(jsonFile, pdfFile);

        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        assertTrue(pdfFile.length() > 0, "The PDF file should not be empty.");
    }

    @Test
    void testConvertJsonToPdf_NestedJson_Success(@TempDir Path tempDir) throws Exception {
        String content = "{\"person\":{\"name\":\"Alice\",\"age\":25,\"address\":{\"city\":\"NYC\",\"zip\":\"10001\"}}}";
        MockMultipartFile jsonFile = new MockMultipartFile(
                "file", 
                "nested.json", 
                "application/json", 
                content.getBytes()
        );

        File pdfFile = tempDir.resolve("nested_output.pdf").toFile();

        jsonToPdfService.convertJsonToPdf(jsonFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertJsonToPdf_JsonArray_Success(@TempDir Path tempDir) throws Exception {
        String content = "[{\"id\":1,\"name\":\"Item1\"},{\"id\":2,\"name\":\"Item2\"}]";
        MockMultipartFile jsonFile = new MockMultipartFile(
                "file", 
                "array.json", 
                "application/json", 
                content.getBytes()
        );

        File pdfFile = tempDir.resolve("array_output.pdf").toFile();

        jsonToPdfService.convertJsonToPdf(jsonFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertJsonToPdf_EmptyJson_Success(@TempDir Path tempDir) throws Exception {
        String content = "{}";
        MockMultipartFile jsonFile = new MockMultipartFile(
                "file", 
                "empty.json", 
                "application/json", 
                content.getBytes()
        );

        File pdfFile = tempDir.resolve("empty_output.pdf").toFile();

        jsonToPdfService.convertJsonToPdf(jsonFile, pdfFile);

        assertTrue(pdfFile.exists());
    }

    @Test
    void testConvertJsonToPdf_EmptyFile_Success(@TempDir Path tempDir) throws Exception {
        String content = "";
        MockMultipartFile jsonFile = new MockMultipartFile(
                "file", 
                "test.json", 
                "application/json", 
                content.getBytes()
        );

        File pdfFile = tempDir.resolve("testJsonEmptyOutput.pdf").toFile();

        jsonToPdfService.convertJsonToPdf(jsonFile, pdfFile);

        assertTrue(pdfFile.exists(), "The PDF file should be created.");
    }

    @Test
    void testConvertJsonToPdf_LargeJson_Success(@TempDir Path tempDir) throws Exception {
        StringBuilder largeJson = new StringBuilder("{\"items\":[");
        for (int i = 0; i < 100; i++) {
            if (i > 0) largeJson.append(",");
            largeJson.append("{\"id\":").append(i).append(",\"name\":\"Item").append(i).append("\"}");
        }
        largeJson.append("]}");

        MockMultipartFile jsonFile = new MockMultipartFile(
                "file", 
                "large.json", 
                "application/json", 
                largeJson.toString().getBytes()
        );

        File pdfFile = tempDir.resolve("large_output.pdf").toFile();

        jsonToPdfService.convertJsonToPdf(jsonFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertJsonToPdf_SpecialCharacters_Success(@TempDir Path tempDir) throws Exception {
        String content = "{\"message\":\"Hello, 世界! Ñoño @#$%\"}";
        MockMultipartFile jsonFile = new MockMultipartFile(
                "file", 
                "special.json", 
                "application/json", 
                content.getBytes()
        );

        File pdfFile = tempDir.resolve("special_output.pdf").toFile();

        jsonToPdfService.convertJsonToPdf(jsonFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertJsonToPdf_NullMultipartFile_ThrowsNullPointerException(@TempDir Path tempDir) {
        File pdfFile = tempDir.resolve("nullInput.pdf").toFile();
        assertThrows(NullPointerException.class, 
            () -> jsonToPdfService.convertJsonToPdf(null, pdfFile));
    }

    @Test
    void testConvertJsonToPdf_NullOutputFile_ThrowsIOException() {
        String content = "test";
        MockMultipartFile jsonFile = new MockMultipartFile(
                "file", 
                "test.json", 
                "application/json", 
                content.getBytes()
        );
        assertThrows(IOException.class, 
            () -> jsonToPdfService.convertJsonToPdf(jsonFile, null));
    }

    @Test
    void testConvertJsonToPdf_BooleanValues_Success(@TempDir Path tempDir) throws Exception {
        String content = "{\"active\":true,\"deleted\":false,\"verified\":null}";
        MockMultipartFile jsonFile = new MockMultipartFile(
                "file", 
                "boolean.json", 
                "application/json", 
                content.getBytes()
        );

        File pdfFile = tempDir.resolve("boolean_output.pdf").toFile();

        jsonToPdfService.convertJsonToPdf(jsonFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertJsonToPdf_NumericValues_Success(@TempDir Path tempDir) throws Exception {
        String content = "{\"integer\":42,\"float\":3.14,\"negative\":-100,\"zero\":0}";
        MockMultipartFile jsonFile = new MockMultipartFile(
                "file", 
                "numeric.json", 
                "application/json", 
                content.getBytes()
        );

        File pdfFile = tempDir.resolve("numeric_output.pdf").toFile();

        jsonToPdfService.convertJsonToPdf(jsonFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }
}
