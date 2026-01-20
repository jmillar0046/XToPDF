# Font Resources

This directory contains font files used by the XToPDF application for rendering text in generated PDF documents.

## Fonts Included

### NotoSans (Latin, Greek, Cyrillic)
- **NotoSans-Regular.ttf** - Variable font supporting multiple weights (Thin to Black)
  - This is a variable font that can render text at any weight from Thin (100) to Black (900)
  - File Size: ~2 MB
- **NotoSans-Bold.ttf** - Static Bold weight font for emphasized text
  - File Size: ~28 KB
- **Source**: [Google Fonts - Noto Sans](https://fonts.google.com/noto/specimen/Noto+Sans)
- **Repository**: https://github.com/google/fonts/tree/main/ofl/notosans
- **Character Coverage**: Latin, Greek, Cyrillic, and many other scripts

### NotoSans CJK (Chinese, Japanese, Korean)
- **NotoSansCJK-Regular.otf** - Regular weight font for CJK characters (Simplified Chinese subset)
- **Source**: [Noto CJK Fonts](https://github.com/notofonts/noto-cjk)
- **Repository**: https://github.com/notofonts/noto-cjk
- **Character Coverage**: Chinese (Simplified), Japanese, Korean characters
- **File Size**: ~8 MB
- **Note**: This is a subset font (SC - Simplified Chinese) to reduce file size while maintaining broad CJK coverage

## License

All Noto fonts are licensed under the **SIL Open Font License (OFL) Version 1.1**.

### License Summary
- ✅ **Free to use** for personal and commercial purposes
- ✅ **Can be embedded** in applications and documents
- ✅ **Can be modified** and redistributed
- ❌ **Cannot be sold** by itself
- ❌ **Font name cannot be used** for modified versions without permission

### Full License Text
The complete SIL Open Font License can be found at:
- https://scripts.sil.org/OFL
- https://github.com/google/fonts/blob/main/ofl/notosans/OFL.txt

### Copyright Notice
```
Copyright 2012 Google Inc. All Rights Reserved.

This Font Software is licensed under the SIL Open Font License, Version 1.1.
This license is copied below, and is also available with a FAQ at:
http://scripts.sil.org/OFL
```

## Usage in XToPDF

These fonts are used by the `PdfBoxDocumentBuilder` class to:
1. **Primary Font**: NotoSans-Regular.ttf is used as the default font for rendering text
2. **Bold Text**: NotoSans-Bold.ttf is used for emphasized or bold text
3. **CJK Fallback**: NotoSansCJK-Regular.otf is used as a fallback when text contains Chinese, Japanese, or Korean characters

The font selection mechanism automatically detects character ranges and selects the appropriate font to ensure proper rendering of international text.

## Font Selection Algorithm

The application implements a font fallback mechanism:
1. Attempt to render text with NotoSans-Regular.ttf (primary font)
2. If characters are not supported, fall back to NotoSansCJK-Regular.otf
3. This ensures comprehensive Unicode coverage for most languages

## Requirements Satisfied

These fonts satisfy the following requirements from the Advanced Improvements specification:
- **Requirement 7.1**: Use Unicode-capable fonts for text rendering
- **Requirement 7.2**: Support CJK (Chinese, Japanese, Korean) characters
- **Requirement 7.3**: Support Arabic characters (via NotoSans)
- **Requirement 7.4**: Support Cyrillic characters (via NotoSans)
- **Requirement 7.6**: Embed fonts in generated PDFs

## Additional Information

### Why Noto Fonts?
- **Comprehensive Coverage**: Noto fonts aim to support all languages with a harmonious look and feel
- **Open Source**: Licensed under OFL, free for commercial use
- **Well-Maintained**: Actively maintained by Google and the open source community
- **Production-Ready**: Used by Android, Chrome OS, and many other Google products
- **High Quality**: Professional-grade fonts with excellent rendering quality

### Font File Formats
- **TTF (TrueType Font)**: Used for NotoSans Regular and Bold - widely supported, good for Latin scripts
- **OTF (OpenType Font)**: Used for NotoSans CJK - better support for complex scripts and large character sets

### Future Enhancements
Consider adding additional Noto font variants if needed:
- **Noto Sans Arabic** - For improved Arabic script support
- **Noto Sans Devanagari** - For Hindi and other Indic scripts
- **Noto Sans Thai** - For Thai script
- **Noto Serif** - For serif font option

## Troubleshooting

### Font Loading Issues
If fonts fail to load:
1. Verify font files exist in `src/main/resources/fonts/`
2. Check file permissions are readable
3. Ensure font files are not corrupted (check file sizes match above)
4. Verify classpath includes resources directory

### Character Rendering Issues
If characters appear as boxes or are missing:
1. Verify the character is supported by the font (check Unicode coverage)
2. Ensure font fallback mechanism is working correctly
3. Check that fonts are properly embedded in the PDF
4. Consider adding additional Noto font variants for specific scripts

## References

- [Noto Fonts Project](https://fonts.google.com/noto)
- [Noto Fonts GitHub Organization](https://github.com/notofonts)
- [Google Fonts](https://fonts.google.com/)
- [SIL Open Font License](https://scripts.sil.org/OFL)
- [Unicode Character Database](https://www.unicode.org/ucd/)
