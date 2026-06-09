# XToPDF API - cURL Examples

Common cURL commands for interacting with the XToPDF API.

## Base URL

```
http://localhost:8080
```

## File Conversion

### Convert a single file

```bash
curl -X POST http://localhost:8080/api/convert \
  -F "file=@document.docx" \
  -F "outputFileName=document.pdf" \
  -o output.pdf
```

### Convert with specific output name

```bash
curl -X POST http://localhost:8080/api/convert?outputFileName=my-report.pdf \
  -F "file=@report.xlsx" \
  -o my-report.pdf
```

### Convert to JSON response (base64-encoded PDF)

```bash
curl -X POST http://localhost:8080/api/convert/json \
  -F "file=@document.docx" \
  | jq .
```

## Batch Conversion

### Convert multiple files at once

```bash
curl -X POST http://localhost:8080/api/convert/batch \
  -F "files=@document.docx" \
  -F "files=@spreadsheet.xlsx" \
  -F "files=@image.png" \
  -o batch-results.zip
```

## Async Conversion

### Submit an async conversion job

```bash
curl -X POST http://localhost:8080/api/convert/async \
  -F "file=@large-document.docx" \
  -w "\nHTTP Status: %{http_code}\n"
```

Response (HTTP 202):
```json
{
  "jobId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "PENDING"
}
```

### Check job status

```bash
curl -s http://localhost:8080/api/convert/async/550e8400-e29b-41d4-a716-446655440000 \
  | jq .
```

Response:
```json
{
  "jobId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "COMPLETED",
  "inputFileName": "large-document.docx",
  "createdAt": "2025-01-15T10:30:00Z",
  "completedAt": "2025-01-15T10:30:45Z"
}
```

### Download async result

```bash
curl -o result.pdf \
  http://localhost:8080/api/convert/async/550e8400-e29b-41d4-a716-446655440000/result
```

### Submit async with webhook notification

```bash
curl -X POST "http://localhost:8080/api/convert/async?webhookUrl=https://myapp.com/webhook" \
  -F "file=@large-document.docx"
```

## PDF Operations

### Add watermark

```bash
# Foreground watermark
curl -X POST "http://localhost:8080/api/pdf/watermark?text=CONFIDENTIAL&layer=FOREGROUND" \
  -F "file=@document.pdf" \
  -o watermarked.pdf

# Background watermark
curl -X POST "http://localhost:8080/api/pdf/watermark?text=DRAFT&layer=BACKGROUND" \
  -F "file=@document.pdf" \
  -o watermarked.pdf
```

### Add page numbers

```bash
# Bottom center, Arabic numerals (default)
curl -X POST "http://localhost:8080/api/pdf/page-numbers?position=BOTTOM&alignment=CENTER&style=ARABIC" \
  -F "file=@document.pdf" \
  -o numbered.pdf

# Top right, Roman numerals
curl -X POST "http://localhost:8080/api/pdf/page-numbers?position=TOP&alignment=RIGHT&style=ROMAN" \
  -F "file=@document.pdf" \
  -o numbered.pdf

# Bottom left, alphabetic
curl -X POST "http://localhost:8080/api/pdf/page-numbers?position=BOTTOM&alignment=LEFT&style=ALPHABETIC" \
  -F "file=@document.pdf" \
  -o numbered.pdf
```

### Merge PDFs

```bash
# Append overlay to back
curl -X POST "http://localhost:8080/api/pdf/merge?position=back" \
  -F "file=@main.pdf" \
  -F "overlayFile=@appendix.pdf" \
  -o merged.pdf

# Prepend overlay to front
curl -X POST "http://localhost:8080/api/pdf/merge?position=front" \
  -F "file=@main.pdf" \
  -F "overlayFile=@cover-page.pdf" \
  -o merged.pdf
```

## Health & Monitoring

### Health check

```bash
curl -s http://localhost:8080/actuator/health | jq .
```

### Prometheus metrics

```bash
curl -s http://localhost:8080/actuator/prometheus
```

### Application info

```bash
curl -s http://localhost:8080/actuator/info | jq .
```

## Error Handling

All error responses follow a consistent format:

```json
{
  "errorCode": "CONVERSION_ERROR",
  "message": "An error occurred during file conversion",
  "correlationId": "abc123-def456"
}
```

### Common HTTP status codes

| Code | Meaning |
|------|---------|
| 200 | Success |
| 202 | Job accepted (async) |
| 400 | Bad request (invalid input) |
| 413 | File too large (>100MB) |
| 415 | Unsupported file type |
| 429 | Rate limit exceeded (check `Retry-After` header) |
| 500 | Internal server error |

### Rate limit response

```bash
# When rate limited, you'll get:
# HTTP 429 Too Many Requests
# Retry-After: 30
curl -v -X POST http://localhost:8080/api/convert \
  -F "file=@document.docx" 2>&1 | grep -E "< HTTP|Retry-After"
```

## Tips

- Use `-v` for verbose output (see headers)
- Use `| jq .` to pretty-print JSON responses
- Use `-o filename` to save binary PDF output
- Use `-w "\n%{http_code}\n"` to see HTTP status codes
- Maximum file size: 100MB per file, 200MB total request
- Rate limit: 100 requests/minute per IP (default)
