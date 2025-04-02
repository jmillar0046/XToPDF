package com.xtopdf.xtopdf.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}