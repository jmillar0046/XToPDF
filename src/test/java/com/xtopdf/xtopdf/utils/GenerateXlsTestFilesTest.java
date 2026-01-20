package com.xtopdf.xtopdf.utils;

import org.junit.jupiter.api.Test;

/**
 * Test to generate XLS test files.
 * Run this test once to create the test files needed for XlsToPdfServiceTest.
 */
class GenerateXlsTestFilesTest {

    @Test
    void generateXlsTestFiles() {
        XlsTestFileGenerator.main(new String[]{});
    }
}
