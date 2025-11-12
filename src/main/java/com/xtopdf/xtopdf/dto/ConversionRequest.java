package com.xtopdf.xtopdf.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversionRequest {
    private String outputFile;
    private Boolean executeMacros;
    private PageNumberRequest pageNumbers;
    private WatermarkRequest watermark;
    private MergeRequest merge;
}
