package com.xtopdf.xtopdf.services.conversion.threed;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import com.xtopdf.xtopdf.services.conversion.threed.StepToPdfService.StepMetadata;
import net.jqwik.api.*;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for StepToPdfService structured metadata extraction.
 *
 * Validates correctness properties from the CAD converter rendering spec:
 * - Property 8: STEP Header Extraction
 * - Property 9: STEP Entity Counting Accuracy
 * - Property 10: STEP Assembly Tree Integrity
 * - Property 11: STEP Empty File Handling
 */
class StepToPdfServicePropertyTest {

    private final PdfBackendProvider mockBackend;
    private final PdfDocumentBuilder mockBuilder;
    private final StepToPdfService service;

    StepToPdfServicePropertyTest() throws IOException {
        mockBackend = Mockito.mock(PdfBackendProvider.class);
        mockBuilder = Mockito.mock(PdfDocumentBuilder.class);
        Mockito.when(mockBackend.createBuilder()).thenReturn(mockBuilder);
        service = new StepToPdfService(mockBackend);
    }

    // ---------------------------------------------------------------
    // Property 8: STEP Header Extraction
    // ---------------------------------------------------------------

    @Property(tries = 25)
    @Tag("Feature: cad-converter-rendering, Property: STEP header extraction preserves all three fields")
    void headerExtractionPreservesAllThreeFields(
            @ForAll("headerDescriptions") String description,
            @ForAll("headerFileNames") String fileName,
            @ForAll("headerSchemas") String schema) throws IOException {

        String content = buildStepWithHeader(description, fileName, schema);
        StepMetadata metadata = service.parseStepContent(content);

        assertThat(metadata.fileDescription)
                .as("FILE_DESCRIPTION should be extracted")
                .contains(description);
        assertThat(metadata.fileName)
                .as("FILE_NAME should be extracted")
                .contains(fileName);
        assertThat(metadata.fileSchema)
                .as("FILE_SCHEMA should be extracted")
                .contains(schema);
    }

    // ---------------------------------------------------------------
    // Property 9: STEP Entity Counting Accuracy
    // ---------------------------------------------------------------

    @Property(tries = 25)
    @Tag("Feature: cad-converter-rendering, Property: STEP entity counting equals number of entity lines")
    void entityCountingEqualsNumberOfEntityLines(
            @ForAll("entityCounts") int entityCount) throws IOException {

        String content = buildStepWithNEntities(entityCount);
        StepMetadata metadata = service.parseStepContent(content);

        assertThat(metadata.totalEntityCount)
                .as("Total entity count should equal number of entity lines")
                .isEqualTo(entityCount);
    }

    // ---------------------------------------------------------------
    // Property 10: STEP Assembly Tree Integrity
    // ---------------------------------------------------------------

    @Property(tries = 25)
    @Tag("Feature: cad-converter-rendering, Property: STEP assembly tree contains all nodes with depth capped at 10")
    void assemblyTreeContainsAllNodesWithDepthCap(
            @ForAll("assemblyDepths") int depth) throws IOException {

        String content = buildStepWithAssemblyChain(depth);
        StepMetadata metadata = service.parseStepContent(content);

        // All nodes should be parsed regardless of depth
        assertThat(metadata.assemblyNodes)
                .as("All assembly nodes should be parsed")
                .hasSize(depth);

        // Verify all parent-child pairs are present
        for (int i = 0; i < depth; i++) {
            int parentId = i + 1;
            int childId = i + 2;
            assertThat(metadata.assemblyNodes.get(i).parentRef())
                    .isEqualTo(String.valueOf(parentId));
            assertThat(metadata.assemblyNodes.get(i).childRef())
                    .isEqualTo(String.valueOf(childId));
        }

        // Verify rendering does not throw (depth cap prevents stack overflow)
        MockMultipartFile file = new MockMultipartFile(
                "file", "assembly.step", "application/octet-stream", content.getBytes());
        File pdfFile = Files.createTempFile("step-prop-", ".pdf").toFile();
        pdfFile.deleteOnExit();

        assertThatNoException()
                .as("Rendering should not throw even with deep assembly trees")
                .isThrownBy(() -> service.convertStepToPdf(file, pdfFile));
    }

    // ---------------------------------------------------------------
    // Property 11: STEP Empty File Handling
    // ---------------------------------------------------------------

