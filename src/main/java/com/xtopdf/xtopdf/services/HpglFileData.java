package com.xtopdf.xtopdf.services;

import java.util.HashMap;
import java.util.Map;

/**
 * Data holder for HPGL/PLT file metadata.
 */
class HpglFileData {
    int totalCommands = 0;
    int penMovements = 0;
    int drawCommands = 0;
    int labelCommands = 0;
    int penSelects = 0;
    Map<String, Integer> commandTypes = new HashMap<>();
}
