package com.xtopdf.xtopdf.config;

import com.xtopdf.xtopdf.enums.WatermarkLayer;
import com.xtopdf.xtopdf.enums.WatermarkOrientation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatermarkConfig {
    private boolean enabled;
    private String text;
    private float fontSize;
    private WatermarkLayer layer;
    private WatermarkOrientation orientation;
    
    public static WatermarkConfig disabled() {
        return WatermarkConfig.builder()
                .enabled(false)
                .build();
    }
}
