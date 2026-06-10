package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.exceptions.FileConversionException;
import com.xtopdf.xtopdf.services.conversion.threed.StlToPdfService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

class StlToPdfServiceTest {

    private StlToPdfService stlToPdfService;
    private com.xtopdf.xtopdf.pdf.PdfBackendProvider pdfBackend;

    private static final long DEFAULT_MAX_FILE_SIZE = 52_428_800L; // 50MB

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        pdfBackend = new com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend();
        stlToPdfService = new StlToPdfService(pdfBackend);
        setMaxFileSize(DEFAULT_MAX_FILE_SIZE);
    }

    private void setMaxFileSize(long value) throws Exception {
        Field field = StlToPdfService.class.getDeclaredField("maxFileSize");
        field.setAccessible(true);
        field.set(stlToPdfService, value);
    }

    @Test
    void testConvertStlToPdf_AsciiFormat() throws Exception {
        String content = "solid test\nfacet normal 0 0 1\nouter loop\nvertex 0 0 0\nvertex 1 0 0\nvertex 0 1 0\nendloop\nendfacet\nendsolid";

        MockMultipartFile stlFile = new MockMultipartFile(
                "file", "test.stl", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());

        File pdfFile = tempDir.resolve("testStlOutput.pdf").toFile();

        stlToPdfService.convertStlToPdf(stlFile, pdfFile);

        assertThat(pdfFile).exists();
        assertThat(pdfFile.length()).isGreaterThan(0);
    }

    @Test
    void testConvertStlToPdf_EmptyFile() throws Exception {
        MockMultipartFile stlFile = new MockMultipartFile(
                "file", "test.stl", MediaType.APPLICATION_OCTET_STREAM_VALUE, "".getBytes());

        File pdfFile = tempDir.resolve("testEmptyStlOutput.pdf").toFile();

        // Empty file should still create a PDF (with 0 triangles)
        stlToPdfService.convertStlToPdf(stlFile, pdfFile);

        assertThat(pdfFile).exists();
    }

    // --- File Size Limit Tests (Requirements 7.1, 7.3, 7.4, 7.5) ---

    @Test
    void testFileExactlyAtLimit_shouldNotThrowSizeException() throws Exception {
        setMaxFileSize(1024L);

        MockMultipartFile stlFile = new MockMultipartFile(
                "file", "model.stl", MediaType.APPLICATION_OCTET_STREAM_VALUE, new byte[0]) {
            @Override
            public long getSize() {
                return 1024L;
            }
        };

        File pdfFile = tempDir.resolve("atLimit.pdf").toFile();

        assertThatNoException()
                .as("File exactly at the size limit should not be rejected")
                .isThrownBy(() -> stlToPdfService.convertStlToPdf(stlFile, pdfFile));
    }

    @Test
    void testFileOneByteOverLimit_shouldThrowFileConversionException() throws Exception {
        setMaxFileSize(1024L);

        MockMultipartFile stlFile = new MockMultipartFile(
                "file", "model.stl", MediaType.APPLICATION_OCTET_STREAM_VALUE, new byte[0]) {
            @Override
            public long getSize() {
                return 1025L;
            }
        };

        File pdfFile = tempDir.resolve("overLimit.pdf").toFile();

        assertThatThrownBy(() -> stlToPdfService.convertStlToPdf(stlFile, pdfFile))
                .as("File 1 byte over the limit should be rejected with FileConversionException")
                .isInstanceOf(FileConversionException.class);
    }

    @Test
    void testErrorMessageContainsSizeLimitInfo() throws Exception {
        setMaxFileSize(1024L);

        MockMultipartFile stlFile = new MockMultipartFile(
                "file", "model.stl", MediaType.APPLICATION_OCTET_STREAM_VALUE, new byte[0]) {
            @Override
            public long getSize() {
                return 2048L;
            }
        };

        File pdfFile = tempDir.resolve("errorMsg.pdf").toFile();

        assertThatThrownBy(() -> stlToPdfService.convertStlToPdf(stlFile, pdfFile))
                .isInstanceOf(FileConversionException.class)
                .hasMessageContaining("1024")
                .as("Error message should contain the size limit value");
    }

    @Test
    void testNormalConversionProceedsWhenWithinLimit() throws Exception {
        setMaxFileSize(DEFAULT_MAX_FILE_SIZE);

        String content = "solid test\nfacet normal 0 0 1\nouter loop\nvertex 0 0 0\nvertex 1 0 0\nvertex 0 1 0\nendloop\nendfacet\nendsolid";

        MockMultipartFile stlFile = new MockMultipartFile(
                "file", "small.stl", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());

        File pdfFile = tempDir.resolve("withinLimit.pdf").toFile();

        stlToPdfService.convertStlToPdf(stlFile, pdfFile);

        assertThat(pdfFile).exists();
        assertThat(pdfFile.length()).isGreaterThan(0);
    }
}
