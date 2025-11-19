package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.StpToPdfService;
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
class StpFileConverterTest {

    @Mock
    private StpToPdfService stpToPdfService;

    private StpFileConverter stpFileConverter;

    @BeforeEach
    void setUp() {
        stpFileConverter = new StpFileConverter(stpToPdfService);
    }

    @Test
    void testConvertToPDF() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("file", "test.stp", "application/octet-stream", "content".getBytes());
        String outputFile = "output.pdf";

        stpFileConverter.convertToPDF(inputFile, outputFile, false);

        verify(stpToPdfService).convertStpToPdf(any(MockMultipartFile.class), any(File.class));
    }
}
