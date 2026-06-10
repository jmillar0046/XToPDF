# API Deprecation Policy

## Overview

XToPDF follows a structured API versioning and deprecation policy to ensure backward compatibility while enabling evolution of the API.

## Current API Versions

| Version | Status | Base Path |
|---------|--------|-----------|
| v1      | Active | `/v1/api/` |
| (none)  | Deprecated | `/api/` |

## Versioning Scheme

API versions use a simple numeric prefix: `/v1/`, `/v2/`, etc. Only major breaking changes trigger a new version.

## Deprecation Timeline

When an API version or endpoint is deprecated:

1. **Announcement** — Deprecation headers are added to all responses from deprecated endpoints.
2. **Grace Period (6 months)** — Deprecated endpoints continue to function normally with deprecation warnings.
3. **Sunset** — After the grace period, deprecated endpoints may be removed.

## Deprecation Headers

Responses from deprecated endpoints include the following headers:

| Header | Description | Example |
|--------|-------------|---------|
| `Deprecation` | Indicates the endpoint is deprecated | `true` |
| `Sunset` | Date when the endpoint will be removed | `2025-06-15` |
| `Link` | Points to the versioned successor endpoint | `</v1/api/convert>; rel="successor-version"` |

## Migration Guide

To migrate from non-versioned to versioned endpoints, prefix all API paths with `/v1`:

| Old Path | New Path |
|----------|----------|
| `POST /api/convert` | `POST /v1/api/convert` |
| `POST /api/convert/json` | `POST /v1/api/convert/json` |
| `POST /api/convert/batch` | `POST /v1/api/convert/batch` |
| `POST /api/convert/async` | `POST /v1/api/convert/async` |
| `GET /api/convert/async/{jobId}` | `GET /v1/api/convert/async/{jobId}` |
| `POST /api/pdf/merge` | `POST /v1/api/pdf/merge` |
| `POST /api/pdf/add-page-numbers` | `POST /v1/api/pdf/add-page-numbers` |
| `POST /api/pdf/add-watermark` | `POST /v1/api/pdf/add-watermark` |

## Backward Compatibility

Within a major version (e.g., v1), the following guarantees hold:

- No existing fields will be removed from responses
- No required request parameters will be added
- Existing endpoints will not be removed
- Response status codes will not change for the same input

Additive changes (new optional fields, new endpoints) are allowed within a major version.

## Support Policy

- **Active version**: Full support, bug fixes, and new features
- **Deprecated version**: Security fixes only during the grace period
- **Sunset version**: No support, endpoints may return HTTP 410 Gone
