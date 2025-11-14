package com.xtopdf.xtopdf.entities;

import lombok.Data;

/**
 * Base class for all DXF/DWG entities.
 */
@Data
public abstract class DxfEntity {
    private String layer = "0"; // Default layer
}
