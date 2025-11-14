# XToPDF

XToPDF is a Spring Boot application for converting various file formats to PDF.

## Releases

This project uses semantic versioning with fully automated releases:

- **Automatic Versioning**: Patch version is automatically incremented on every merge to main
- **Automatic Tagging**: Git tags are created automatically by GitHub Actions
- **Automatic Releases**: GitHub Releases are created with JAR artifacts attached
- **No Manual Tagging**: Version tags are protected and only created through automated workflows

### Downloading Releases

1. Go to the [Releases page](https://github.com/jmillar0046/XToPDF/releases)
2. Download the latest `xtopdf-VERSION.jar` file
3. Run it with Java 21+:

```bash
java -jar xtopdf-VERSION.jar
```

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

### CAD Formats
- Convert DXF files to PDF (Drawing Exchange Format)
- Convert DWG files to PDF (AutoCAD Drawing - requires pre-conversion to DXF or external tools)

### Other Features
- REST API endpoints for file conversion
- Optional page numbering with customizable position, alignment, and style
- **Watermark support** - Add customizable watermarks to PDFs with control over text, font size, layer (foreground/background), and orientation (horizontal, vertical, diagonal)
- **Formula recalculation for Excel files** - When enabled, all formulas are recalculated before conversion (useful for macro-dependent formulas)

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

#### Formula Recalculation (Macro Execution)

For Excel files (XLSX, XLS), this recalculates all formulas before conversion:

```bash
curl -X POST http://localhost:8080/api/convert \
  -F "inputFile=@spreadsheet.xlsx" \
  -F "outputFile=output.pdf" \
  -F "executeMacros=true"
```

**Note:** Apache POI cannot execute VBA macros or user-defined functions (UDFs). This feature forces recalculation of all formulas, but formulas that depend on VBA user-defined functions cannot be recalculated since Apache POI cannot execute VBA code.

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

#### Watermark

Add a watermark to the converted PDF:

```bash
curl -X POST http://localhost:8080/api/convert \
  -F "inputFile=@document.docx" \
  -F "outputFile=output.pdf" \
  -F "addWatermark=true" \
  -F "watermarkText=CONFIDENTIAL" \
  -F "watermarkFontSize=48" \
  -F "watermarkLayer=FOREGROUND" \
  -F "watermarkOrientation=DIAGONAL_UP"
```

Watermark options:
- `watermarkText`: The text to display as a watermark (required when `addWatermark=true`)
- `watermarkFontSize`: Font size for the watermark text (default: `48`, range: `0-200`)
- `watermarkLayer`: `FOREGROUND` (in front of content) or `BACKGROUND` (behind content) (default: `FOREGROUND`)
- `watermarkOrientation`: 
  - `HORIZONTAL` - Standard horizontal text
  - `VERTICAL` - Text rotated 90 degrees (bottom to top)
  - `DIAGONAL_UP` - Diagonal from upper-left to bottom-right (default)
  - `DIAGONAL_DOWN` - Diagonal from bottom-left to top-right

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

#### CAD File Conversion

Convert DXF files to PDF:

```bash
curl -X POST http://localhost:8080/api/convert \
  -F "inputFile=@drawing.dxf" \
  -F "outputFile=output.pdf"
```

Convert DWG files to PDF:

**Note:** Direct DWG to PDF conversion requires the DWG file to be in a format compatible with DXF processing, or pre-converted to DXF format using external tools. The application follows the conversion path: DWG → DXF → PDF.

For best results with DWG files, consider using external conversion tools first:
- **ODA File Converter** (free, cross-platform)
- **LibreDWG** (open source command-line tool)
- Commercial libraries like Aspose.CAD or Teigha

```bash
# If your DWG file is compatible or pre-converted
curl -X POST http://localhost:8080/api/convert \
  -F "inputFile=@drawing.dwg" \
  -F "outputFile=output.pdf"
```

## Development

### Automated Release Process

Releases are **fully automated** and happen when pull requests are merged to the `main` branch:

1. Create a pull request with your changes
2. Get the PR reviewed and approved
3. Merge the PR to `main`
4. GitHub Actions automatically:
   - Increments the patch version (e.g., v1.2.3 → v1.2.4)
   - Creates a git tag
   - Builds the project
   - Runs all tests
   - Creates a GitHub Release with JAR files

**Important**: Do not manually create or push version tags. The automated workflow handles all versioning.

### Building Locally

The version is automatically determined from git tags. If no tag exists, it uses v0.0.0.

```bash
./gradlew build
```

The built JAR files will be in `build/libs/`.

## License

This project is provided as-is for educational and commercial purposes.

