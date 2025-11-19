# PDF Backend Refactoring Guide

## Overview

This document outlines the ongoing refactoring to remove the iText 7 (AGPL) dependency and replace it with Apache PDFBox (Apache 2.0) to enable commercial use without license restrictions.

## Motivation

**Problem:** iText 7 is licensed under AGPL 3.0, which requires:
- Source code disclosure for any application using it as a network service
- All derivative works must also be AGPL licensed
- This creates legal barriers for commercial deployment

**Solution:** Migrate to Apache PDFBox (Apache 2.0) which:
- ✅ Allows commercial use without source disclosure
- ✅ Is already in our dependencies (used for PDF merging)
- ✅ Has a mature, stable API
- ✅ Is actively maintained by the Apache Foundation

## Architecture

### Abstraction Layer

We've introduced a PDF backend abstraction layer that allows swapping PDF libraries without changing service code:

```
┌─────────────────────────────────────────────────┐
│           Services (TxtToPdfService, etc.)      │
│                                                  │
│  Uses: PdfBackendProvider, PdfDocumentBuilder  │
└──────────────────────┬──────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────┐
│          PdfBackendConfiguration                 │
│  Selects backend based on pdf.backend property  │
└──────────────────────┬──────────────────────────┘
                       │
           ┌───────────┴───────────┐
           │                       │
           ▼                       ▼
┌──────────────────┐    ┌──────────────────┐
│  ITextBackend    │    │  PdfBoxBackend   │
│  (AGPL - temp)   │    │  (Apache 2.0)    │
└──────────────────┘    └──────────────────┘
```

### Key Interfaces

1. **PdfDocumentBuilder** - Main interface for creating PDF documents
   - `newPage()` - Add a new page
   - `addParagraph(text)` - Add text content
   - `addTable(data)` - Add tables
   - `addImage(bytes)` - Add images
   - `drawLine/Circle/Rectangle()` - Draw shapes
   - `save(file)` - Save the PDF

2. **PdfBackendProvider** - Factory for creating builders
   - `createBuilder()` - Create a new builder instance
   - `getBackendName()` - Get backend identifier

3. **PdfBackendConfiguration** - Spring configuration
   - Manages backend selection via `pdf.backend` property
   - Defaults to PDFBox for new installations

## Migration Status

### Phase 1: Abstraction Layer ✅ COMPLETE
- [x] Create `PdfDocumentBuilder` interface
- [x] Create `PdfBackendProvider` interface
- [x] Create `PdfBackendConfiguration` class
- [x] Add Apache 2.0 LICENSE file
- [x] Update README with license information

### Phase 2: Implement PDFBox Backend (IN PROGRESS)
- [ ] Create `PdfBoxDocumentBuilder` implementation
- [ ] Create `PdfBoxBackend` provider
- [ ] Handle text rendering with word wrap
- [ ] Handle image scaling and embedding
- [ ] Handle table layout
- [ ] Handle shape drawing (lines, circles, rectangles)

### Phase 3: Migrate Simple Services
- [ ] TxtToPdfService
- [ ] CsvToPdfService
- [ ] JsonToPdfService
- [ ] XmlToPdfService
- [ ] MarkdownToPdfService
- [ ] Update tests to use abstraction layer

### Phase 4: Migrate Image Services
- [ ] JpegToPdfService
- [ ] PngToPdfService
- [ ] BmpToPdfService
- [ ] GifToPdfService
- [ ] TiffToPdfService

### Phase 5: Migrate Office Document Services
- [ ] DocxToPdfService
- [ ] XlsxToPdfService
- [ ] XlsToPdfService
- [ ] PptxToPdfService
- [ ] Update POI integration

### Phase 6: Migrate CAD/3D Services
- [ ] DxfToPdfService (1,246 lines - requires careful refactoring)
- [ ] DwgToPdfService
- [ ] Other CAD format services
- [ ] All 3D model services

### Phase 7: Handle Special Cases
- [ ] HTML conversion (use Flying Saucer + PDFBox)
- [ ] SVG conversion (use Apache Batik)
- [ ] PageNumberService
- [ ] WatermarkService

### Phase 8: Remove iText Dependency
- [ ] Remove iText from build.gradle
- [ ] Remove ITextBackend implementation
- [ ] Update all documentation
- [ ] Final testing and validation

## Configuration

### Selecting PDF Backend

In `application.properties` or `application.yml`:

```properties
# Use PDFBox (Apache 2.0) - Recommended for commercial use
pdf.backend=pdfbox

# Use iText (AGPL) - Only for backward compatibility
# pdf.backend=itext
```

### Default Behavior

- **New installations:** Default to PDFBox
- **Existing installations:** Can temporarily use iText during migration
- **Production:** Should use PDFBox to avoid AGPL restrictions

## Testing Strategy

1. **Parallel Testing:** Run tests with both backends during migration
2. **Visual Comparison:** Compare PDF outputs from both backends
3. **Regression Testing:** Ensure no functionality is lost
4. **Performance Testing:** Benchmark both backends

## Benefits After Migration

✅ **Legal:** Apache 2.0 license allows commercial use  
✅ **Cost:** No commercial license fees  
✅ **Memory:** PDFBox typically uses less memory  
✅ **Flexibility:** Can swap backends if needed  
✅ **Community:** Apache Foundation backing  

## Timeline

**Estimated completion:** 6-8 weeks with 1 full-time developer

**Current status:** Phase 1 complete, Phase 2 in progress

## Contributing

When contributing to the migration:

1. **Services:** Use `PdfBackendProvider` instead of direct iText imports
2. **Tests:** Test with both backends during transition
3. **Documentation:** Update docs to reflect abstraction layer
4. **Code Review:** Ensure no new iText dependencies are added

## Questions?

See [TECHNICAL_ANALYSIS.md](TECHNICAL_ANALYSIS.md) for the full analysis and detailed migration plan.
