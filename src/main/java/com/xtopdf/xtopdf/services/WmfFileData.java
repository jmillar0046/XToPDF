package com.xtopdf.xtopdf.services;

/**
 * Data holder for WMF file metadata.
 */
class WmfFileData {
    boolean isPlaceable = false;
    boolean boundsValid = false;
    int boundsLeft = 0;
    int boundsTop = 0;
    int boundsRight = 0;
    int boundsBottom = 0;
    int maxRecordSize = 0;
}
