package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.EmfToPdfService;
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
class EmfFileConverterTest {

    @Mock
    private EmfToPdfService emfToPdfService;

    private EmfFileConverter emfFileConverter;

    @BeforeEach
    void setUp() {
        emfFileConverter = new EmfFileConverter(emfToPdfService);
    }

    @Test
    void testConvertToPDF() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("file", "test.emf", "application/octet-stream", "content".getBytes());
        String outputFile = "output.pdf";

        emfFileConverter.convertToPDF(inputFile, outputFile, false);

        verify(emfToPdfService).convertEmfToPdf(any(MockMultipartFile.class), any(File.class));
    }
}
