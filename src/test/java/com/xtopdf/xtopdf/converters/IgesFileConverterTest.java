package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.IgesToPdfService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class IgesFileConverterTest {

    @Mock
    private IgesToPdfService igesToPdfService;

    private IgesFileConverter igesFileConverter;

    @BeforeEach
    void setUp() {
        igesFileConverter = new IgesFileConverter(igesToPdfService);
    }

    @Test
    void testConvertToPDF() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("file", "test.iges", "application/octet-stream", "content".getBytes());
        String outputFile = "output.pdf";

        igesFileConverter.convertToPDF(inputFile, outputFile, false);

        verify(igesToPdfService).convertIgesToPdf(any(MockMultipartFile.class), any(File.class));
    }
}
