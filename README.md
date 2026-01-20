# XToPDF

[![Build Status](https://img.shields.io/github/actions/workflow/status/jmillar0046/XToPDF/build.yml?branch=main)](https://github.com/jmillar0046/XToPDF/actions)
[![Latest Release](https://img.shields.io/github/v/release/jmillar0046/XToPDF)](https://github.com/jmillar0046/XToPDF/releases/latest)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](https://github.com/jmillar0046/XToPDF/pulls)

**XToPDF** is an open-source, all-in-one file-to-PDF converter supporting documents, spreadsheets, presentations, images, CAD files, and 3D models. Built with Spring Boot and Java 25, it provides both a REST API and command-line interface for seamless file conversion workflows.

## Features

- **50+ File Formats**: Convert documents (DOCX, DOC, ODT, RTF, TXT, HTML, Markdown), spreadsheets (XLSX, XLS, ODS, CSV, TSV), presentations (PPTX, PPT, ODP), images (PNG, JPEG, SVG, TIFF), and more
- **CAD & 3D Support**: Professional-grade conversion for DXF, DWG, DWF, STL, OBJ, STEP, IGES formats
- **REST API**: Simple HTTP endpoints for programmatic conversion
- **Advanced Features**: Page numbering, watermarks, formula recalculation, PDF merging
- **Open Source**: Apache 2.0 licensed, free for commercial use

## Installation / Quick Start

### Download the Latest JAR

1. Visit the [Releases page](https://github.com/jmillar0046/XToPDF/releases/latest)
2. Download `xtopdf-VERSION.jar`
3. Ensure Java 25+ is installed

### Running Conversions

Start the server:

```bash
java -jar xtopdf-VERSION.jar
```

Convert files via command line:

```bash
# Convert DOCX to PDF
curl -X POST http://localhost:8080/api/convert \
  -F "inputFile=@document.docx" \
  -F "outputFile=output.pdf"

# Convert PNG to PDF
curl -X POST http://localhost:8080/api/convert \
  -F "inputFile=@image.png" \
  -F "outputFile=output.pdf"

# Convert TSV to PDF
curl -X POST http://localhost:8080/api/convert \
  -F "inputFile=@data.tsv" \
  -F "outputFile=output.pdf"

# Convert DXF (CAD) to PDF
curl -X POST http://localhost:8080/api/convert \
  -F "inputFile=@drawing.dxf" \
  -F "outputFile=output.pdf"
```

## Supported Formats

| Category | Supported Formats |
|----------|-------------------|
| **Documents** | DOCX, DOC, ODT, RTF, TXT, HTML, Markdown (.md), XML, JSON |
| **Spreadsheets** | XLSX, XLS, ODS, CSV, TSV/TAB |
| **Presentations** | PPTX, PPT, ODP |
| **Images** | PNG, JPEG/JPG, BMP, GIF, TIFF/TIF, SVG, EMF, WMF |
| **CAD** | DXF, DWG, DWF, DWFX, DWT, HPGL, PLT |
| **3D Models** | STL, OBJ, STEP/STP, IGES/IGS, 3MF, WRL (VRML), X3D |

## Use Cases

- **Batch Conversion**: Automate large-scale document processing workflows
- **Engineering & CAD**: Convert technical drawings (DXF, DWG) to PDF for sharing and archival
- **Document Management**: Standardize diverse file formats into searchable PDFs
- **API Integration**: Embed conversion capabilities into web applications and services
- **3D Manufacturing**: Generate PDF documentation from 3D model files (STL, STEP, OBJ)
- **Report Generation**: Convert spreadsheets and presentations to PDF for distribution

## Advanced Features

### Page Numbering

Add customizable page numbers to PDFs:

```bash
curl -X POST http://localhost:8080/api/convert \
  -F "inputFile=@document.docx" \
  -F "outputFile=output.pdf" \
  -F "addPageNumbers=true" \
  -F "pageNumberPosition=BOTTOM" \
  -F "pageNumberAlignment=CENTER" \
  -F "pageNumberStyle=ARABIC"
```

Options: `TOP/BOTTOM` position, `LEFT/CENTER/RIGHT` alignment, `ARABIC/ROMAN_UPPER/ROMAN_LOWER` styles

### Watermarks

Add text watermarks with custom orientation and styling:

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

Options: `HORIZONTAL/VERTICAL/DIAGONAL_UP/DIAGONAL_DOWN` orientation, `FOREGROUND/BACKGROUND` layer

### Excel Formula Recalculation

Force formula recalculation for macro-dependent spreadsheets:

```bash
curl -X POST http://localhost:8080/api/convert \
  -F "inputFile=@spreadsheet.xlsx" \
  -F "outputFile=output.pdf" \
  -F "executeMacros=true"
```

**Note**: VBA macros and user-defined functions cannot be executed by Apache POI.

### PDF Merging

Merge converted files with existing PDFs:

```bash
curl -X POST http://localhost:8080/api/convert \
  -F "inputFile=@newcontent.txt" \
  -F "outputFile=output.pdf" \
  -F "existingPdf=@existing.pdf" \
  -F "position=back"
```

## Building from Source

### Prerequisites

- Java 21+
- Gradle (included via wrapper)

### Build

```bash
./gradlew build
```

### Run Locally

```bash
./gradlew bootRun
```

The server will start at `http://localhost:8080`.

### Run Tests

```bash
./gradlew test
```

## Contributing

We welcome contributions! Here's how to get started:

1. **Fork the repository** and create a feature branch
2. **Make your changes** with clear, descriptive commits
3. **Add tests** for new functionality
4. **Submit a pull request** with a detailed description

### Good First Issues

Looking for a place to start? Check out issues labeled [`good first issue`](https://github.com/jmillar0046/XToPDF/issues?q=is%3Aissue+is%3Aopen+label%3A%22good+first+issue%22).

### Contributing Guidelines

- Follow existing code style and conventions
- Ensure all tests pass before submitting PR
- Update documentation for user-facing changes
- See [CONTRIBUTING.md](CONTRIBUTING.md) for detailed guidelines

### Code of Conduct

This project adheres to a [Code of Conduct](CODE_OF_CONDUCT.md). By participating, you agree to uphold this code.

## Technology Stack

- **Java 21** - Core runtime
- **Spring Boot 4.0.0** - Application framework
- **Apache POI** - Microsoft Office format processing
- **PDFBox 3.0.6** - PDF generation and manipulation
- **ODF Toolkit** - OpenDocument format support
- **Commonmark** - Markdown parsing

## Performance Characteristics

### File Size Limits

| Limit | Value | Configurable |
|-------|-------|--------------|
| Maximum file size | 100 MB | Yes (MAX_FILE_SIZE) |
| Maximum line length (CSV/TSV) | 1 MB | Yes (MAX_LINE_LENGTH) |
| Maximum fields per row (CSV/TSV) | 10,000 | Yes (MAX_FIELDS) |
| Streaming threshold | 10 MB | Yes (STREAMING_THRESHOLD) |

### Memory Usage

- **Small files (<10MB)**: ~2x file size in memory
- **Large files (>10MB)**: ~1.5x file size in memory (streaming mode)
- **Maximum memory**: Should not exceed 3x file size

### Conversion Times (Approximate)

| File Size | Format | Expected Time |
|-----------|--------|---------------|
| 1 MB | CSV/TSV | ~500ms |
| 10 MB | CSV/TSV | ~2s |
| 50 MB | CSV/TSV | ~10s |
| 100 MB | CSV/TSV | ~20s |

*Note: Times vary based on hardware and data complexity*

### Streaming Mode

For CSV/TSV files larger than 10MB, XToPDF automatically uses streaming mode to minimize memory usage:
- Processes files in chunks of 1,000 rows
- Reduces memory footprint by ~33%
- Enables conversion of files up to 100MB

## Error Codes and Meanings

When a conversion fails, the API returns a structured error response with:
- **errorCode**: Type of error
- **message**: Human-readable description
- **correlationId**: Unique ID for tracking in logs

### Common Error Codes

| Error Code | HTTP Status | Meaning | Solution |
|------------|-------------|---------|----------|
| `CONVERSION_ERROR` | 400 | File conversion failed | Check file format, ensure file is not corrupted |
| `IO_ERROR` | 500 | File read/write failed | Check file permissions, disk space |
| `INTERNAL_ERROR` | 500 | Unexpected server error | Contact support with correlation ID |

### Example Error Response

```json
{
  "errorCode": "CONVERSION_ERROR",
  "message": "Line 100 exceeds maximum length: 1000000",
  "correlationId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

## Troubleshooting

### Common Issues

#### "File size exceeds maximum allowed"

**Cause**: Input file is larger than 100MB  
**Solution**: 
- Split the file into smaller chunks
- Increase MAX_FILE_SIZE in application configuration
- Use streaming mode (automatic for CSV/TSV >10MB)

#### "Line exceeds maximum length"

**Cause**: CSV/TSV file has a line longer than 1MB  
**Solution**:
- Check for malformed data (unclosed quotes, missing delimiters)
- Increase MAX_LINE_LENGTH if legitimate
- Split data into multiple rows

#### "Line exceeds maximum field count"

**Cause**: CSV/TSV file has more than 10,000 columns  
**Solution**:
- Verify file format is correct
- Increase MAX_FIELDS if legitimate
- Consider alternative data format

#### "Failed to delete temporary file"

**Cause**: Temporary file cleanup failed (permissions, disk full)  
**Solution**:
- Check disk space: `df -h`
- Check file permissions in temp directory
- Manually clean up: `find /tmp -name "temp_*" -mtime +1 -delete`

#### "Conversion timeout"

**Cause**: Large or complex file taking too long  
**Solution**:
- Increase server timeout settings
- Use streaming mode for large files
- Simplify file content (remove complex formatting)

### Getting Help

If you encounter an error:

1. **Note the correlation ID** from the error response
2. **Check application logs** for detailed stack trace:
   ```bash
   grep "correlationId=YOUR_ID" application.log
   ```
3. **Search existing issues** on GitHub
4. **Create a new issue** with:
   - Error message and correlation ID
   - File type and size
   - Steps to reproduce
   - Relevant log excerpts

For more detailed troubleshooting, see [TROUBLESHOOTING.md](TROUBLESHOOTING.md).

## Monitoring and Observability

XToPDF includes comprehensive logging and monitoring capabilities:

- **Structured logging** with correlation IDs for request tracking
- **Performance metrics** for conversion duration and memory usage
- **Error tracking** with detailed stack traces
- **Temporary file monitoring** to prevent disk space issues

For detailed monitoring setup and metrics, see [MONITORING.md](MONITORING.md).

## Release Process

XToPDF uses automated semantic versioning:

- **Automatic releases** on merge to `main` branch
- **GitHub Actions** handles versioning, tagging, and artifact publishing
- Download JARs from the [Releases page](https://github.com/jmillar0046/XToPDF/releases)

**Note**: Do not manually create version tags. The CI/CD pipeline manages all versioning.

## License

Licensed under the [Apache License 2.0](LICENSE) - see the [LICENSE](LICENSE) file for details.

**Commercial use permitted** - Apache 2.0 allows commercial use, modification, distribution, and private use.

## Support

- **Issues**: [GitHub Issues](https://github.com/jmillar0046/XToPDF/issues)
- **Discussions**: [GitHub Discussions](https://github.com/jmillar0046/XToPDF/discussions)
- **Documentation**: Check the [Wiki](https://github.com/jmillar0046/XToPDF/wiki) for detailed guides

---

Made with ❤️ by the XToPDF community

