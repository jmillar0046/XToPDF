package com.xtopdf.xtopdf.services.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Data holder for IGES file metadata.
 */
public class IgesFileData {
    public List<String> startSection = new ArrayList<>();
    public List<String> globalSection = new ArrayList<>();
    public List<String> terminateSection = new ArrayList<>();
    public int directoryEntryCount = 0;
    public int parameterDataCount = 0;
}
