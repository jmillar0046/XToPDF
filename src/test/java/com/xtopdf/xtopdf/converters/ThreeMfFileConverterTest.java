package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.ThreeMfToPdfService;
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
class ThreeMfFileConverterTest {

    @Mock
    private ThreeMfToPdfService threeMfToPdfService;

    private ThreeMfFileConverter threeMfFileConverter;

    @BeforeEach
    void setUp() {
        threeMfFileConverter = new ThreeMfFileConverter(threeMfToPdfService);
    }

    @Test
    void testConvertToPDF() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("file", "test.3mf", "application/octet-stream", "content".getBytes());
        String outputFile = "output.pdf";

        threeMfFileConverter.convertToPDF(inputFile, outputFile, false);

        verify(threeMfToPdfService).convert3mfToPdf(any(MockMultipartFile.class), any(File.class));
    }
}
