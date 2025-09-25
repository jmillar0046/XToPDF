package com.xtopdf.xtopdf;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

class XtopdfApplicationTest {

    @Test
    void testMain() {
        try (MockedStatic<SpringApplication> mockedSpringApplication = mockStatic(SpringApplication.class)) {
            String[] args = {"arg1", "arg2"};
            
            XtopdfApplication.main(args);
            
            mockedSpringApplication.verify(() -> SpringApplication.run(XtopdfApplication.class, args));
        }
    }
}