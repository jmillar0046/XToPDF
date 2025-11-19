package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.HpglToPdfService;
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
class HpglFileConverterTest {

    @Mock
    private HpglToPdfService hpglToPdfService;

    private HpglFileConverter hpglFileConverter;

    @BeforeEach
    void setUp() {
        hpglFileConverter = new HpglFileConverter(hpglToPdfService);
    }

    @Test
    void testConvertToPDF() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("file", "test.hpgl", "application/octet-stream", "content".getBytes());
        String outputFile = "output.pdf";

        hpglFileConverter.convertToPDF(inputFile, outputFile, false);

        verify(hpglToPdfService).convertHpglToPdf(any(MockMultipartFile.class), any(File.class));
    }
}
