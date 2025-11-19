package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.IgsToPdfService;
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
class IgsFileConverterTest {

    @Mock
    private IgsToPdfService igsToPdfService;

    private IgsFileConverter igsFileConverter;

    @BeforeEach
    void setUp() {
        igsFileConverter = new IgsFileConverter(igsToPdfService);
    }

    @Test
    void testConvertToPDF() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("file", "test.igs", "application/octet-stream", "content".getBytes());
        String outputFile = "output.pdf";

        igsFileConverter.convertToPDF(inputFile, outputFile, false);

        verify(igsToPdfService).convertIgsToPdf(any(MockMultipartFile.class), any(File.class));
    }
}
