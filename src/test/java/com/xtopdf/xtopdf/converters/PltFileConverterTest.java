package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.PltToPdfService;
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
class PltFileConverterTest {

    @Mock
    private PltToPdfService pltToPdfService;

    private PltFileConverter pltFileConverter;

    @BeforeEach
    void setUp() {
        pltFileConverter = new PltFileConverter(pltToPdfService);
    }

    @Test
    void testConvertToPDF() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("file", "test.plt", "application/octet-stream", "content".getBytes());
        String outputFile = "output.pdf";

        pltFileConverter.convertToPDF(inputFile, outputFile, false);

        verify(pltToPdfService).convertPltToPdf(any(MockMultipartFile.class), any(File.class));
    }
}
