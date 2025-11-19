package com.xtopdf.xtopdf.services;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.*;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class TxtToPdfServiceTest {

    private TxtToPdfService txtToPdfService;

    @BeforeEach
    void setUp() {
        txtToPdfService = new TxtToPdfService();
    }


    @Test
    void testConvertTxtToPdf_Success(@TempDir Path tempDir) throws Exception {
        String content = "Hello, this is a test file content!";
        MockMultipartFile txtFile = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, content.getBytes());

        File pdfFile = tempDir.resolve("testOutput.pdf").toFile();

        txtToPdfService.convertTxtToPdf(txtFile, pdfFile);

        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        assertTrue(pdfFile.length() > 0, "The PDF file should not be empty.");
    }

    @Test
    void testConvertTxtToPdf_EmptyFile(@TempDir Path tempDir) throws Exception {
        String content = "";
        MockMultipartFile txtFile = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, content.getBytes());

        File pdfFile = tempDir.resolve("testEmptyOutput.pdf").toFile();

        txtToPdfService.convertTxtToPdf(txtFile, pdfFile);

        assertTrue(pdfFile.exists(), "The PDF file should be created.");
    }

    @Test
    void testConvertTxtToPdf_WithUnicodeCharacters(@TempDir Path tempDir) throws Exception {
        String content = "Hello, 你好, привет, 😀";
        MockMultipartFile txtFile = new MockMultipartFile("file", "unicode.txt", MediaType.TEXT_PLAIN_VALUE, content.getBytes("UTF-8"));
        File pdfFile = tempDir.resolve("unicodeOutput.pdf").toFile();
        txtToPdfService.convertTxtToPdf(txtFile, pdfFile);
        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        assertTrue(pdfFile.length() > 0, "The PDF file should not be empty.");
    }

    @Test
    void testConvertTxtToPdf_OnlyWhitespace(@TempDir Path tempDir) throws Exception {
        String content = "     \n   \n";
        MockMultipartFile txtFile = new MockMultipartFile("file", "whitespace.txt", MediaType.TEXT_PLAIN_VALUE, content.getBytes());
        File pdfFile = tempDir.resolve("whitespaceOutput.pdf").toFile();
        txtToPdfService.convertTxtToPdf(txtFile, pdfFile);
        assertTrue(pdfFile.exists(), "The PDF file should be created.");
    }

    @Test
    void testConvertTxtToPdf_MultipleLines(@TempDir Path tempDir) throws Exception {
        String content = "Line 1\nLine 2\nLine 3";
        MockMultipartFile txtFile = new MockMultipartFile("file", "multilines.txt", MediaType.TEXT_PLAIN_VALUE, content.getBytes());
        File pdfFile = tempDir.resolve("multilinesOutput.pdf").toFile();
        txtToPdfService.convertTxtToPdf(txtFile, pdfFile);
        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        assertTrue(pdfFile.length() > 0, "The PDF file should not be empty.");
    }

    @Test
    void testConvertTxtToPdf_LongText(@TempDir Path tempDir) throws Exception {
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            content.append("This is line ").append(i).append(" of the text file.\n");
        }
        MockMultipartFile txtFile = new MockMultipartFile("file", "long.txt", MediaType.TEXT_PLAIN_VALUE, content.toString().getBytes());
        File pdfFile = tempDir.resolve("longOutput.pdf").toFile();
        txtToPdfService.convertTxtToPdf(txtFile, pdfFile);
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertTxtToPdf_WithTabs(@TempDir Path tempDir) throws Exception {
        String content = "Column1\tColumn2\tColumn3\nValue1\tValue2\tValue3";
        MockMultipartFile txtFile = new MockMultipartFile("file", "tabs.txt", MediaType.TEXT_PLAIN_VALUE, content.getBytes());
        File pdfFile = tempDir.resolve("tabsOutput.pdf").toFile();
        txtToPdfService.convertTxtToPdf(txtFile, pdfFile);
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertTxtToPdf_SpecialCharacters(@TempDir Path tempDir) throws Exception {
        String content = "Special chars: @#$%^&*()_+-=[]{}|;:',.<>?/~`";
        MockMultipartFile txtFile = new MockMultipartFile("file", "special.txt", MediaType.TEXT_PLAIN_VALUE, content.getBytes());
        File pdfFile = tempDir.resolve("specialOutput.pdf").toFile();
        txtToPdfService.convertTxtToPdf(txtFile, pdfFile);
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertTxtToPdf_MixedLineEndings(@TempDir Path tempDir) throws Exception {
        String content = "Line 1\nLine 2\rLine 3\r\nLine 4";
        MockMultipartFile txtFile = new MockMultipartFile("file", "mixed.txt", MediaType.TEXT_PLAIN_VALUE, content.getBytes());
        File pdfFile = tempDir.resolve("mixedOutput.pdf").toFile();
        txtToPdfService.convertTxtToPdf(txtFile, pdfFile);
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertTxtToPdf_VeryLongLine(@TempDir Path tempDir) throws Exception {
        StringBuilder longLine = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            longLine.append("word").append(i).append(" ");
        }
        MockMultipartFile txtFile = new MockMultipartFile("file", "longline.txt", MediaType.TEXT_PLAIN_VALUE, longLine.toString().getBytes());
        File pdfFile = tempDir.resolve("longlineOutput.pdf").toFile();
        txtToPdfService.convertTxtToPdf(txtFile, pdfFile);
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertTxtToPdf_NumbersOnly(@TempDir Path tempDir) throws Exception {
        String content = "123456789\n987654321\n111222333";
        MockMultipartFile txtFile = new MockMultipartFile("file", "numbers.txt", MediaType.TEXT_PLAIN_VALUE, content.getBytes());
        File pdfFile = tempDir.resolve("numbersOutput.pdf").toFile();
        txtToPdfService.convertTxtToPdf(txtFile, pdfFile);
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertTxtToPdf_RepeatedCharacters(@TempDir Path tempDir) throws Exception {
        String content = "AAAAAAAAAA\nBBBBBBBBBB\nCCCCCCCCCC";
        MockMultipartFile txtFile = new MockMultipartFile("file", "repeated.txt", MediaType.TEXT_PLAIN_VALUE, content.getBytes());
        File pdfFile = tempDir.resolve("repeatedOutput.pdf").toFile();
        txtToPdfService.convertTxtToPdf(txtFile, pdfFile);
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertTxtToPdf_NullMultipartFile_ThrowsNullPointerException(@TempDir Path tempDir) {
        File pdfFile = tempDir.resolve("nullInput.pdf").toFile();
        assertThrows(NullPointerException.class, () -> txtToPdfService.convertTxtToPdf(null, pdfFile));
    }

    @Test
    void testConvertTxtToPdf_NullOutputFile_ThrowsIOException() {
        String content = "test";
        MockMultipartFile txtFile = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, content.getBytes());
        assertThrows(IOException.class, () -> txtToPdfService.convertTxtToPdf(txtFile, null));
    }
}
