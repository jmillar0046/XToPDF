package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.DwfToPdfService;
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
class DwfFileConverterTest {

    @Mock
    private DwfToPdfService dwfToPdfService;

    private DwfFileConverter dwfFileConverter;

    @BeforeEach
    void setUp() {
        dwfFileConverter = new DwfFileConverter(dwfToPdfService);
    }

    @Test
    void testConvertToPDF() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("file", "test.dwf", "application/octet-stream", "content".getBytes());
        String outputFile = "output.pdf";

        dwfFileConverter.convertToPDF(inputFile, outputFile, false);

        verify(dwfToPdfService).convertDwfToPdf(any(MockMultipartFile.class), any(File.class));
    }
}
