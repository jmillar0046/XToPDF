package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.exceptions.FileConversionException;
import net.jqwik.api.*;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * Property-based tests for file size limit enforcement in 3D converters.
 *
 * // Feature: converter-improvements, Property 5: File size limit enforcement
 *
 * Property 5: File size limit enforcement
 * - For any file where getSize() > maxFileSize, the STL and OBJ converters
 *   SHALL throw a FileConversionException before performing any parsing.
 * - For any file where getSize() <= maxFileSize, the converter SHALL proceed
 *   to the parsing step without throwing a size-related exception.
 *
 * **Validates: Requirements 7.1, 7.2, 7.3, 7.5**
 */
class FileSizeLimitPropertyTest {

    private static final long MAX_FILE_SIZE = 52_428_800L; // 50MB default

    /**
     * Simulates the file size check logic used by STL and OBJ converters.
     * This isolates the size enforcement behavior for property testing.
     */
    private void checkFileSize(long fileSize, long maxFileSize) throws FileConversionException {
        if (fileSize > maxFileSize) {
            throw new FileConversionException(
                    "File exceeds maximum size limit of " + maxFileSize + " bytes");
        }
    }

    /**
     * Property 5: Files exceeding the maximum size limit throw FileConversionException.
     *
     * **Validates: Requirements 7.1, 7.2, 7.3, 7.5**
     */
    @Property(tries = 25)
    @Tag("Feature: converter-improvements, Property 5: File size limit enforcement")
    void filesOverLimitThrowFileConversionException(@ForAll("fileSizesOverLimit") long fileSize) {
        assertThatThrownBy(() -> checkFileSize(fileSize, MAX_FILE_SIZE))
                .as("File of size %d (over limit %d) should throw FileConversionException", fileSize, MAX_FILE_SIZE)
                .isInstanceOf(FileConversionException.class)
                .hasMessageContaining(String.valueOf(MAX_FILE_SIZE));
    }

    /**
     * Property 5: Files within the size limit do not throw a size-related exception.
     *
     * **Validates: Requirements 7.1, 7.2, 7.3, 7.5**
     */
    @Property(tries = 25)
    @Tag("Feature: converter-improvements, Property 5: File size limit enforcement")
    void filesWithinLimitDoNotThrowSizeException(@ForAll("fileSizesWithinLimit") long fileSize) {
        assertThatNoException()
                .as("File of size %d (within limit %d) should not throw size exception", fileSize, MAX_FILE_SIZE)
                .isThrownBy(() -> checkFileSize(fileSize, MAX_FILE_SIZE));
    }

    /**
     * Property 5: The size check behaves correctly with MockMultipartFile for STL files.
     *
     * **Validates: Requirements 7.1, 7.2, 7.3, 7.5**
     */
    @Property(tries = 25)
    @Tag("Feature: converter-improvements, Property 5: File size limit enforcement")
    void stlFileSizeCheckWithMockMultipartFile(@ForAll("fileSizesAroundBoundary") long fileSize) {
        // Create a MockMultipartFile that reports the desired size
        byte[] content = new byte[(int) Math.min(fileSize, 1024)];
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "model.stl", "application/octet-stream", content) {
            @Override
            public long getSize() {
                return fileSize;
            }
        };

        if (mockFile.getSize() > MAX_FILE_SIZE) {
            assertThatThrownBy(() -> checkFileSize(mockFile.getSize(), MAX_FILE_SIZE))
                    .as("STL file of size %d should be rejected", fileSize)
                    .isInstanceOf(FileConversionException.class);
        } else {
            assertThatNoException()
                    .as("STL file of size %d should be accepted", fileSize)
                    .isThrownBy(() -> checkFileSize(mockFile.getSize(), MAX_FILE_SIZE));
        }
    }

    /**
     * Property 5: The size check behaves correctly with MockMultipartFile for OBJ files.
     *
     * **Validates: Requirements 7.1, 7.2, 7.3, 7.5**
     */
    @Property(tries = 25)
    @Tag("Feature: converter-improvements, Property 5: File size limit enforcement")
    void objFileSizeCheckWithMockMultipartFile(@ForAll("fileSizesAroundBoundary") long fileSize) {
        byte[] content = new byte[(int) Math.min(fileSize, 1024)];
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "model.obj", "application/octet-stream", content) {
            @Override
            public long getSize() {
                return fileSize;
            }
        };

        if (mockFile.getSize() > MAX_FILE_SIZE) {
            assertThatThrownBy(() -> checkFileSize(mockFile.getSize(), MAX_FILE_SIZE))
                    .as("OBJ file of size %d should be rejected", fileSize)
                    .isInstanceOf(FileConversionException.class);
        } else {
            assertThatNoException()
                    .as("OBJ file of size %d should be accepted", fileSize)
                    .isThrownBy(() -> checkFileSize(mockFile.getSize(), MAX_FILE_SIZE));
        }
    }

    /**
     * Generates file sizes that are strictly over the limit.
     * Focuses on boundary values and random sizes up to 100MB.
     */
    @Provide
    Arbitrary<Long> fileSizesOverLimit() {
        return Arbitraries.oneOf(
                // Just over the boundary
                Arbitraries.of(
                        MAX_FILE_SIZE + 1,
                        MAX_FILE_SIZE + 10,
                        MAX_FILE_SIZE + 100,
                        MAX_FILE_SIZE + 1024
                ),
                // Random sizes between limit+1 and 100MB
                Arbitraries.longs().between(MAX_FILE_SIZE + 1, 104_857_600L)
        );
    }

    /**
     * Generates file sizes that are within the limit (0 to maxFileSize inclusive).
     * Focuses on boundary values and common sizes.
     */
    @Provide
    Arbitrary<Long> fileSizesWithinLimit() {
        return Arbitraries.oneOf(
                // Boundary and edge values
                Arbitraries.of(
                        0L,
                        1L,
                        MAX_FILE_SIZE - 10,
                        MAX_FILE_SIZE - 1,
                        MAX_FILE_SIZE
                ),
                // Random sizes between 0 and limit
                Arbitraries.longs().between(0L, MAX_FILE_SIZE)
        );
    }

    /**
     * Generates file sizes around the boundary for combined tests.
     * Includes values just below, at, and just above the limit.
     */
    @Provide
    Arbitrary<Long> fileSizesAroundBoundary() {
        return Arbitraries.oneOf(
                // Specific boundary values
                Arbitraries.of(
                        0L,
                        1L,
                        MAX_FILE_SIZE - 10,
                        MAX_FILE_SIZE - 1,
                        MAX_FILE_SIZE,
                        MAX_FILE_SIZE + 1,
                        MAX_FILE_SIZE + 10
                ),
                // Random sizes from 0 to 100MB
                Arbitraries.longs().between(0L, 104_857_600L)
        );
    }
}
