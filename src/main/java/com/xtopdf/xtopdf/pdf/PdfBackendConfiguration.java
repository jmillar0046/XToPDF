package com.xtopdf.xtopdf.pdf;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration class for managing PDF backend providers.
 * This allows switching between different PDF libraries (iText, PDFBox, OpenPDF)
 * via application configuration.
 * 
 * <p>The active backend can be selected using the {@code pdf.backend} property
 * in application.properties or application.yml. Supported values:</p>
 * <ul>
 *   <li>"itext" - iText 7 (AGPL, being phased out)</li>
 *   <li>"pdfbox" - Apache PDFBox (Apache 2.0, recommended)</li>
 * </ul>
 * 
 * <p>Example configuration in application.properties:</p>
 * <pre>
 * pdf.backend=pdfbox
 * </pre>
 */
@Configuration
public class PdfBackendConfiguration {
    
    @Value("${pdf.backend:pdfbox}")
    private String backendType;
    
    /**
     * Creates the primary PDF backend provider based on configuration.
     * This bean will be injected into services that need to create PDF documents.
     * 
     * @param itextBackend The iText backend implementation (if available)
     * @param pdfboxBackend The PDFBox backend implementation
     * @return The configured PDF backend provider
     */
    @Bean
    @Primary
    public PdfBackendProvider pdfBackendProvider(
            @Qualifier("itextBackend") PdfBackendProvider itextBackend,
            @Qualifier("pdfboxBackend") PdfBackendProvider pdfboxBackend) {
        
        return switch (backendType.toLowerCase()) {
            case "itext" -> {
                // Log warning about AGPL license
                System.err.println("WARNING: Using iText backend with AGPL license. " +
                        "Consider migrating to PDFBox (Apache 2.0) for commercial use.");
                yield itextBackend;
            }
            case "pdfbox" -> pdfboxBackend;
            default -> {
                System.err.println("Unknown PDF backend: " + backendType + ". Defaulting to PDFBox.");
                yield pdfboxBackend;
            }
        };
    }
}
