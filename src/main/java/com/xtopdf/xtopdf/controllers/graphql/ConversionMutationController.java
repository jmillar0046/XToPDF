package com.xtopdf.xtopdf.controllers.graphql;

import com.xtopdf.xtopdf.dto.ConversionJob;
import com.xtopdf.xtopdf.services.JobTrackingService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

/**
 * GraphQL mutation resolver for XToPDF file conversion operations.
 */
@Controller
public class ConversionMutationController {

  private final JobTrackingService jobTrackingService;

  public ConversionMutationController(JobTrackingService jobTrackingService) {
    this.jobTrackingService = jobTrackingService;
  }

  /**
   * Submits a file for conversion. Creates a job record and returns immediately with PENDING status.
   *
   * <p>Note: Actual file conversion requires a file upload which GraphQL doesn't easily support.
   * This mutation creates and tracks the job. The actual conversion must be triggered via the
   * REST endpoint with file upload. This serves as a job submission/tracking entry point.</p>
   */
  @MutationMapping
  public ConversionResult convertFile(@Argument String fileName, @Argument Long fileSize) {
    String outputFileName = fileName.replaceAll("\\.[^.]+$", ".pdf");
    ConversionJob job = jobTrackingService.submit(fileName, outputFileName, null);

    return new ConversionResult(
        job.id(),
        job.inputFileName(),
        "AWAITING_UPLOAD",  // Distinct from PENDING to indicate file must be uploaded via REST
        fileSize,
        job.createdAt() != null ? job.createdAt().toString() : null,
        null,
        "Upload file via POST /api/convert/async to begin conversion");
  }
}
