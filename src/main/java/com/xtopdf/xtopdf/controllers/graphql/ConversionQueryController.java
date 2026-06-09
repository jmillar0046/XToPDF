package com.xtopdf.xtopdf.controllers.graphql;

import com.xtopdf.xtopdf.converters.ConverterRegistry;
import java.util.List;
import java.util.Set;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

/**
 * GraphQL query resolver for XToPDF conversion queries.
 */
@Controller
public class ConversionQueryController {

  private final ConverterRegistry converterRegistry;

  public ConversionQueryController(ConverterRegistry converterRegistry) {
    this.converterRegistry = converterRegistry;
  }

  /**
   * Returns all supported file format extensions.
   */
  @QueryMapping
  public List<String> supportedFormats() {
    Set<String> extensions = converterRegistry.getSupportedExtensions();
    return extensions.stream().sorted().toList();
  }

  /**
   * Returns the status of a conversion job by ID. Returns null if not found.
   */
  @QueryMapping
  public ConversionResult conversionJob(@Argument String id) {
    // Placeholder: In a full implementation this would look up job status
    // from the JobTrackingService. For now return null (not found).
    return null;
  }
}
