package com.xtopdf.xtopdf.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
@Slf4j
public class PdfMergeService {
    
    /**
     * Merges an existing PDF with a newly converted PDF.
     * 
     * @param convertedPdfFile The newly converted PDF file
     * @param existingPdf The existing PDF file to merge
     * @param position The position to add the existing PDF ("front" or "back")
     * @throws IOException if there's an error during the merge process
     */
    public void mergePdfs(File convertedPdfFile, MultipartFile existingPdf, String position) throws IOException {
        PDFMergerUtility pdfMerger = new PDFMergerUtility();
        File tempFile = File.createTempFile("merged_", ".pdf");
        
        try {
            // Save the existing PDF to a temporary file
            File existingPdfFile = File.createTempFile("existing_", ".pdf");
            try (FileOutputStream fos = new FileOutputStream(existingPdfFile)) {
                fos.write(existingPdf.getBytes());
            }
            
            // Set the destination file for the merged PDF
            pdfMerger.setDestinationFileName(tempFile.getAbsolutePath());
            
            // Add PDFs in the specified order
            if ("front".equalsIgnoreCase(position)) {
                pdfMerger.addSource(existingPdfFile);
                pdfMerger.addSource(convertedPdfFile);
            } else {
                pdfMerger.addSource(convertedPdfFile);
                pdfMerger.addSource(existingPdfFile);
            }
            
            // Merge the PDFs
            pdfMerger.mergeDocuments(null);
            
            // Replace the original converted PDF with the merged PDF
            if (!convertedPdfFile.delete()) {
                log.warn("Could not delete original converted PDF file");
            }
            if (!tempFile.renameTo(convertedPdfFile)) {
                // If rename fails, copy the content
                try (FileOutputStream fos = new FileOutputStream(convertedPdfFile)) {
                    java.nio.file.Files.copy(tempFile.toPath(), fos);
                }
            }
            
            // Clean up temporary files
            existingPdfFile.delete();
            tempFile.delete();
            
        } catch (Exception e) {
            tempFile.delete();
            throw new IOException("Error merging PDFs: " + e.getMessage(), e);
        }
    }
}
