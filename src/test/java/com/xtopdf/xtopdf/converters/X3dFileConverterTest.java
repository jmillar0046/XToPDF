package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.X3dToPdfService;
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
class X3dFileConverterTest {

    @Mock
    private X3dToPdfService x3dToPdfService;

    private X3dFileConverter x3dFileConverter;

    @BeforeEach
    void setUp() {
        x3dFileConverter = new X3dFileConverter(x3dToPdfService);
    }

    @Test
    void testConvertToPDF() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("file", "test.x3d", "application/octet-stream", "content".getBytes());
        String outputFile = "output.pdf";

        x3dFileConverter.convertToPDF(inputFile, outputFile, false);

        verify(x3dToPdfService).convertX3dToPdf(any(MockMultipartFile.class), any(File.class));
    }
}
