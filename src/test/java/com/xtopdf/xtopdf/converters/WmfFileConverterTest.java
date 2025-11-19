package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.WmfToPdfService;
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
class WmfFileConverterTest {

    @Mock
    private WmfToPdfService wmfToPdfService;

    private WmfFileConverter wmfFileConverter;

    @BeforeEach
    void setUp() {
        wmfFileConverter = new WmfFileConverter(wmfToPdfService);
    }

    @Test
    void testConvertToPDF() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("file", "test.wmf", "application/octet-stream", "content".getBytes());
        String outputFile = "output.pdf";

        wmfFileConverter.convertToPDF(inputFile, outputFile, false);

        verify(wmfToPdfService).convertWmfToPdf(any(MockMultipartFile.class), any(File.class));
    }
}
