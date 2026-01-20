package com.xtopdf.xtopdf.services;

import java.util.ArrayList;
import java.util.List;

/**
 * Data holder for IGES file metadata.
 */
class IgesFileData {
    List<String> startSection = new ArrayList<>();
    List<String> globalSection = new ArrayList<>();
    List<String> terminateSection = new ArrayList<>();
    int directoryEntryCount = 0;
    int parameterDataCount = 0;
}
