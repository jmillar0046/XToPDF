package com.xtopdf.xtopdf.services.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Data holder for HPGL/PLT file metadata.
 */
public class HpglFileData {
    public int totalCommands = 0;
    public int penMovements = 0;
    public int drawCommands = 0;
    public int labelCommands = 0;
    public int penSelects = 0;
    public Map<String, Integer> commandTypes = new HashMap<>();
}
