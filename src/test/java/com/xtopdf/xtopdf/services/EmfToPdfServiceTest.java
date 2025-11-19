package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class EmfToPdfServiceTest {

    private EmfToPdfService emfToPdfService;
    
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        emfToPdfService = new EmfToPdfService();
    }

    @Test
    void testConvertEmfToPdf_ValidFile() throws Exception {
        // Create a minimal EMF header
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteBuffer buffer = ByteBuffer.allocate(100);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        // EMR_HEADER
        buffer.putInt(1); // iType (EMR_HEADER = 1)
        buffer.putInt(88); // nSize
        buffer.putInt(0); // rclBounds.left
        buffer.putInt(0); // rclBounds.top
        buffer.putInt(100); // rclBounds.right
        buffer.putInt(100); // rclBounds.bottom
        buffer.putInt(0); // rclFrame.left
        buffer.putInt(0); // rclFrame.top
        buffer.putInt(1000); // rclFrame.right
        buffer.putInt(1000); // rclFrame.bottom
        buffer.putInt(0x464D4520); // dSignature
        buffer.putInt(0x00010000); // nVersion
        buffer.putInt(100); // nBytes
        buffer.putInt(5); // nRecords
        
        baos.write(buffer.array(), 0, buffer.position());
        
        MockMultipartFile emfFile = new MockMultipartFile(
                "file", "test.emf", MediaType.APPLICATION_OCTET_STREAM_VALUE, baos.toByteArray());

        File pdfFile = tempDir.resolve("testEmfOutput.pdf").toFile();

        emfToPdfService.convertEmfToPdf(emfFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertEmfToPdf_InvalidFile() throws Exception {
        // Too short file
        MockMultipartFile emfFile = new MockMultipartFile(
                "file", "test.emf", MediaType.APPLICATION_OCTET_STREAM_VALUE, "short".getBytes());

        File pdfFile = tempDir.resolve("testEmfInvalidOutput.pdf").toFile();

        emfToPdfService.convertEmfToPdf(emfFile, pdfFile);

        assertTrue(pdfFile.exists());
    }

    @Test
    void testConvertEmfToPdf_WrongType() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteBuffer buffer = ByteBuffer.allocate(100);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        // Wrong record type
        buffer.putInt(999); // Invalid type
        buffer.putInt(88);
        
        baos.write(buffer.array(), 0, buffer.position());
        
        MockMultipartFile emfFile = new MockMultipartFile(
                "file", "test.emf", MediaType.APPLICATION_OCTET_STREAM_VALUE, baos.toByteArray());

        File pdfFile = tempDir.resolve("testEmfWrongTypeOutput.pdf").toFile();

        emfToPdfService.convertEmfToPdf(emfFile, pdfFile);

        assertTrue(pdfFile.exists());
    }
}
