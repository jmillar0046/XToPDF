# Troubleshooting Guide

This guide helps you diagnose and resolve common issues with XToPDF.

## Table of Contents

- [File Size and Validation Errors](#file-size-and-validation-errors)
- [Conversion Failures](#conversion-failures)
- [Temporary File Issues](#temporary-file-issues)
- [Memory and Performance Issues](#memory-and-performance-issues)
- [Format-Specific Issues](#format-specific-issues)
- [Advanced Debugging](#advanced-debugging)

## File Size and Validation Errors

### Error: "File size exceeds maximum allowed: 100000000 bytes"

**Symptoms**: API returns 400 error when uploading large files

**Causes**:
- Input file is larger than 100MB (default limit)
- File size validation is too restrictive for your use case

**Solutions**:

1. **Split the file** into smaller chunks:
   ```bash
   # For CSV files
   split -l 50000 large_file.csv chunk_
   ```

2. **Increase the limit** in `application.properties`:
   ```properties
   # Increase to 200MB
   xtopdf.max-file-size=200000000
   ```

3. **Use streaming mode** (automatic for CSV/TSV >10MB):
   - Streaming mode processes files in chunks
   - Reduces memory usage by ~33%
   - Enabled automatically, no configuration needed

### Error: "Line X exceeds maximum length: 1000000"

**Symptoms**: CSV/TSV conversion fails with line length error

**Causes**:
- Malformed CSV/TSV with unclosed quotes
- Extremely long text fields
- Missing delimiters causing multiple rows to merge

**Solutions**:

1. **Check for unclosed quotes**:
   ```bash
   # Count quotes in the file (should be even)
   grep -o '"' file.csv | wc -l
   ```

2. **Validate CSV format**:
   ```bash
   # Use csvlint or similar tool
   csvlint file.csv
   ```

3. **Increase the limit** if legitimate:
   ```properties
   xtopdf.max-line-length=2000000
   ```

4. **Fix malformed data**:
   - Ensure all quotes are properly escaped (`""` for literal quotes)
   - Verify delimiters are consistent
   - Check for embedded newlines in quoted fields

### Error: "Line X exceeds maximum field count: 10000"

**Symptoms**: CSV/TSV conversion fails with too many columns

**Causes**:
- File has more than 10,000 columns
- Incorrect delimiter detection
- Malformed data with extra delimiters

**Solutions**:

1. **Verify the delimiter**:
   ```bash
   # Check first line
   head -1 file.csv
   ```

2. **Count actual columns**:
   ```bash
   # For CSV
   head -1 file.csv | tr ',' '\n' | wc -l
   
   # For TSV
   head -1 file.tsv | tr '\t' '\n' | wc -l
   ```

3. **Increase the limit** if legitimate:
   ```properties
   xtopdf.max-fields=20000
   ```

4. **Fix delimiter issues**:
   - Ensure quoted fields don't contain unescaped delimiters
   - Use proper CSV escaping for special characters

## Conversion Failures

### Error: "Unsupported file format"

**Symptoms**: API returns "Failed to convert file" or "Unsupported file format"

**Causes**:
- File extension not recognized
- File extension doesn't match actual content
- Corrupted file

**Solutions**:

1. **Check file extension**:
   ```bash
   file document.docx
   # Should show: Microsoft Word 2007+
   ```

2. **Verify file is not corrupted**:
   - Try opening in native application
   - Check file size is not 0 bytes
   - Verify file downloaded completely

3. **Rename with correct extension**:
   ```bash
   mv document.doc document.docx
   ```

4. **Check supported formats** in README.md

### Error: "Error converting X to PDF: [specific error]"

**Symptoms**: Conversion fails with format-specific error message

**Causes**: Varies by format (see Format-Specific Issues below)

**Solutions**:

1. **Note the correlation ID** from error response:
   ```json
   {
     "errorCode": "CONVERSION_ERROR",
     "message": "Error converting DOCX to PDF: ...",
     "correlationId": "a1b2c3d4-..."
   }
   ```

2. **Search logs** for detailed error:
   ```bash
   grep "correlationId=a1b2c3d4" logs/application.log
   ```

3. **Check file integrity**:
   - Open file in native application
   - Save a fresh copy
   - Remove complex formatting or embedded objects

4. **Try simpler version**:
   - Remove macros, scripts, or embedded content
   - Simplify formatting
   - Export to newer format version

## Temporary File Issues

### Error: "Failed to delete temporary file"

**Symptoms**: Warning in logs about temporary file cleanup

**Causes**:
- Insufficient disk space
- File permissions issues
- File locked by another process
- Filesystem errors

**Solutions**:

1. **Check disk space**:
   ```bash
   df -h /tmp
   # Should have at least 10% free
   ```

2. **Check temp directory permissions**:
   ```bash
   ls -ld /tmp
   # Should be: drwxrwxrwt
   ```

3. **Manually clean up old temp files**:
   ```bash
   # Find temp files older than 1 day
   find /tmp -name "temp_*" -mtime +1
   
   # Delete them
   find /tmp -name "temp_*" -mtime +1 -delete
   ```

4. **Check for disk errors**:
   ```bash
   # On Linux
   dmesg | grep -i error
   
   # On macOS
   diskutil verifyVolume /
   ```

5. **Monitor temp file count**:
   ```bash
   # Count temp files
   ls /tmp/temp_* 2>/dev/null | wc -l
   ```

### Issue: Disk space filling up

**Symptoms**: Disk space decreasing over time, temp files accumulating

**Causes**:
- Temporary files not being cleaned up
- High conversion volume
- Crashes leaving orphaned files

**Solutions**:

1. **Set up automated cleanup**:
   ```bash
   # Add to crontab (runs daily at 2 AM)
   0 2 * * * find /tmp -name "temp_*" -mtime +1 -delete
   ```

2. **Monitor disk usage**:
   ```bash
   # Check current usage
   du -sh /tmp
   
   # Watch in real-time
   watch -n 5 'du -sh /tmp'
   ```

3. **Configure temp directory**:
   ```properties
   # Use dedicated temp directory
   java.io.tmpdir=/var/xtopdf/temp
   ```

4. **Increase disk space** or move temp directory to larger volume

## Memory and Performance Issues

### Issue: Out of Memory errors

**Symptoms**: Application crashes with `OutOfMemoryError`

**Causes**:
- File too large for available memory
- Streaming mode not enabled
- Memory leak
- Insufficient heap size

**Solutions**:

1. **Increase heap size**:
   ```bash
   java -Xmx4g -jar xtopdf.jar
   # Allocates 4GB heap
   ```

2. **Verify streaming mode** is enabled for large files:
   - Check logs for "Using streaming mode"
   - Streaming threshold is 10MB by default

3. **Monitor memory usage**:
   ```bash
   # On Linux
   ps aux | grep xtopdf
   
   # Or use jconsole
   jconsole
   ```

4. **Check for memory leaks**:
   ```bash
   # Enable GC logging
   java -Xlog:gc* -jar xtopdf.jar
   ```

5. **Reduce file size** or split into smaller chunks

### Issue: Slow conversions

**Symptoms**: Conversions taking longer than expected

**Causes**:
- Large or complex files
- Insufficient system resources
- Streaming mode overhead
- Disk I/O bottleneck

**Solutions**:

1. **Check file size and complexity**:
   ```bash
   ls -lh file.csv
   wc -l file.csv
   ```

2. **Monitor system resources**:
   ```bash
   # CPU and memory
   top
   
   # Disk I/O
   iostat -x 1
   ```

3. **Optimize for large files**:
   - Increase CHUNK_SIZE for faster processing
   - Use SSD for temp directory
   - Increase heap size

4. **Profile the conversion**:
   ```bash
   # Enable profiling
   java -agentlib:hprof=cpu=samples -jar xtopdf.jar
   ```

5. **Consider parallel processing** for batch conversions

## Format-Specific Issues

### DOCX/DOC Issues

**Issue**: Formatting lost or incorrect

**Solutions**:
- Ensure fonts are installed on server
- Simplify complex formatting
- Use newer DOCX format instead of DOC
- Check for embedded objects or macros

**Issue**: "Error processing DOCX file"

**Solutions**:
- Verify file is valid DOCX (not renamed DOC)
- Remove password protection
- Repair file in Microsoft Word
- Check for corrupted embedded images

### XLSX/XLS Issues

**Issue**: Formulas not calculated

**Solutions**:
- Use `executeMacros=true` parameter
- Note: VBA macros cannot be executed
- Pre-calculate formulas in Excel
- Use XLSX format (better formula support)

**Issue**: Large spreadsheets fail

**Solutions**:
- Split into multiple sheets
- Remove unused rows/columns
- Simplify complex formulas
- Use streaming mode (automatic for >10MB)

### CSV/TSV Issues

**Issue**: Unclosed quotes warning

**Solutions**:
- Fix CSV formatting
- Escape quotes properly (`""` for literal quotes)
- Use CSV validation tool
- Check for embedded newlines

**Issue**: Wrong delimiter detected

**Solutions**:
- Verify file extension (.csv vs .tsv)
- Check delimiter consistency
- Use proper file extension
- Manually specify delimiter (future feature)

### Image Issues

**Issue**: Image quality poor

**Solutions**:
- Use higher resolution source images
- Avoid multiple conversions (quality loss)
- Use PNG for graphics, JPEG for photos
- Check DPI settings

**Issue**: Large images cause memory errors

**Solutions**:
- Resize images before conversion
- Increase heap size
- Use JPEG instead of PNG (smaller)
- Compress images

### CAD Issues

**Issue**: DWG conversion fails

**Solutions**:
- Verify DWG version is supported
- Convert to DXF first
- Check for corrupted file
- Simplify complex drawings

**Issue**: Missing layers or objects

**Solutions**:
- Ensure all layers are visible
- Check for frozen or locked layers
- Verify object types are supported
- Export to newer DXF version

## Advanced Debugging

### Enable Debug Logging

Add to `application.properties`:

```properties
logging.level.com.xtopdf=DEBUG
logging.level.org.apache.pdfbox=DEBUG
logging.level.org.apache.poi=DEBUG
```

### Capture Full Stack Traces

All errors include full stack traces in logs. Search by correlation ID:

```bash
grep -A 50 "correlationId=YOUR_ID" logs/application.log
```

### Test with Minimal File

Create a minimal test file to isolate the issue:

```bash
# Minimal CSV
echo "A,B,C" > test.csv
echo "1,2,3" >> test.csv

# Minimal TSV
echo -e "A\tB\tC" > test.tsv
echo -e "1\t2\t3" >> test.tsv
```

### Check Dependencies

Verify all required libraries are present:

```bash
java -jar xtopdf.jar --version
```

### Network and Firewall Issues

If running in container or remote server:

```bash
# Test connectivity
curl http://localhost:8080/actuator/health

# Check firewall
sudo iptables -L
```

### Performance Profiling

Use Java profiling tools:

```bash
# JProfiler
java -agentpath:/path/to/jprofiler/bin/linux-x64/libjprofilerti.so -jar xtopdf.jar

# VisualVM
jvisualvm
```

## Getting Additional Help

If you can't resolve the issue:

1. **Search existing issues**: [GitHub Issues](https://github.com/jmillar0046/XToPDF/issues)

2. **Create a new issue** with:
   - Error message and correlation ID
   - File type and size
   - Steps to reproduce
   - Relevant log excerpts (with sensitive data removed)
   - System information (OS, Java version, memory)

3. **Join discussions**: [GitHub Discussions](https://github.com/jmillar0046/XToPDF/discussions)

4. **Check documentation**:
   - [README.md](README.md) - General usage
   - [MONITORING.md](MONITORING.md) - Monitoring and metrics
   - [CONTRIBUTING.md](CONTRIBUTING.md) - Development guidelines

## Common Log Messages

### INFO Messages

```
Successfully converted TSV to PDF: data.tsv -> data.pdf (15000 rows)
```
✅ Normal - Conversion completed successfully

```
Using streaming mode for large file: 15000000 bytes
```
✅ Normal - Streaming mode enabled for large file

### WARN Messages

```
Unclosed quote in line 42, treating as literal
```
⚠️ Warning - CSV/TSV has formatting issue but conversion continues

```
Failed to delete temporary file: /tmp/temp_12345.pdf
```
⚠️ Warning - Temp file cleanup failed, may need manual cleanup

### ERROR Messages

```
File conversion error [correlationId=...]: Line 100 exceeds maximum length
```
❌ Error - Validation failed, check file format

```
I/O error [correlationId=...]: No space left on device
```
❌ Error - Disk space issue, free up space

```
Unexpected error [correlationId=...]: NullPointerException
```
❌ Error - Internal error, report with correlation ID

## Prevention Best Practices

1. **Validate files before conversion**
   - Check file size
   - Verify format
   - Test with small sample

2. **Monitor system resources**
   - Disk space
   - Memory usage
   - Temp file count

3. **Set up automated cleanup**
   - Cron job for temp files
   - Log rotation
   - Disk space alerts

4. **Use appropriate limits**
   - Configure MAX_FILE_SIZE for your use case
   - Adjust streaming threshold
   - Set reasonable timeouts

5. **Keep logs for debugging**
   - Enable structured logging
   - Retain correlation IDs
   - Archive old logs

6. **Test in staging first**
   - Validate with real files
   - Load test with expected volume
   - Monitor performance metrics
