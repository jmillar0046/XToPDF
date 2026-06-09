package com.xtopdf.xtopdf.controllers.graphql;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

/**
 * GraphQL mutation resolver for XToPDF file conversion operations.
 */
@Controller
public class ConversionMutationController {

  /**
   * Submits a file for conversion. In a real implementation, this would accept an Upload scalar
   * and delegate to the FileConversionService. For now, it creates a job record and returns
   * immediately with PENDING status.
   */
  @MutationMapping
  public ConversionResult convertFile(@Argument String fileName, @Argument Long fileSize) {
    String jobId = UUID.randomUUID().toString();
    String submittedAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

    return new ConversionResult(
        jobId, fileName, "PENDING", fileSize, submittedAt, null, null);
  }
}
