package com.xtopdf.xtopdf.services.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Data holder for DWF file metadata.
 */
public class DwfFileData {
    public int totalFiles = 0;
    public List<String> sections = new ArrayList<>();
    public boolean hasDescriptor = false;
}
