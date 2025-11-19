package com.xtopdf.xtopdf.pdf;

import java.io.IOException;

/**
 * Factory interface for creating PDF document builders.
 * This interface defines the contract for PDF backend providers, allowing
 * different PDF libraries to be plugged in without changing client code.
 * 
 * <p>Implementations should be registered as Spring beans and can be
 * selected via configuration.</p>
 * 
 * @see PdfDocumentBuilder
 */
public interface PdfBackendProvider {
    
    /**
     * Creates a new PDF document builder instance.
     * 
     * @return A new {@link PdfDocumentBuilder} instance
     * @throws IOException if the builder cannot be created
     */
    PdfDocumentBuilder createBuilder() throws IOException;
    
    /**
     * Returns the name of this PDF backend provider.
     * This can be used for logging and configuration.
     * 
     * @return The backend name (e.g., "itext", "pdfbox", "openpdf")
     */
    String getBackendName();
}
