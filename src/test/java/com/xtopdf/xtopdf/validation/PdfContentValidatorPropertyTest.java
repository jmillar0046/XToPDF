package com.xtopdf.xtopdf.validation;

import net.jqwik.api.*;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for PdfContentValidator.
 *
 * Property 5: PDF magic byte validation — For any byte array of length >= 4,
 * PdfContentValidator.isPdf() returns true if and only if the first 4 bytes
 * equal [0x25, 0x50, 0x44, 0x46] (the ASCII string "%PDF").
 * For byte arrays shorter than 4 bytes or null/empty inputs, it returns false.
 *
 * **Validates: Requirements 12.1, 12.2, 12.3, 12.4**
 */
class PdfContentValidatorPropertyTest {

    private static final byte[] PDF_MAGIC = {0x25, 0x50, 0x44, 0x46}; // %PDF

    /**
     * Random bytes starting with %PDF magic → isPdf returns true.
     */
    @Property(tries = 25)
    @Tag("Feature: codebase-hardening, Property 5: PDF magic byte validation")
    void bytesStartingWithPdfMagicReturnTrue(
            @ForAll("randomBytesWithPdfMagicPrefix") byte[] content) throws IOException {

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/octet-stream", content);

        assertThat(PdfContentValidator.isPdf(file))
                .as("File starting with %%PDF magic bytes should be detected as PDF")
                .isTrue();
    }

    /**
     * Random bytes NOT starting with %PDF magic → isPdf returns false.
     */
    @Property(tries = 25)
    @Tag("Feature: codebase-hardening, Property 5: PDF magic byte validation")
    void bytesNotStartingWithPdfMagicReturnFalse(
            @ForAll("randomBytesWithoutPdfMagicPrefix") byte[] content) throws IOException {

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.bin", "application/octet-stream", content);

        assertThat(PdfContentValidator.isPdf(file))
                .as("File NOT starting with %%PDF magic bytes should not be detected as PDF")
                .isFalse();
    }

    /**
     * File with < 4 bytes → isPdf returns false.
     */
    @Property(tries = 25)
    @Tag("Feature: codebase-hardening, Property 5: PDF magic byte validation")
    void fileShorterThan4BytesReturnsFalse(
            @ForAll("shortByteArrays") byte[] content) throws IOException {

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.bin", "application/octet-stream", content);

        assertThat(PdfContentValidator.isPdf(file))
                .as("File with fewer than 4 bytes should return false")
                .isFalse();
    }

    /**
     * Null file → isPdf returns false.
     */
    @Example
    @Tag("Feature: codebase-hardening, Property 5: PDF magic byte validation")
    void nullFileReturnsFalse() throws IOException {
        assertThat(PdfContentValidator.isPdf(null))
                .as("Null file should return false")
                .isFalse();
    }

    /**
     * Empty file → isPdf returns false.
     */
    @Example
    @Tag("Feature: codebase-hardening, Property 5: PDF magic byte validation")
    void emptyFileReturnsFalse() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.pdf", "application/octet-stream", new byte[0]);

        assertThat(PdfContentValidator.isPdf(file))
                .as("Empty file should return false")
                .isFalse();
    }

    @Provide
    Arbitrary<byte[]> randomBytesWithPdfMagicPrefix() {
        return Arbitraries.bytes().array(byte[].class)
                .ofMinSize(0).ofMaxSize(100)
                .map(randomSuffix -> {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    try {
                        bos.write(PDF_MAGIC);
                        bos.write(randomSuffix);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return bos.toByteArray();
                });
    }

    @Provide
    Arbitrary<byte[]> randomBytesWithoutPdfMagicPrefix() {
        // Generate byte arrays of length >= 4 whose first 4 bytes are NOT %PDF
        return Arbitraries.bytes().array(byte[].class)
                .ofMinSize(4).ofMaxSize(100)
                .filter(bytes -> !(bytes[0] == 0x25
                        && bytes[1] == 0x50
                        && bytes[2] == 0x44
                        && bytes[3] == 0x46));
    }

    @Provide
    Arbitrary<byte[]> shortByteArrays() {
        // Generate byte arrays of length 1, 2, or 3 (never 0 — that's the empty case)
        return Arbitraries.integers().between(1, 3)
                .flatMap(size -> Arbitraries.bytes().array(byte[].class)
                        .ofSize(size));
    }
}
