package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.DwtToPdfService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DwtFileConverterTest {

    @Mock
    private DwtToPdfService dwtToPdfService;

    private DwtFileConverter dwtFileConverter;

    @BeforeEach
    void setUp() {
        dwtFileConverter = new DwtFileConverter(dwtToPdfService);
    }

    @Test
    void testConvertToPDF() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("file", "test.dwt", "application/octet-stream", "content".getBytes());
        String outputFile = "output.pdf";

        dwtFileConverter.convertToPDF(inputFile, outputFile, false);

        verify(dwtToPdfService).convertDwtToPdf(any(MockMultipartFile.class), any(File.class));
    }
}
