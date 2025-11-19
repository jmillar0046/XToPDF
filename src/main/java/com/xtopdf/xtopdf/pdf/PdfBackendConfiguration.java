package com.xtopdf.xtopdf.pdf;

import com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration class for PDF backend.
 * This configures Apache PDFBox as the PDF generation engine.
 * 
 * <p>PDFBox is licensed under Apache License 2.0, making it suitable
 * for commercial use without source code disclosure requirements.</p>
 */
@Configuration
public class PdfBackendConfiguration {
    
    /**
     * Creates the PDF backend provider using Apache PDFBox.
     * 
     * @param pdfboxBackend The PDFBox backend implementation
     * @return The PDF backend provider
     */
    @Bean
    @Primary
    public PdfBackendProvider pdfBackendProvider(PdfBoxBackend pdfboxBackend) {
        return pdfboxBackend;
    }
}
