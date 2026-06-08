package com.xtopdf.xtopdf.dto;

/**
 * Data Transfer Object for successful API responses.
 * Provides structured success information to API clients.
 */
public record SuccessResponse(String status, String message) {

    /**
     * Creates a SuccessResponse with status "success" and the given message.
     *
     * @param message the success message to include in the response
     * @return a new SuccessResponse with status "success"
     */
    public static SuccessResponse ok(String message) {
        return new SuccessResponse("success", message);
    }
}
