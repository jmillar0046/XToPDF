package com.xtopdf.xtopdf.services.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Data holder for STEP file metadata.
 */
public class StepFileData {
    public List<String> header = new ArrayList<>();
    public List<String> entities = new ArrayList<>();
    public int entityCount = 0;
}
