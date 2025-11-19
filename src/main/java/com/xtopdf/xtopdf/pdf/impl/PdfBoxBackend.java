package com.xtopdf.xtopdf.pdf.impl;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Apache PDFBox backend provider implementation.
 * 
 * <p>This provider creates PDF document builders using Apache PDFBox,
 * which is licensed under Apache License 2.0 and suitable for commercial use.</p>
 * 
 * <p>PDFBox is a mature, stable library backed by the Apache Foundation,
 * making it an excellent choice for production deployments.</p>
 */
@Component("pdfboxBackend")
public class PdfBoxBackend implements PdfBackendProvider {
    
    @Override
    public PdfDocumentBuilder createBuilder() throws IOException {
        return new PdfBoxDocumentBuilder();
    }
    
    @Override
    public String getBackendName() {
        return "pdfbox";
    }
}
