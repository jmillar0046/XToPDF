package com.xtopdf.xtopdf.dto;

import com.xtopdf.xtopdf.enums.WatermarkLayer;
import com.xtopdf.xtopdf.enums.WatermarkOrientation;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatermarkRequest {
    @NotBlank(message = "Watermark text must not be blank")
    private String text;

    @DecimalMin(value = "1", message = "Font size must be at least 1")
    @DecimalMax(value = "200", message = "Font size must be at most 200")
    private Float fontSize;

    private WatermarkLayer layer;
    private WatermarkOrientation orientation;
}
