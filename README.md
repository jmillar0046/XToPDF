# XToPDF

XToPDF is a Spring Boot application for converting various file formats to PDF.

## Releases and Packages

This project uses semantic versioning and automated releases:

- **Releases**: Created automatically when a version tag (e.g., `v1.0.0`) is pushed
- **Packages**: Published to [GitHub Packages](https://github.com/jmillar0046/XToPDF/packages) on every release
- **Snapshots**: Development versions are published to GitHub Packages on every push to main

### Using Published Packages

To use this package in your Gradle project:

```gradle
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/jmillar0046/XToPDF")
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.token") ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation 'com.xtopdf:xtopdf:VERSION'
}
```

For Maven projects, add to your `settings.xml` or `pom.xml`:

```xml
<repositories>
  <repository>
    <id>github</id>
    <url>https://maven.pkg.github.com/jmillar0046/XToPDF</url>
  </repository>
</repositories>

<dependency>
  <groupId>com.xtopdf</groupId>
  <artifactId>xtopdf</artifactId>
  <version>VERSION</version>
</dependency>
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

### Other Features
- REST API endpoints for file conversion
- Optional page numbering with customizable position, alignment, and style
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

## Development

### Creating a Release

To create a new release:

1. Update the version following [semantic versioning](https://semver.org/)
2. Create and push a git tag:

```bash
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

3. GitHub Actions will automatically:
   - Build the project
   - Run tests
   - Create a GitHub Release with the JAR files
   - Publish the package to GitHub Packages

### Building Locally

The version is automatically determined from git tags. If no tag exists, it uses the commit SHA.

```bash
./gradlew build
```

To publish to your local Maven repository:

```bash
./gradlew publishToMavenLocal
```

## License

This project is provided as-is for educational and commercial purposes.

