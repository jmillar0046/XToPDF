import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.mock.web.MockMultipartFile;

// Import our service
import com.xtopdf.xtopdf.services.XlsxToPdfService;

public class XlsxTest {
    public static void main(String[] args) throws Exception {
        // Create a simple XLSX file in memory
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test Sheet");
        
        // Create header row
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Name");
        headerRow.createCell(1).setCellValue("Age");
        headerRow.createCell(2).setCellValue("City");
        
        // Create data rows
        Row row1 = sheet.createRow(1);
        row1.createCell(0).setCellValue("John Doe");
        row1.createCell(1).setCellValue(30);
        row1.createCell(2).setCellValue("New York");
        
        Row row2 = sheet.createRow(2);
        row2.createCell(0).setCellValue("Jane Smith");
        row2.createCell(1).setCellValue(25);
        row2.createCell(2).setCellValue("Los Angeles");
        
        // Write to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();
        
        // Create MockMultipartFile
        MockMultipartFile multipartFile = new MockMultipartFile(
            "file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
            baos.toByteArray()
        );
        
        // Test our service
        XlsxToPdfService service = new XlsxToPdfService();
        File outputFile = new File("/tmp/xlsx-test/output.pdf");
        
        try {
            service.convertXlsxToPdf(multipartFile, outputFile);
            System.out.println("✅ XLSX conversion successful! PDF created at: " + outputFile.getAbsolutePath());
            System.out.println("📄 PDF file size: " + outputFile.length() + " bytes");
        } catch (Exception e) {
            System.out.println("❌ XLSX conversion failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}