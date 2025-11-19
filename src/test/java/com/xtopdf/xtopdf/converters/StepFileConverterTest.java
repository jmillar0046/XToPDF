package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.StepToPdfService;
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
class StepFileConverterTest {

    @Mock
    private StepToPdfService stepToPdfService;

    private StepFileConverter stepFileConverter;

    @BeforeEach
    void setUp() {
        stepFileConverter = new StepFileConverter(stepToPdfService);
    }

    @Test
    void testConvertToPDF() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile("file", "test.step", "application/octet-stream", "content".getBytes());
        String outputFile = "output.pdf";

        stepFileConverter.convertToPDF(inputFile, outputFile, false);

        verify(stepToPdfService).convertStepToPdf(any(MockMultipartFile.class), any(File.class));
    }
}
