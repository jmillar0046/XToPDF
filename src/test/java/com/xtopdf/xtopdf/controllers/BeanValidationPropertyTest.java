package com.xtopdf.xtopdf.controllers;

import com.xtopdf.xtopdf.dto.ConversionRequest;
import com.xtopdf.xtopdf.dto.WatermarkRequest;
import net.jqwik.api.*;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for bean validation error response structure.
 *
 * Feature: codebase-hardening, Property 11: Bean validation error response structure
 *
 * For any request DTO that violates one or more Jakarta Bean Validation constraints,
 * the validation SHALL produce field-level error messages identifying each invalid field.
 *
 * Validates: Requirements 14.4
 */
@Tag("Feature: codebase-hardening, Property 11: Bean validation error response structure")
class BeanValidationPropertyTest {

    private final Validator validator;

    BeanValidationPropertyTest() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            this.validator = factory.getValidator();
        }
    }

    /**
     * Property 11a: ConversionRequest with blank outputFile produces validation error.
     *
     * For any blank/null outputFile value, the validator SHALL report a violation
     * on the "outputFile" field.
     *
     * Validates: Requirements 14.4
     */
    @Property(tries = 25)
    @Label("ConversionRequest with blank outputFile fails validation with field-level error")
    void conversionRequestBlankOutputFileFailsValidation(
            @ForAll("blankStrings") String blankValue) {

        ConversionRequest request = ConversionRequest.builder()
                .outputFile(blankValue)
                .build();

        Set<ConstraintViolation<ConversionRequest>> violations = validator.validate(request);

        assertThat(violations)
                .as("Blank outputFile should produce at least one validation violation")
                .isNotEmpty();

        assertThat(violations.stream().map(v -> v.getPropertyPath().toString()))
                .as("Violation should identify the 'outputFile' field")
                .contains("outputFile");
    }

    /**
     * Property 11b: WatermarkRequest with blank watermarkText produces validation error.
     *
     * For any blank/null text value, the validator SHALL report a violation
     * on the "text" field.
     *
     * Validates: Requirements 14.4
     */
    @Property(tries = 25)
    @Label("WatermarkRequest with blank text fails validation with field-level error")
    void watermarkRequestBlankTextFailsValidation(
            @ForAll("blankStrings") String blankValue) {

        WatermarkRequest request = WatermarkRequest.builder()
                .text(blankValue)
                .fontSize(48.0f)
                .build();

        Set<ConstraintViolation<WatermarkRequest>> violations = validator.validate(request);

        assertThat(violations)
                .as("Blank watermarkText should produce at least one validation violation")
                .isNotEmpty();

        assertThat(violations.stream().map(v -> v.getPropertyPath().toString()))
                .as("Violation should identify the 'text' field")
                .contains("text");
    }

    /**
     * Property 11c: WatermarkRequest with fontSize out of range produces validation error.
     *
     * For any fontSize value < 1 or > 200, the validator SHALL report a violation
     * on the "fontSize" field.
     *
     * Validates: Requirements 14.4
     */
    @Property(tries = 25)
    @Label("WatermarkRequest with out-of-range fontSize fails validation with field-level error")
    void watermarkRequestOutOfRangeFontSizeFailsValidation(
            @ForAll("outOfRangeFontSizes") float invalidFontSize) {

        WatermarkRequest request = WatermarkRequest.builder()
                .text("Valid watermark text")
                .fontSize(invalidFontSize)
                .build();

        Set<ConstraintViolation<WatermarkRequest>> violations = validator.validate(request);

        assertThat(violations)
                .as("Out-of-range fontSize (%f) should produce at least one validation violation", invalidFontSize)
                .isNotEmpty();

        assertThat(violations.stream().map(v -> v.getPropertyPath().toString()))
                .as("Violation should identify the 'fontSize' field")
                .contains("fontSize");
    }

    /**
     * Property 11d: Valid WatermarkRequest passes validation.
     *
     * For any valid fontSize (1-200) and non-blank text, validation SHALL produce
     * zero violations.
     *
     * Validates: Requirements 14.4
     */
    @Property(tries = 25)
    @Label("WatermarkRequest with valid values passes validation")
    void watermarkRequestWithValidValuesPassesValidation(
            @ForAll("validFontSizes") float validFontSize,
            @ForAll("nonBlankStrings") String validText) {

        WatermarkRequest request = WatermarkRequest.builder()
                .text(validText)
                .fontSize(validFontSize)
                .build();

        Set<ConstraintViolation<WatermarkRequest>> violations = validator.validate(request);

        assertThat(violations)
                .as("Valid WatermarkRequest should produce zero violations")
                .isEmpty();
    }

    // --- Arbitraries ---

    @Provide
    Arbitrary<String> blankStrings() {
        return Arbitraries.of("", "   ", "\t", "\n", " \t\n ");
    }

    @Provide
    Arbitrary<Float> outOfRangeFontSizes() {
        return Arbitraries.oneOf(
                // Below minimum (< 1)
                Arbitraries.floats().between(-100f, 0.99f),
                // Above maximum (> 200)
                Arbitraries.floats().between(200.01f, 1000f)
        );
    }

    @Provide
    Arbitrary<Float> validFontSizes() {
        return Arbitraries.floats().between(1.0f, 200.0f);
    }

    @Provide
    Arbitrary<String> nonBlankStrings() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(50);
    }
}
