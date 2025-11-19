package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.StlToPdfService;
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
class StlFileConverterTest {

    @Mock
    private StlToPdfService stlToPdfService;

    private StlFileConverter stlFileConverter;

    @BeforeEach
    void setUp() {
        stlFileConverter = new StlFileConverter(stlToPdfService);
    }

    @Test
    void testConvertToPDF() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("file", "test.stl", "application/octet-stream", "content".getBytes());
        String outputFile = "output.pdf";

        stlFileConverter.convertToPDF(inputFile, outputFile, false);

        verify(stlToPdfService).convertStlToPdf(any(MockMultipartFile.class), any(File.class));
    }
}
