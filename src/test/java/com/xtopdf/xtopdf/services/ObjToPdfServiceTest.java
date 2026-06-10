package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.exceptions.FileConversionException;
import com.xtopdf.xtopdf.services.conversion.threed.ObjToPdfService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

class ObjToPdfServiceTest {

    private ObjToPdfService objToPdfService;
    private com.xtopdf.xtopdf.pdf.PdfBackendProvider pdfBackend;

    private static final long DEFAULT_MAX_FILE_SIZE = 52_428_800L; // 50MB

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        pdfBackend = new com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend();
        objToPdfService = new ObjToPdfService(pdfBackend);
        setMaxFileSize(DEFAULT_MAX_FILE_SIZE);
    }

    private void setMaxFileSize(long value) throws Exception {
        Field field = ObjToPdfService.class.getDeclaredField("maxFileSize");
        field.setAccessible(true);
        field.set(objToPdfService, value);
    }

    @Test
    void testConvertObjToPdf_Success() throws Exception {
        String content = "# Test OBJ\nv 0 0 0\nv 1 0 0\nv 0 1 0\nf 1 2 3\n";

        MockMultipartFile objFile = new MockMultipartFile(
                "file", "test.obj", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());

        File pdfFile = tempDir.resolve("testObjOutput.pdf").toFile();

        objToPdfService.convertObjToPdf(objFile, pdfFile);

        assertThat(pdfFile).exists();
        assertThat(pdfFile.length()).isGreaterThan(0);
    }

    @Test
    void testConvertObjToPdf_WithGroups() throws Exception {
        String content = "v 0 0 0\ng group1\nv 1 0 0\nf 1 2 3\n";

        MockMultipartFile objFile = new MockMultipartFile(
                "file", "test.obj", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());

        File pdfFile = tempDir.resolve("testObjGroupsOutput.pdf").toFile();

        objToPdfService.convertObjToPdf(objFile, pdfFile);

        assertThat(pdfFile).exists();
    }

    // --- File Size Limit Tests (Requirements 7.2, 7.3, 7.4, 7.5) ---

    @Test
    void testFileExactlyAtLimit_shouldNotThrowSizeException() throws Exception {
        setMaxFileSize(1024L);

        MockMultipartFile objFile = new MockMultipartFile(
                "file", "model.obj", MediaType.APPLICATION_OCTET_STREAM_VALUE, new byte[0]) {
            @Override
            public long getSize() {
                return 1024L;
            }
        };

        File pdfFile = tempDir.resolve("atLimit.pdf").toFile();

        assertThatNoException()
                .as("File exactly at the size limit should not be rejected")
                .isThrownBy(() -> objToPdfService.convertObjToPdf(objFile, pdfFile));
    }

    @Test
    void testFileOneByteOverLimit_shouldThrowFileConversionException() throws Exception {
        setMaxFileSize(1024L);

        MockMultipartFile objFile = new MockMultipartFile(
                "file", "model.obj", MediaType.APPLICATION_OCTET_STREAM_VALUE, new byte[0]) {
            @Override
            public long getSize() {
                return 1025L;
            }
        };

        File pdfFile = tempDir.resolve("overLimit.pdf").toFile();

        assertThatThrownBy(() -> objToPdfService.convertObjToPdf(objFile, pdfFile))
                .as("File 1 byte over the limit should be rejected with FileConversionException")
                .isInstanceOf(FileConversionException.class);
    }

    @Test
    void testErrorMessageContainsSizeLimitInfo() throws Exception {
        setMaxFileSize(1024L);

        MockMultipartFile objFile = new MockMultipartFile(
                "file", "model.obj", MediaType.APPLICATION_OCTET_STREAM_VALUE, new byte[0]) {
            @Override
            public long getSize() {
                return 2048L;
            }
        };

        File pdfFile = tempDir.resolve("errorMsg.pdf").toFile();

        assertThatThrownBy(() -> objToPdfService.convertObjToPdf(objFile, pdfFile))
                .isInstanceOf(FileConversionException.class)
                .hasMessageContaining("1024")
                .as("Error message should contain the size limit value");
    }

    @Test
    void testConfigurableMaxSize_customLimit() throws Exception {
        setMaxFileSize(500L);

        MockMultipartFile objFile = new MockMultipartFile(
                "file", "model.obj", MediaType.APPLICATION_OCTET_STREAM_VALUE, new byte[0]) {
            @Override
            public long getSize() {
                return 501L;
            }
        };

        File pdfFile = tempDir.resolve("customLimit.pdf").toFile();

        assertThatThrownBy(() -> objToPdfService.convertObjToPdf(objFile, pdfFile))
                .isInstanceOf(FileConversionException.class)
                .hasMessageContaining("500");
    }
}
