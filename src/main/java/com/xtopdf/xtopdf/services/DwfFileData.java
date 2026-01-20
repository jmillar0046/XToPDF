package com.xtopdf.xtopdf.services;

import java.util.ArrayList;
import java.util.List;

/**
 * Data holder for DWF file metadata.
 */
class DwfFileData {
    int totalFiles = 0;
    List<String> sections = new ArrayList<>();
    boolean hasDescriptor = false;
}
