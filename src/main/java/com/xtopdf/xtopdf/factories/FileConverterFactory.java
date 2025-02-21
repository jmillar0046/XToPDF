package com.xtopdf.xtopdf.factories;

import com.xtopdf.xtopdf.converters.FileConverter;

public interface FileConverterFactory {
    FileConverter createFileConverter();
}
