package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.config.BatchConfig;
import com.xtopdf.xtopdf.dto.BatchConversionResult;
import com.xtopdf.xtopdf.dto.ConversionParameters;
import com.xtopdf.xtopdf.exceptions.FileConversionException;
import net.jqwik.api.*;
import net.jqwik.api.lifecycle.BeforeProperty;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for batch conversion processing.
 *
 * **Validates: Requirements 10.1, 10.4, 10.5, 10.6**
 */
class BatchConversionPropertyTest {

    private BatchConversionService batchConversionService;
    private FileConversionService fileConversionService;
    private BatchConfig batchConfig;

    @BeforeProperty
    void setup() {
        fileConversionService = mock(FileConversionService.class);
        batchConfig = new BatchConfig();
        ReflectionTestUtils.setField(batchConfig, "maxBatchSize", 10);
        ReflectionTestUtils.setField(batchConfig, "parallelWorkers", 4);
        ReflectionTestUtils.setField(batchConfig, "timeoutPerFileSeconds", 300);
        batchConversionService = new BatchConversionService(
                fileConversionService, batchConfig, "/safe/output/directory");
    }

    /**
     * Property 20: For any batch of files within the size limit,
     * all files in the batch produce a result (success or failure).
     *
     * **Validates: Requirements 10.1, 10.6**
     */
    @Property(tries = 25)
    @Tag("Feature: advanced-improvements, Property 20: Parallel Batch Processing")
    void allFilesInBatchProduceResults(
            @ForAll("batchSizes") int batchSize) throws FileConversionException {
        doNothing().when(fileConversionService).convertFile(any(ConversionParameters.class));

        List<MultipartFile> files = IntStream.range(0, batchSize)
                .mapToObj(i -> new MockMultipartFile(
                        "file" + i, "test" + i + ".docx",
                        "application/octet-stream", "content".getBytes()))
                .map(f -> (MultipartFile) f)
                .toList();

        BatchConversionResult result = batchConversionService.convertBatch(files);

        assertThat(result.totalFiles()).isEqualTo(batchSize);
        assertThat(result.results()).hasSize(batchSize);
        assertThat(result.successCount() + result.failureCount()).isEqualTo(batchSize);
    }

    /**
     * Property 21: Batch size exceeding the maximum is rejected.
     *
     * **Validates: Requirements 10.4**
     */
    @Property(tries = 25)
    @Tag("Feature: advanced-improvements, Property 21: Concurrency Limit Enforcement")
    void batchExceedingMaxSizeIsRejected(
            @ForAll("oversizedBatches") int batchSize) {
        List<MultipartFile> files = IntStream.range(0, batchSize)
                .mapToObj(i -> new MockMultipartFile(
                        "file" + i, "test" + i + ".docx",
                        "application/octet-stream", "content".getBytes()))
                .map(f -> (MultipartFile) f)
                .toList();

        assertThatThrownBy(() -> batchConversionService.convertBatch(files))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Property 22: Individual failures do not prevent other files from being processed.
     *
     * **Validates: Requirements 10.5**
     */
    @Property(tries = 25)
    @Tag("Feature: advanced-improvements, Property 22: Batch Error Isolation")
    void individualFailuresDoNotAffectOtherFiles(
            @ForAll("batchSizes") int batchSize) throws FileConversionException {
        // Make every other file fail
        doAnswer(invocation -> {
            ConversionParameters params = invocation.getArgument(0);
            String outputFile = params.outputFile();
            if (outputFile.contains("test1.pdf") || outputFile.contains("test3.pdf")) {
                throw new FileConversionException("Simulated failure");
            }
            return null;
        }).when(fileConversionService).convertFile(any(ConversionParameters.class));

        List<MultipartFile> files = IntStream.range(0, batchSize)
                .mapToObj(i -> new MockMultipartFile(
                        "file" + i, "test" + i + ".docx",
                        "application/octet-stream", "content".getBytes()))
                .map(f -> (MultipartFile) f)
                .toList();

        BatchConversionResult result = batchConversionService.convertBatch(files);

        // All files should have results regardless of individual failures
        assertThat(result.totalFiles()).isEqualTo(batchSize);
        assertThat(result.results()).hasSize(batchSize);
        // At least some should succeed (those that didn't match the failure pattern)
        if (batchSize > 1) {
            assertThat(result.successCount()).isGreaterThan(0);
        }
    }

    /**
     * Property 23: Batch result status reflects the overall outcome.
     *
     * **Validates: Requirements 10.6**
     */
    @Property(tries = 25)
    @Tag("Feature: advanced-improvements, Property 23: Batch Result Completeness")
    void batchResultStatusReflectsOutcome(
            @ForAll("batchSizes") int batchSize) throws FileConversionException {
        doNothing().when(fileConversionService).convertFile(any(ConversionParameters.class));

        List<MultipartFile> files = IntStream.range(0, batchSize)
                .mapToObj(i -> new MockMultipartFile(
                        "file" + i, "test" + i + ".docx",
                        "application/octet-stream", "content".getBytes()))
                .map(f -> (MultipartFile) f)
                .toList();

        BatchConversionResult result = batchConversionService.convertBatch(files);

        if (result.failureCount() == 0) {
            assertThat(result.status()).isEqualTo("success");
        } else if (result.successCount() == 0) {
            assertThat(result.status()).isEqualTo("failed");
        } else {
            assertThat(result.status()).isEqualTo("partial");
        }
    }

    @Provide
    Arbitrary<Integer> batchSizes() {
        return Arbitraries.integers().between(1, 10);
    }

    @Provide
    Arbitrary<Integer> oversizedBatches() {
        return Arbitraries.integers().between(11, 20);
    }
}
