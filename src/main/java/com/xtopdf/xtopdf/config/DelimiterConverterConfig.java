package com.xtopdf.xtopdf.config;

import com.xtopdf.xtopdf.converters.DelimiterSeparatedConverter;
import com.xtopdf.xtopdf.converters.FileConverter;
import com.xtopdf.xtopdf.services.conversion.spreadsheet.DelimiterSeparatedToPdfService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * Configuration class that registers delimiter-separated file converter beans.
 * Creates two instances of DelimiterSeparatedConverter: one for CSV and one for TSV.
 */
@Configuration
public class DelimiterConverterConfig {

    @Bean
    public FileConverter csvConverter(DelimiterSeparatedToPdfService service) {
        return new DelimiterSeparatedConverter(service, ',', "CSV", Set.of(".csv"));
    }

    @Bean
    public FileConverter tsvConverter(DelimiterSeparatedToPdfService service) {
        return new DelimiterSeparatedConverter(service, '\t', "TSV", Set.of(".tsv", ".tab"));
    }
}
