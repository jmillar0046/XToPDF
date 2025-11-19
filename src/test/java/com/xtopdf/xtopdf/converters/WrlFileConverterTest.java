package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.WrlToPdfService;
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
class WrlFileConverterTest {

    @Mock
    private WrlToPdfService wrlToPdfService;

    private WrlFileConverter wrlFileConverter;

    @BeforeEach
    void setUp() {
        wrlFileConverter = new WrlFileConverter(wrlToPdfService);
    }

    @Test
    void testConvertToPDF() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("file", "test.wrl", "application/octet-stream", "content".getBytes());
        String outputFile = "output.pdf";

        wrlFileConverter.convertToPDF(inputFile, outputFile, false);

        verify(wrlToPdfService).convertWrlToPdf(any(MockMultipartFile.class), any(File.class));
    }
}
