package com.xtopdf.xtopdf.controllers.graphql;

/**
 * GraphQL type representing the result of a file conversion operation.
 */
public record ConversionResult(
        String id,
        String fileName,
        String status,
        Integer fileSize,
        String submittedAt,
        String completedAt,
        String errorMessage) {}
