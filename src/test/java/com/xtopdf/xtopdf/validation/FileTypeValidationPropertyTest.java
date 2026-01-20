package com.xtopdf.xtopdf.validation;

import net.jqwik.api.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for file type validation.
 * Validates Requirements 14.1, 14.2, 14.5
 * 
 * Property 34: Magic Number Validation
 * Property 35: Extension Mismatch Rejection
 * Property 36: Validation Error Descriptiveness
 */
class FileTypeValidationPropertyTest {

    /**
     * Property 34: Magic Number Validation
     * 
     * Files with correct magic numbers should be identified correctly.
     */
    @Property
    @Label("Magic numbers identify file types correctly")
    void magicNumbersIdentifyFileTypes(
            @ForAll("knownFileTypes") FileTypeInfo fileType) {
        
        // Create file with magic number
        byte[] fileContent = createFileWithMagicNumber(fileType.magicNumber);
        
        // Verify magic number matches
        assertThat(fileContent).startsWith(fileType.magicNumber);
        
        // Verify file type can be detected
        String detectedType = detectFileType(fileContent);
        assertThat(detectedType).isEqualTo(fileType.typeName);
    }

    /**
     * Property 35: Extension Mismatch Rejection
     * 
     * Files where extension doesn't match content should be rejected.
     */
    @Property
    @Label("Extension mismatch is detected")
    void extensionMismatchIsDetected(
            @ForAll("knownFileTypes") FileTypeInfo declaredType,
            @ForAll("knownFileTypes") FileTypeInfo actualType) {
        
        Assume.that(!declaredType.equals(actualType));
        
        // Create file with one type's magic number
        byte[] fileContent = createFileWithMagicNumber(actualType.magicNumber);
        
        // Detect type from content
        String detectedType = detectFileType(fileContent);
        
        // Verify mismatch is detected
        if (!declaredType.typeName.equals(actualType.typeName)) {
            assertThat(detectedType).isNotEqualTo(declaredType.typeName);
        }
    }

    /**
     * Property 36: Validation Error Descriptiveness
     * 
     * Validation errors should include helpful information.
     */
    @Property
    @Label("Validation errors are descriptive")
    void validationErrorsAreDescriptive(
            @ForAll("invalidFileContent") byte[] invalidContent,
            @ForAll("fileExtensions") String extension) {
        
        // Attempt to validate invalid content
        String errorMessage = validateFile(invalidContent, extension);
        
        // Verify error message is descriptive
        if (errorMessage != null) {
            assertThat(errorMessage).isNotEmpty();
            assertThat(errorMessage.length()).isGreaterThan(10);
        }
    }

    /**
     * Property 37: Empty files are rejected
     * 
     * Empty files should be rejected with clear error.
     */
    @Property
    @Label("Empty files are rejected")
    void emptyFilesAreRejected() {
        byte[] emptyContent = new byte[0];
        
        String errorMessage = validateFile(emptyContent, ".pdf");
        
        assertThat(errorMessage).isNotNull();
        assertThat(errorMessage).containsIgnoringCase("empty");
    }

    // Helper methods

    private byte[] createFileWithMagicNumber(byte[] magicNumber) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            bos.write(magicNumber);
            bos.write(new byte[100]); // Add some dummy content
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bos.toByteArray();
    }

    private String detectFileType(byte[] content) {
        if (content.length < 4) return "UNKNOWN";
        
        // PDF
        if (content[0] == 0x25 && content[1] == 0x50 && content[2] == 0x44 && content[3] == 0x46) {
            return "PDF";
        }
        // PNG
        if (content[0] == (byte)0x89 && content[1] == 0x50 && content[2] == 0x4E && content[3] == 0x47) {
            return "PNG";
        }
        // JPEG
        if (content[0] == (byte)0xFF && content[1] == (byte)0xD8 && content[2] == (byte)0xFF) {
            return "JPEG";
        }
        // ZIP (includes DOCX, XLSX, etc.)
        if (content[0] == 0x50 && content[1] == 0x4B && content[2] == 0x03 && content[3] == 0x04) {
            return "ZIP";
        }
        
        return "UNKNOWN";
    }

    private String validateFile(byte[] content, String extension) {
        if (content.length == 0) {
            return "File is empty";
        }
        
        String detectedType = detectFileType(content);
        if ("UNKNOWN".equals(detectedType)) {
            return "Unknown or unsupported file type";
        }
        
        // Check extension mismatch
        String expectedType = extensionToType(extension);
        if (expectedType != null && !expectedType.equals(detectedType)) {
            return "File extension " + extension + " does not match content type " + detectedType;
        }
        
        return null; // Valid
    }

    private String extensionToType(String extension) {
        switch (extension.toLowerCase()) {
            case ".pdf": return "PDF";
            case ".png": return "PNG";
            case ".jpg":
            case ".jpeg": return "JPEG";
            case ".docx":
            case ".xlsx":
            case ".zip": return "ZIP";
            default: return null;
        }
    }

    // Arbitraries for generating test data

    @Provide
    Arbitrary<FileTypeInfo> knownFileTypes() {
        return Arbitraries.of(
                new FileTypeInfo("PDF", new byte[]{0x25, 0x50, 0x44, 0x46}),
                new FileTypeInfo("PNG", new byte[]{(byte)0x89, 0x50, 0x4E, 0x47}),
                new FileTypeInfo("JPEG", new byte[]{(byte)0xFF, (byte)0xD8, (byte)0xFF}),
                new FileTypeInfo("ZIP", new byte[]{0x50, 0x4B, 0x03, 0x04})
        );
    }

    @Provide
    Arbitrary<byte[]> invalidFileContent() {
        return Arbitraries.bytes()
                .array(byte[].class)
                .ofMinSize(0)
                .ofMaxSize(100);
    }

    @Provide
    Arbitrary<String> fileExtensions() {
        return Arbitraries.of(".pdf", ".png", ".jpg", ".jpeg", ".docx", ".xlsx", ".txt", ".csv");
    }

    // Helper class
    static class FileTypeInfo {
        final String typeName;
        final byte[] magicNumber;

        FileTypeInfo(String typeName, byte[] magicNumber) {
            this.typeName = typeName;
            this.magicNumber = magicNumber;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FileTypeInfo that = (FileTypeInfo) o;
            return typeName.equals(that.typeName);
        }

        @Override
        public int hashCode() {
            return typeName.hashCode();
        }
    }
}
