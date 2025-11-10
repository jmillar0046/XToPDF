# XToPDF

XToPDF is a Spring Boot application for converting various file formats to PDF.

## Features

### Document Formats
- Convert TXT files to PDF
- Convert DOCX files to PDF (Microsoft Word 2007+)
- Convert DOC files to PDF (Microsoft Word 97-2003)
- Convert ODT files to PDF (OpenDocument Text)
- Convert RTF files to PDF
- Convert HTML files to PDF
- Convert Markdown files to PDF (.md and .markdown)
- Convert XML files to PDF
- Convert JSON files to PDF

### Spreadsheet Formats
- Convert XLSX files to PDF (Microsoft Excel 2007+)
- Convert XLS files to PDF (Microsoft Excel 97-2003)
- Convert ODS files to PDF (OpenDocument Spreadsheet)
- Convert CSV files to PDF

### Presentation Formats
- Convert PPTX files to PDF (Microsoft PowerPoint 2007+)
- Convert PPT files to PDF (Microsoft PowerPoint 97-2003)
- Convert ODP files to PDF (OpenDocument Presentation)

### Image Formats
- Convert JPEG/JPG images to PDF
- Convert PNG images to PDF
- Convert BMP images to PDF
- Convert GIF images to PDF
- Convert TIFF/TIF images to PDF
- Convert SVG images to PDF

### Other Features
- REST API endpoints for file conversion
- Optional page numbering with customizable position, alignment, and style
- **Formula recalculation for Excel files** - When enabled, all formulas are recalculated before conversion (useful for macro-dependent formulas)
- **Field updates for Word documents** - When enabled, attempts to update calculated fields before conversion

## Technologies

- Java 21
- Spring Boot 3.5.7
- Apache POI (for Microsoft Office formats)
- ODF Toolkit (for OpenDocument formats)
- iText 7 (for PDF generation)
- PDFBox (for PDF manipulation)
- Commonmark (Markdown parser)
- Gradle

## Getting Started

### Prerequisites

- Java 21+
- Gradle

### Build

```sh
./gradlew build
```

### Run

```sh
./gradlew bootRun
```

## API Usage

### Basic File Conversion

Convert a file to PDF using the REST API:

```bash
curl -X POST http://localhost:8080/api/convert \
  -F "inputFile=@document.xlsx" \
  -F "outputFile=output.pdf"
```

### Advanced Options

#### Formula Recalculation / Field Updates (Macro Execution)

For Excel files (XLSX, XLS), this recalculates all formulas before conversion. For Word files (DOCX, DOC), this updates calculated fields:

```bash
curl -X POST http://localhost:8080/api/convert \
  -F "inputFile=@spreadsheet.xlsx" \
  -F "outputFile=output.pdf" \
  -F "executeMacros=true"
```

**Note:** Apache POI cannot execute VBA macros directly. This feature:
- For Excel: Forces recalculation of all formulas, including those that reference user-defined functions
- For Word: Attempts to update calculated fields (limited support in Apache POI)

#### Page Numbering

Add page numbers to the converted PDF:

```bash
curl -X POST http://localhost:8080/api/convert \
  -F "inputFile=@document.docx" \
  -F "outputFile=output.pdf" \
  -F "addPageNumbers=true" \
  -F "pageNumberPosition=BOTTOM" \
  -F "pageNumberAlignment=CENTER" \
  -F "pageNumberStyle=ARABIC"
```

Page numbering options:
- `pageNumberPosition`: `TOP` or `BOTTOM`
- `pageNumberAlignment`: `LEFT`, `CENTER`, or `RIGHT`
- `pageNumberStyle`: `ARABIC`, `ROMAN_UPPER`, `ROMAN_LOWER`, `ALPHABETIC_UPPER`, or `ALPHABETIC_LOWER`

#### Merge with Existing PDF

Merge the converted file with an existing PDF:

```bash
curl -X POST http://localhost:8080/api/convert \
  -F "inputFile=@newcontent.txt" \
  -F "outputFile=output.pdf" \
  -F "existingPdf=@existing.pdf" \
  -F "position=back"
```

- `position`: `front` (prepend) or `back` (append)

## License

This project is provided as-is for educational and commercial purposes.

