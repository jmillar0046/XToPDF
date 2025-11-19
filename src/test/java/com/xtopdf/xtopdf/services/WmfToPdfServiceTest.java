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

class WmfToPdfServiceTest {

    private WmfToPdfService wmfToPdfService;
    
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        wmfToPdfService = new WmfToPdfService();
    }

    @Test
    void testConvertWmfToPdf_PlaceableWmf() throws Exception {
        // Create a placeable WMF header
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteBuffer buffer = ByteBuffer.allocate(50);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        // Placeable header
        buffer.putInt(0x9AC6CDD7); // Magic number
        buffer.putShort((short) 0); // Handle
        buffer.putShort((short) 0); // Left
        buffer.putShort((short) 0); // Top
        buffer.putShort((short) 100); // Right
        buffer.putShort((short) 100); // Bottom
        buffer.putShort((short) 96); // Inch
        buffer.putInt(0); // Reserved
        buffer.putShort((short) 0); // Checksum
        
        // Standard WMF header
        buffer.putShort((short) 1); // Type
        buffer.putShort((short) 9); // HeaderSize
        buffer.putShort((short) 0x0300); // Version
        buffer.putInt(100); // Size
        buffer.putShort((short) 0); // NumObjects
        buffer.putInt(10); // MaxRecord
        
        baos.write(buffer.array(), 0, buffer.position());
        
        MockMultipartFile wmfFile = new MockMultipartFile(
                "file", "test.wmf", MediaType.APPLICATION_OCTET_STREAM_VALUE, baos.toByteArray());

        File pdfFile = tempDir.resolve("testWmfOutput.pdf").toFile();

        wmfToPdfService.convertWmfToPdf(wmfFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertWmfToPdf_StandardWmf() throws Exception {
        // Create a standard WMF header (no placeable header)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteBuffer buffer = ByteBuffer.allocate(30);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        // Standard WMF header
        buffer.putShort((short) 1); // Type
        buffer.putShort((short) 9); // HeaderSize
        buffer.putShort((short) 0x0300); // Version
        buffer.putInt(100); // Size
        buffer.putShort((short) 0); // NumObjects
        buffer.putInt(10); // MaxRecord
        
        baos.write(buffer.array(), 0, buffer.position());
        
        MockMultipartFile wmfFile = new MockMultipartFile(
                "file", "test.wmf", MediaType.APPLICATION_OCTET_STREAM_VALUE, baos.toByteArray());

        File pdfFile = tempDir.resolve("testWmfStandardOutput.pdf").toFile();

        wmfToPdfService.convertWmfToPdf(wmfFile, pdfFile);

        assertTrue(pdfFile.exists());
    }

    @Test
    void testConvertWmfToPdf_InvalidFile() throws Exception {
        // Too short file
        MockMultipartFile wmfFile = new MockMultipartFile(
                "file", "test.wmf", MediaType.APPLICATION_OCTET_STREAM_VALUE, "short".getBytes());

        File pdfFile = tempDir.resolve("testWmfInvalidOutput.pdf").toFile();

        wmfToPdfService.convertWmfToPdf(wmfFile, pdfFile);

        assertTrue(pdfFile.exists());
    }
}