    @Property(tries = 25)
    @Tag("Feature: cad-converter-rendering, Property: STEP empty DATA section produces informational message")
    void emptyDataSectionProducesInformationalMessage(
            @ForAll("emptyStepVariants") String content) throws IOException {

        StepMetadata metadata = service.parseStepContent(content);

        assertThat(metadata.totalEntityCount)
                .as("Empty DATA section should have zero entities")
                .isEqualTo(0);

        // Verify the conversion produces a PDF with the informational message
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.step", "application/octet-stream", content.getBytes());
        File pdfFile = Files.createTempFile("step-empty-", ".pdf").toFile();
        pdfFile.deleteOnExit();

        // Reset mock for this call
        Mockito.reset(mockBackend, mockBuilder);
        Mockito.when(mockBackend.createBuilder()).thenReturn(mockBuilder);
        service.convertStepToPdf(file, pdfFile);

        verify(mockBuilder).addFormattedText(
                contains("could not be analyzed"), anyBoolean(), anyBoolean(), anyFloat());
    }

    // ---------------------------------------------------------------
    // Generators
    // ---------------------------------------------------------------

    @Provide
    Arbitrary<String> headerDescriptions() {
        return Arbitraries.of(
                "A mechanical assembly",
                "Test model",
                "Automotive part design",
                "Simple bracket",
                "Multi-body assembly",
                "Sheet metal part",
                "Parametric model",
                "Imported geometry"
        );
    }

    @Provide
    Arbitrary<String> headerFileNames() {
        return Arbitraries.of(
                "assembly.step",
                "bracket.stp",
                "gear_box.step",
                "housing_v2.step",
                "motor_mount.stp",
                "part_001.step",
                "model.step",
                "design_final.stp"
        );
    }

    @Provide
    Arbitrary<String> headerSchemas() {
        return Arbitraries.of(
                "AUTOMOTIVE_DESIGN",
                "AP214",
                "AP203",
                "CONFIG_CONTROL_DESIGN",
                "AP242",
                "STRUCTURAL_FRAME_SCHEMA"
        );
    }

    @Provide
    Arbitrary<Integer> entityCounts() {
        return Arbitraries.integers().between(1, 50);
    }

    @Provide
    Arbitrary<Integer> assemblyDepths() {
        return Arbitraries.integers().between(1, 15);
    }

    @Provide
    Arbitrary<String> emptyStepVariants() {
        return Arbitraries.of(
                "",
                "ISO-10303-21;\nEND-ISO-10303-21;",
                "ISO-10303-21;\nHEADER;\nFILE_DESCRIPTION(('empty'),'2;1');\nENDSEC;\nEND-ISO-10303-21;",
                "ISO-10303-21;\nHEADER;\nENDSEC;\nDATA;\nENDSEC;\nEND-ISO-10303-21;",
                "ISO-10303-21;\nHEADER;\nFILE_NAME('empty.step','2024-01-01');\nENDSEC;\nDATA;\nENDSEC;\nEND-ISO-10303-21;",
                "HEADER;\nENDSEC;\nDATA;\nENDSEC;",
                "random text with no step structure"
        );
    }

    // ---------------------------------------------------------------
    // Content Builders
    // ---------------------------------------------------------------

    private String buildStepWithHeader(String description, String fileName, String schema) {
        return "ISO-10303-21;\n" +
                "HEADER;\n" +
                "FILE_DESCRIPTION(('" + description + "'),'2;1');\n" +
                "FILE_NAME('" + fileName + "','2024-06-01');\n" +
                "FILE_SCHEMA(('" + schema + "'));\n" +
                "ENDSEC;\n" +
                "DATA;\n" +
                "#1=CARTESIAN_POINT('',(0.,0.,0.));\n" +
                "ENDSEC;\n" +
                "END-ISO-10303-21;\n";
    }

    private String buildStepWithNEntities(int count) {
        StringBuilder sb = new StringBuilder();
        sb.append("ISO-10303-21;\nHEADER;\nENDSEC;\nDATA;\n");
        for (int i = 1; i <= count; i++) {
            sb.append("#").append(i).append("=CARTESIAN_POINT('',(")
              .append(i).append(".,0.,0.));\n");
        }
        sb.append("ENDSEC;\nEND-ISO-10303-21;\n");
        return sb.toString();
    }

    private String buildStepWithAssemblyChain(int depth) {
        StringBuilder sb = new StringBuilder();
        sb.append("ISO-10303-21;\nHEADER;\nENDSEC;\nDATA;\n");
        for (int i = 1; i <= depth; i++) {
            sb.append("#").append(100 + i)
              .append("=NEXT_ASSEMBLY_USAGE_OCCURRENCE('n").append(i)
              .append("','Level").append(i).append("','',#")
              .append(i).append(",#").append(i + 1).append(",$);\n");
        }
        sb.append("ENDSEC;\nEND-ISO-10303-21;\n");
        return sb.toString();
    }
}
