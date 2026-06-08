package com.xtopdf.xtopdf.adapters.container;

import com.xtopdf.xtopdf.exceptions.FileConversionException;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Size;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Property-based tests for container response PDF validation.
 *
 * Tests a static validation utility method that will be added in tasks 7.2/7.3.
 * The method validates that container conversion output begins with PDF magic bytes.
 *
 * Validates: Requirements 6.3
 */
@Tag("Feature: codebase-hardening, Property 6: Container response PDF validation")
class ContainerPdfValidationPropertyTest {

    private static final byte[] PDF_MAGIC = {0x25, 0x50, 0x44, 0x46}; // %PDF

    /**
     * Property 6: Container response PDF validation — invalid PDF bytes
     *
     * For any byte array NOT beginning with %PDF magic bytes (0x25, 0x50, 0x44, 0x46),
     * the adapter SHALL throw FileConversionException indicating invalid conversion output.
     *
     * Validates: Requirements 6.3
     */
    @Property(tries = 25)
    @Label("Non-PDF byte arrays throw FileConversionException")
    void nonPdfByteArraysThrowFileConversionException(
            @ForAll("nonPdfByteArrays") byte[] responseBytes) {

        assertThatThrownBy(() -> ContainerPdfValidator.validatePdfResponse(responseBytes))
                .isInstanceOf(FileConversionException.class)
                .hasMessageContaining("not a valid PDF");
    }

    /**
     * Property 6: Container response PDF validation — valid PDF bytes
     *
     * For any byte array that DOES begin with %PDF magic bytes,
     * validation should pass without throwing an exception.
     *
     * Validates: Requirements 6.3
     */
    @Property(tries = 25)
    @Label("Valid PDF byte arrays pass validation without exception")
    void validPdfByteArraysPassValidation(
            @ForAll("validPdfByteArrays") byte[] responseBytes) {

        assertThatNoException()
                .as("Byte array starting with %%PDF magic should pass validation")
                .isThrownBy(() -> ContainerPdfValidator.validatePdfResponse(responseBytes));
    }

    /**
     * Property 6: Container response PDF validation — null input
     *
     * Null input SHALL throw FileConversionException indicating empty or invalid response.
     *
     * Validates: Requirements 6.3
     */
    @Property(tries = 25)
    @Label("Null input throws FileConversionException")
    void nullInputThrowsFileConversionException() {

        assertThatThrownBy(() -> ContainerPdfValidator.validatePdfResponse(null))
                .isInstanceOf(FileConversionException.class)
                .hasMessageContaining("empty or invalid response");
    }

    /**
     * Property 6: Container response PDF validation — empty array
     *
     * Empty byte array SHALL throw FileConversionException indicating empty or invalid response.
     *
     * Validates: Requirements 6.3
     */
    @Property(tries = 25)
    @Label("Empty array throws FileConversionException")
    void emptyArrayThrowsFileConversionException() {

        assertThatThrownBy(() -> ContainerPdfValidator.validatePdfResponse(new byte[0]))
                .isInstanceOf(FileConversionException.class)
                .hasMessageContaining("empty or invalid response");
    }

    /**
     * Property 6: Container response PDF validation — arrays shorter than 4 bytes
     *
     * Any byte array shorter than 4 bytes SHALL throw FileConversionException
     * indicating empty or invalid response.
     *
     * Validates: Requirements 6.3
     */
    @Property(tries = 25)
    @Label("Arrays shorter than 4 bytes throw FileConversionException")
    void shortArraysThrowFileConversionException(
            @ForAll("shortByteArrays") byte[] responseBytes) {

        assertThatThrownBy(() -> ContainerPdfValidator.validatePdfResponse(responseBytes))
                .isInstanceOf(FileConversionException.class)
                .hasMessageContaining("empty or invalid response");
    }

    // ---- Arbitraries ----

    @Provide
    Arbitrary<byte[]> nonPdfByteArrays() {
        return Arbitraries.bytes().array(byte[].class)
                .ofMinSize(4)
                .ofMaxSize(100)
                .filter(bytes -> bytes[0] != 0x25 || bytes[1] != 0x50
                        || bytes[2] != 0x44 || bytes[3] != 0x46);
    }

    @Provide
    Arbitrary<byte[]> validPdfByteArrays() {
        // Generate random byte arrays that start with the PDF magic bytes
        return Arbitraries.bytes().array(byte[].class)
                .ofMinSize(0)
                .ofMaxSize(100)
                .map(trailing -> {
                    byte[] result = new byte[PDF_MAGIC.length + trailing.length];
                    System.arraycopy(PDF_MAGIC, 0, result, 0, PDF_MAGIC.length);
                    System.arraycopy(trailing, 0, result, PDF_MAGIC.length, trailing.length);
                    return result;
                });
    }

    @Provide
    Arbitrary<byte[]> shortByteArrays() {
        return Arbitraries.integers().between(1, 3)
                .flatMap(size -> Arbitraries.bytes().array(byte[].class)
                        .ofSize(size));
    }
}
