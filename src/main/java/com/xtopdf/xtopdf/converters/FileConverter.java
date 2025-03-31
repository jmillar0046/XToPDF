package com.xtopdf.xtopdf.converters;

import java.io.File;

public interface FileConverter {
    void convertToPDF(File inputFile, String outputFile);
}
