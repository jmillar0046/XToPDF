package com.xtopdf.xtopdf.config;

import com.xtopdf.xtopdf.enums.PageNumberAlignment;
import com.xtopdf.xtopdf.enums.PageNumberPosition;
import com.xtopdf.xtopdf.enums.PageNumberStyle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageNumberConfig {
    private boolean enabled;
    private PageNumberPosition position;
    private PageNumberAlignment alignment;
    private PageNumberStyle style;
    
    public static PageNumberConfig disabled() {
        return PageNumberConfig.builder()
                .enabled(false)
                .build();
    }
}
