package com.xtopdf.xtopdf.utils;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test to generate Excel test files.
 * This test creates the necessary Excel files for testing Excel conversion services.
 */
@SpringBootTest
class ExcelTestFileGeneratorTest {

    @Test
    void generateTestFiles() {
        // Run the generator
        ExcelTestFileGenerator.main(new String[]{});
        
        // Verify files were created
        String testFilesDir = "src/test/resources/test-files/";
        
        File basicFile = new File(testFilesDir + "basic-spreadsheet.xlsx");
        assertTrue(basicFile.exists(), "basic-spreadsheet.xlsx should exist");
        assertTrue(basicFile.length() > 0, "basic-spreadsheet.xlsx should not be empty");
        
        File formulasFile = new File(testFilesDir + "formulas-spreadsheet.xlsx");
        assertTrue(formulasFile.exists(), "formulas-spreadsheet.xlsx should exist");
        assertTrue(formulasFile.length() > 0, "formulas-spreadsheet.xlsx should not be empty");
        
        File chartsFile = new File(testFilesDir + "charts-spreadsheet.xlsx");
        assertTrue(chartsFile.exists(), "charts-spreadsheet.xlsx should exist");
        assertTrue(chartsFile.length() > 0, "charts-spreadsheet.xlsx should not be empty");
        
        File formattedFile = new File(testFilesDir + "formatted-spreadsheet.xlsx");
        assertTrue(formattedFile.exists(), "formatted-spreadsheet.xlsx should exist");
        assertTrue(formattedFile.length() > 0, "formatted-spreadsheet.xlsx should not be empty");
        
        File corruptedFile = new File(testFilesDir + "corrupted-spreadsheet.xlsx");
        assertTrue(corruptedFile.exists(), "corrupted-spreadsheet.xlsx should exist");
        assertTrue(corruptedFile.length() > 0, "corrupted-spreadsheet.xlsx should not be empty");
        
        System.out.println("\n✓ All test Excel files verified successfully!");
    }
}
