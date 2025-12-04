package com.xtopdf.xtopdf.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileConversionExceptionTest {

    @Test
    void testFileConversionExceptionDefault() {
        var expectedMessage = "File conversion failed";
        Exception e = assertThrows(FileConversionException.class, () -> {
           throw new FileConversionException();
        });

        assertEquals(expectedMessage, e.getMessage());
    }

    @Test
    void testFileConversionExceptionCustomMessage() {
        var expectedMessage = "File conversion failed dang it";
        Exception e = assertThrows(FileConversionException.class, () -> {
            throw new FileConversionException(expectedMessage);
        });

        assertEquals(expectedMessage, e.getMessage());
    }

    @Test
    void testFileConversionExceptionWithCause() {
        var expectedMessage = "Container execution failed";
        var cause = new RuntimeException("Docker error");
        
        Exception e = assertThrows(FileConversionException.class, () -> {
            throw new FileConversionException(expectedMessage, cause);
        });

        assertEquals(expectedMessage, e.getMessage());
        assertNotNull(e.getCause());
        assertEquals(cause, e.getCause());
        assertEquals("Docker error", e.getCause().getMessage());
    }

    @Test
    void testFileConversionExceptionCauseChain() {
        var rootCause = new IllegalArgumentException("Invalid parameter");
        var intermediateCause = new RuntimeException("Processing error", rootCause);
        var topException = new FileConversionException("Conversion failed", intermediateCause);

        assertEquals("Conversion failed", topException.getMessage());
        assertEquals(intermediateCause, topException.getCause());
        assertEquals(rootCause, topException.getCause().getCause());
    }
}