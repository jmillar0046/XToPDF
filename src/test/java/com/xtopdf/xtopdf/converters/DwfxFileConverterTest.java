package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.DwfxToPdfService;
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
class DwfxFileConverterTest {

    @Mock
    private DwfxToPdfService dwfxToPdfService;

    private DwfxFileConverter dwfxFileConverter;

    @BeforeEach
    void setUp() {
        dwfxFileConverter = new DwfxFileConverter(dwfxToPdfService);
    }

    @Test
    void testConvertToPDF() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("file", "test.dwfx", "application/octet-stream", "content".getBytes());
        String outputFile = "output.pdf";

        dwfxFileConverter.convertToPDF(inputFile, outputFile, false);

        verify(dwfxToPdfService).convertDwfxToPdf(any(MockMultipartFile.class), any(File.class));
    }
}
