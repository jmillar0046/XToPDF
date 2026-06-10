package com.xtopdf.xtopdf.controllers.graphql;

import com.xtopdf.xtopdf.converters.ConverterRegistry;
import com.xtopdf.xtopdf.services.JobTrackingService;
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
  private final JobTrackingService jobTrackingService;

  public ConversionQueryController(ConverterRegistry converterRegistry,
                                   JobTrackingService jobTrackingService) {
    this.converterRegistry = converterRegistry;
    this.jobTrackingService = jobTrackingService;
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
    return jobTrackingService.getStatus(id)
        .map(job -> new ConversionResult(
            job.id(),
            job.inputFileName(),
            job.status().name(),
            null,
            job.createdAt() != null ? job.createdAt().toString() : null,
            job.completedAt() != null ? job.completedAt().toString() : null,
            job.errorMessage()))
        .orElse(null);
  }
}
