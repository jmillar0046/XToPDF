package com.xtopdf.xtopdf.controllers.graphql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.xtopdf.xtopdf.converters.ConverterRegistry;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for GraphQL query and mutation resolvers.
 */
class GraphQLResolverTest {

  private ConverterRegistry converterRegistry;
  private ConversionQueryController queryController;
  private ConversionMutationController mutationController;

  @BeforeEach
  void setup() {
    converterRegistry = mock(ConverterRegistry.class);
    var jobTrackingService = new com.xtopdf.xtopdf.services.JobTrackingService();
    queryController = new ConversionQueryController(converterRegistry, jobTrackingService);
    mutationController = new ConversionMutationController(jobTrackingService);
  }

  @Test
  void supportedFormatsReturnsAllRegisteredExtensions() {
    when(converterRegistry.getSupportedExtensions())
        .thenReturn(Set.of(".pdf", ".docx", ".xlsx", ".png", ".jpg"));

    List<String> result = queryController.supportedFormats();

    assertThat(result).hasSize(5);
    assertThat(result).contains(".pdf", ".docx", ".xlsx", ".png", ".jpg");
  }

  @Test
  void supportedFormatsReturnsSortedList() {
    when(converterRegistry.getSupportedExtensions())
        .thenReturn(Set.of(".xlsx", ".docx", ".png", ".bmp", ".txt"));

    List<String> result = queryController.supportedFormats();

    assertThat(result).isSorted();
  }

  @Test
  void conversionJobReturnsNullForUnknownId() {
    ConversionResult result = queryController.conversionJob("unknown-id");

    assertThat(result).isNull();
  }

  @Test
  void convertFileMutationReturnsPendingJob() {
    ConversionResult result = mutationController.convertFile("document.docx", 1024);

    assertThat(result).isNotNull();
    assertThat(result.id()).isNotNull().isNotEmpty();
    assertThat(result.fileName()).isEqualTo("document.docx");
    assertThat(result.status()).isEqualTo("AWAITING_UPLOAD");
    assertThat(result.fileSize()).isEqualTo(1024);
    assertThat(result.submittedAt()).isNotNull();
    assertThat(result.completedAt()).isNull();
  }

  @Test
  void convertFileMutationGeneratesUniqueJobIds() {
    ConversionResult result1 = mutationController.convertFile("file1.docx", 100);
    ConversionResult result2 = mutationController.convertFile("file2.xlsx", 200);

    assertThat(result1.id()).isNotEqualTo(result2.id());
  }
}
