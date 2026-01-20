package com.xtopdf.xtopdf.services;

import java.util.ArrayList;
import java.util.List;

/**
 * Data holder for STEP file metadata.
 */
class StepFileData {
    List<String> header = new ArrayList<>();
    List<String> entities = new ArrayList<>();
    int entityCount = 0;
}
