# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- TSV (Tab-Separated Values) file format support
  - Convert .tsv and .tab files to PDF
  - Support for quoted fields with embedded tabs
  - Support for escaped quotes (double quotes)
  - Automatic normalization of rows with inconsistent column counts
  - Full integration with existing features (page numbers, watermarks, PDF merging)
- Comprehensive test suite for TSV conversion
  - 18 unit tests covering edge cases
  - 5 property-based tests with 100+ iterations each
  - Integration tests with FileConversionService
- Added jqwik library for property-based testing

### Changed
- Updated README.md to include TSV format in supported formats list
- Enhanced TsvToPdfService with detailed logging for better observability

## [Previous Releases]

See [GitHub Releases](https://github.com/jmillar0046/XToPDF/releases) for previous version history.
