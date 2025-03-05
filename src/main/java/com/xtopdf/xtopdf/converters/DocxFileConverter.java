package com.xtopdf.xtopdf.converters;

import org.springframework.stereotype.Component;

@Component
public class DocxFileConverter implements FileConverter {

    @Autowired
    private DocxToPdfService docxToPdfService;

    @Override
    public void convertToPDF(String inputFile, String outputFile) {
        docxToPdfService.convertDocxToPdf(new File(inputFile), new File(outputFile));
    }
    
}
