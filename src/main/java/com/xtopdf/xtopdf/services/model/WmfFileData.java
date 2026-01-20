package com.xtopdf.xtopdf.services.model;

/**
 * Data holder for WMF file metadata.
 */
public class WmfFileData {
    public boolean isPlaceable = false;
    public boolean boundsValid = false;
    public int boundsLeft = 0;
    public int boundsTop = 0;
    public int boundsRight = 0;
    public int boundsBottom = 0;
    public int maxRecordSize = 0;
}
