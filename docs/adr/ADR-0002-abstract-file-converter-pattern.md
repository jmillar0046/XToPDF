# ADR-0002: Use AbstractFileConverter Template Method Pattern

## Status

Accepted

## Date

2025-01-15

## Context

XToPDF supports 40+ file format converters. Each converter shares common concerns:

- Null input validation (file must not be null, output path must not be null)
- Exception wrapping (all conversion errors wrapped in `FileConversionException`)
- Consistent error message formatting with format name
- Logging of conversion start/completion

Without a shared abstraction, these cross-cutting concerns would be duplicated across every converter, leading to inconsistent error handling and boilerplate code.

## Decision

We use the Template Method pattern via `AbstractFileConverter`:

```java
public abstract class AbstractFileConverter implements FileConverter {

    @Override
    public final File convert(MultipartFile file, String outputPath) {
        // Template method: shared validation + exception wrapping
        if (file == null || file.isEmpty()) {
            throw new FileConversionException("Input file must not be null or empty");
        }
        if (outputPath == null || outputPath.isBlank()) {
            throw new FileConversionException("Output path must not be null or blank");
        }
        try {
            return doConvert(file, outputPath);
        } catch (FileConversionException e) {
            throw e;
        } catch (Exception e) {
            throw new FileConversionException(
                "Error converting " + getFormatName() + " to PDF: " + e.getMessage(), e);
        }
    }

    protected abstract File doConvert(MultipartFile file, String outputPath) throws Exception;
    public abstract String getFormatName();
    public abstract Set<String> getSupportedExtensions();
}
```

All converters extend `AbstractFileConverter` and implement only:
- `doConvert()` — the actual conversion logic
- `getFormatName()` — format identifier (e.g., "DOCX", "PNG")
- `getSupportedExtensions()` — set of file extensions (e.g., `Set.of(".docx")`)

## Consequences

### Positive

- Zero duplication of validation/error-handling code across 40+ converters
- Guaranteed consistent exception wrapping — no converter can accidentally leak raw exceptions
- Adding a new format requires only conversion logic, not boilerplate
- `final` on `convert()` prevents subclasses from bypassing the template
- `ConverterRegistry` auto-discovers all converters via Spring's `List<FileConverter>` injection

### Negative

- Inheritance hierarchy: all converters must extend the abstract class (no composition)
- Cannot easily override the template steps without modifying the base class
- Deep class hierarchy if further specialization is needed (e.g., ImageFileConverter base)

### Neutral

- Converters are still free to throw `FileConversionException` directly from `doConvert()` (it passes through unwrapped)
- The pattern is well-understood in the Java ecosystem

## Alternatives Considered

| Alternative | Pros | Cons | Reason for Rejection |
|-------------|------|------|------|
| Interface with default methods | No inheritance constraint | Cannot enforce `final`, defaults can be overridden | Loses the guarantee of consistent validation |
| Decorator pattern | Composable, flexible | Complex wiring for 40+ converters, harder to discover | Over-engineered for shared pre/post logic |
| AOP (aspect-oriented) | No base class needed | Implicit behavior, harder to debug, magic | Too much hidden behavior for critical error paths |
| Each converter handles own validation | Simple, self-contained | Massive duplication, inconsistent error messages | Violates DRY, error-prone |

## References

- Gang of Four, "Template Method" pattern
- `src/main/java/com/xtopdf/xtopdf/converters/AbstractFileConverter.java`
- `src/main/java/com/xtopdf/xtopdf/converters/FileConverter.java`
- `src/main/java/com/xtopdf/xtopdf/converters/ConverterRegistry.java`
