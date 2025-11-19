package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.ObjToPdfService;
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
class ObjFileConverterTest {

    @Mock
    private ObjToPdfService objToPdfService;

    private ObjFileConverter objFileConverter;

    @BeforeEach
    void setUp() {
        objFileConverter = new ObjFileConverter(objToPdfService);
    }

    @Test
    void testConvertToPDF() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("file", "test.obj", "application/octet-stream", "content".getBytes());
        String outputFile = "output.pdf";

        objFileConverter.convertToPDF(inputFile, outputFile, false);

        verify(objToPdfService).convertObjToPdf(any(MockMultipartFile.class), any(File.class));
    }
}
