package com.xtopdf.xtopdf.utils;

import net.jqwik.api.*;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * Property-based tests for PdfFileHelper exception propagation.
 *
 * Property 7: PdfFileHelper exception propagation — for any non-IOException
 * exception thrown during PDF processing, the exception SHALL propagate to
 * the caller without being caught into an HTTP 500 response.
 *
 * Validates: Requirements 11.2
 */
class PdfFileHelperPropertyTest {

    @Property(tries = 25)
    @Tag("Feature: codebase-hardening, Property 7: PdfFileHelper exception propagation")
    void nonIoExceptionsPropagate(@ForAll("runtimeExceptionMessages") String message) {
        // Valid PDF magic bytes so the file is accepted
        byte[] pdfContent = new byte[]{0x25, 0x50, 0x44, 0x46, 0x2D, 0x31, 0x2E, 0x34};
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", pdfContent);

        Throwable thrown = catchThrowable(() ->
                PdfFileHelper.processPdfFile(
                        pdfFile,
                        file -> { throw new RuntimeException(message); },
                        "output.pdf"
                )
        );

        // RuntimeException should propagate — NOT be caught into an HTTP response
        assertThat(thrown)
                .as("RuntimeException should propagate, not be swallowed")
                .isInstanceOf(RuntimeException.class)
                .hasMessage(message);
    }

    @Property(tries = 25)
    @Tag("Feature: codebase-hardening, Property 7: PdfFileHelper exception propagation")
    void ioExceptionsReturnHttpResponse(@ForAll("ioExceptionMessages") String message) {
        byte[] pdfContent = new byte[]{0x25, 0x50, 0x44, 0x46, 0x2D, 0x31, 0x2E, 0x34};
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", pdfContent);

        // IOException should be caught and return an HTTP 500 response (not propagate)
        var response = PdfFileHelper.processPdfFile(
                pdfFile,
                file -> { throw new IOException(message); },
                "output.pdf"
        );

        assertThat(response.getStatusCode().value())
                .as("IOException should be caught and return HTTP 500")
                .isEqualTo(500);
    }

    @Provide
    Arbitrary<String> runtimeExceptionMessages() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(1)
                .ofMaxLength(50)
                .map(s -> "Error: " + s);
    }

    @Provide
    Arbitrary<String> ioExceptionMessages() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(1)
                .ofMaxLength(50)
                .map(s -> "IO failure: " + s);
    }
}
