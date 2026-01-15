# Code Coverage Analysis

## Current Coverage Status

**Overall Coverage: 81% instruction coverage, 71% branch coverage**

This is a strong baseline, but there are opportunities to improve coverage in specific areas.

## Coverage by Package

| Package | Instruction Coverage | Branch Coverage | Status |
|---------|---------------------|-----------------|--------|
| services | 84% | 72% | Good |
| converters | 84% | 78% | Good |
| controllers | 97% | 86% | Excellent |
| utils | 93% | 84% | Excellent |
| pdf.impl | 92% | 79% | Excellent |
| factories | 100% | n/a | Perfect |
| enums | 100% | n/a | Perfect |
| exceptions | 100% | n/a | Perfect |
| adapters.container | 2% | 1% | **Critical Gap** |
| entities | 74% | n/a | Good |
| config | 81% | 33% | **Needs Improvement** |

## Critical Coverage Gaps

### 1. Container Adapters (2% coverage) ⚠️
**Files:**
- `DockerContainerAdapter.java` - Almost no coverage
- `PodmanContainerAdapter.java` - Almost no coverage

**Impact:** High - These are critical infrastructure components
**Recommendation:** Add integration tests for container orchestration

### 2. Config Package (33% branch coverage) ⚠️
**Files:**
- `ContainerOrchestrationConfig.java` - Missing branch coverage
- `ContainerRuntimeConfiguration.java` - Missing branch coverage

**Impact:** Medium - Configuration logic needs validation
**Recommendation:** Add tests for all configuration branches

### 3. Services with Low Coverage

#### FileConversionService (37% instruction, 42% branch)
**Missing Coverage:**
- Exception handling paths
- Edge cases in conversion logic
- ConversionRuntimeException class (0% coverage)

**Recommendation:** Add tests for:
- Exception wrapping/unwrapping
- Error propagation
- Correlation ID generation

#### CSV/TSV Services (59% coverage)
**Missing Coverage:**
- Streaming logic for large files
- Edge cases in parsing
- Error handling for malformed input

**Recommendation:** Already have property tests, but need:
- Tests for streaming threshold logic
- Tests for chunk processing
- Tests for memory-constrained scenarios

#### XlsxToPdfService (8% coverage) ⚠️
#### XlsToPdfService (9% coverage) ⚠️
**Impact:** High - These are commonly used converters
**Recommendation:** Add comprehensive unit tests

#### PageNumberService (85% instruction, 57% branch)
**Missing Coverage:**
- Exception handling in finally blocks
- File replacement failure scenarios
- Edge cases in positioning logic

**Recommendation:** Add tests for:
- Cleanup on exception
- File system errors
- All position/alignment/style combinations

#### WatermarkService (82% instruction, 60% branch)
**Missing Coverage:**
- Exception handling in finally blocks
- Empty watermark text handling
- Layer and orientation combinations

**Recommendation:** Add tests for:
- Cleanup on exception
- Edge cases in watermark placement
- All layer/orientation combinations

#### PdfMergeService (79% instruction, 61% branch)
**Missing Coverage:**
- Exception handling
- Invalid PDF handling
- Position edge cases

**Recommendation:** Add tests for:
- Merge with corrupted PDFs
- File replacement failures
- Both "front" and "back" positions

## Recommended Test Additions

### High Priority (Critical Gaps)

1. **Container Adapter Tests**
   - Mock Docker/Podman CLI interactions
   - Test container lifecycle (start, stop, cleanup)
   - Test error handling for missing runtimes
   - Estimated effort: 4-6 hours

2. **XLS/XLSX Service Tests**
   - Test basic conversion
   - Test with various Excel features (formulas, charts, formatting)
   - Test error handling
   - Estimated effort: 3-4 hours

3. **FileConversionService Exception Tests**
   - Test ConversionRuntimeException usage
   - Test exception unwrapping logic
   - Test correlation ID propagation
   - Estimated effort: 2-3 hours

### Medium Priority (Improve Coverage)

4. **PageNumberService Edge Cases**
   - Test cleanup on exception (try-finally blocks)
   - Test file replacement failures
   - Test all 18 combinations (3 positions × 3 alignments × 2 styles)
   - Estimated effort: 2-3 hours

5. **WatermarkService Edge Cases**
   - Test cleanup on exception
   - Test empty watermark text
   - Test all layer/orientation combinations
   - Estimated effort: 2-3 hours

6. **PdfMergeService Edge Cases**
   - Test with invalid PDFs
   - Test file replacement failures
   - Test both merge positions
   - Estimated effort: 2 hours

7. **CSV/TSV Streaming Tests**
   - Test streaming threshold logic
   - Test chunk processing
   - Test memory usage with large files
   - Estimated effort: 3-4 hours

### Low Priority (Nice to Have)

8. **Property-Based Tests for More Services**
   - Add PBT for Excel services
   - Add PBT for image services
   - Add PBT for CAD services
   - Estimated effort: 6-8 hours

9. **Integration Tests**
   - Full pipeline tests with all features enabled
   - Performance tests with realistic file sizes
   - Stress tests for concurrent conversions
   - Estimated effort: 4-6 hours

## Coverage Goals

### Short Term (Next Sprint)
- **Target:** 85% instruction coverage, 75% branch coverage
- **Focus:** High priority items (1-3)
- **Estimated effort:** 9-13 hours

### Medium Term (Next Month)
- **Target:** 90% instruction coverage, 80% branch coverage
- **Focus:** High + Medium priority items (1-7)
- **Estimated effort:** 20-28 hours

### Long Term (Next Quarter)
- **Target:** 95% instruction coverage, 85% branch coverage
- **Focus:** All items including low priority
- **Estimated effort:** 30-42 hours

## Test Quality Improvements

Beyond coverage numbers, consider:

1. **Property-Based Testing**
   - Already implemented for TSV/CSV parsing
   - Expand to other parsing services
   - Add properties for file operations

2. **Integration Testing**
   - Test full conversion pipelines
   - Test with real-world file samples
   - Test concurrent operations

3. **Performance Testing**
   - Benchmark conversion times
   - Monitor memory usage
   - Test with large files (50MB+)

4. **Security Testing**
   - Test path traversal prevention
   - Test with malicious files
   - Test resource exhaustion scenarios

## Excluded from Coverage

Some code is intentionally not covered:
- Main application class (bootstrap code)
- Configuration classes (Spring-managed)
- DTO classes (data holders)
- Enum classes (simple constants)

These exclusions are appropriate and don't indicate quality issues.

## Conclusion

The current 81% coverage is solid, but there are specific areas that need attention:

1. **Critical:** Container adapters need tests (currently 2%)
2. **Important:** XLS/XLSX services need tests (currently <10%)
3. **Improvement:** Exception handling paths need more coverage
4. **Enhancement:** Edge cases in PDF operations need tests

Focusing on the high-priority items would bring coverage to 85%+ and significantly improve code quality and reliability.
