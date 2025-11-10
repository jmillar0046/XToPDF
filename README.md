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
- Convert EPS files to PDF (basic support)

### Other Formats
- Convert EPUB files to PDF (basic support)
- Convert XPS files to PDF (basic support)
- REST API endpoints for file conversion

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
