package com.xtopdf.xtopdf.dto;

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
public class WatermarkRequest {
    private String text;
    private Float fontSize;
    private WatermarkLayer layer;
    private WatermarkOrientation orientation;
}
