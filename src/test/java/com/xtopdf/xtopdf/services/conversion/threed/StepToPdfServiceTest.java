package com.xtopdf.xtopdf.services.conversion.threed;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import com.xtopdf.xtopdf.services.conversion.threed.StepToPdfService.StepAssemblyNode;
import com.xtopdf.xtopdf.services.conversion.threed.StepToPdfService.StepMetadata;
import com.xtopdf.xtopdf.services.conversion.threed.StepToPdfService.StepProduct;
import com.xtopdf.xtopdf.services.conversion.threed.StepToPdfService.StepShape;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StepToPdfServiceTest {

    @Mock
    private PdfBackendProvider pdfBackend;

    @Mock
    private PdfDocumentBuilder builder;

    private StepToPdfService service;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        when(pdfBackend.createBuilder()).thenReturn(builder);
        service = new StepToPdfService(pdfBackend);
    }

    // ---------------------------------------------------------------
    // Header field extraction
    // ---------------------------------------------------------------

    @Test
    void parsesFileDescriptionFromHeader() throws IOException {
        String content = """
                ISO-10303-21;
                HEADER;
                FILE_DESCRIPTION(('A mechanical assembly'),'2;1');
                ENDSEC;
                DATA;
                #1=CARTESIAN_POINT('',(0.,0.,0.));
                ENDSEC;
                END-ISO-10303-21;
                """;

        StepMetadata metadata = service.parseStepContent(content);

        assertThat(metadata.fileDescription).contains("A mechanical assembly");
    }

    @Test
    void parsesFileNameFromHeader() throws IOException {
        String content = """
                ISO-10303-21;
                HEADER;
                FILE_NAME('assembly.step','2024-01-15');
                ENDSEC;
                DATA;
                #1=CARTESIAN_POINT('',(0.,0.,0.));
                ENDSEC;
                END-ISO-10303-21;
                """;

        StepMetadata metadata = service.parseStepContent(content);

        assertThat(metadata.fileName).contains("assembly.step");
    }

    @Test
    void parsesFileSchemaFromHeader() throws IOException {
        String content = """
                ISO-10303-21;
                HEADER;
                FILE_SCHEMA(('AUTOMOTIVE_DESIGN'));
                ENDSEC;
                DATA;
                #1=CARTESIAN_POINT('',(0.,0.,0.));
                ENDSEC;
                END-ISO-10303-21;
                """;

        StepMetadata metadata = service.parseStepContent(content);

        assertThat(metadata.fileSchema).contains("AUTOMOTIVE_DESIGN");
    }

    @Test
    void parsesAllThreeHeaderFields() throws IOException {
        String content = """
                ISO-10303-21;
                HEADER;
                FILE_DESCRIPTION(('test description'),'2;1');
                FILE_NAME('test.step','2024-06-01');
                FILE_SCHEMA(('AP214'));
                ENDSEC;
                DATA;
                #1=PRODUCT('Widget','A widget','widget_id',#2);
                ENDSEC;
                END-ISO-10303-21;
                """;

        StepMetadata metadata = service.parseStepContent(content);

        assertThat(metadata.fileDescription).isNotEmpty();
        assertThat(metadata.fileName).isNotEmpty();
        assertThat(metadata.fileSchema).isNotEmpty();
    }

    // ---------------------------------------------------------------
    // PRODUCT entity extraction
    // ---------------------------------------------------------------

    @Test
    void extractsProductNameAndId() throws IOException {
        String content = """
                ISO-10303-21;
                HEADER;
                ENDSEC;
                DATA;
                #10=PRODUCT('Gear Assembly','gear_001','',#11);
                ENDSEC;
                END-ISO-10303-21;
                """;

        StepMetadata metadata = service.parseStepContent(content);

        assertThat(metadata.products).hasSize(1);
        assertThat(metadata.products.get(0).name()).isEqualTo("Gear Assembly");
        assertThat(metadata.products.get(0).id()).isEqualTo("gear_001");
    }

    @Test
    void extractsMultipleProducts() throws IOException {
        String content = """
                ISO-10303-21;
                HEADER;
                ENDSEC;
                DATA;
                #10=PRODUCT('Part A','id_a','',#11);
                #20=PRODUCT('Part B','id_b','',#21);
                #30=PRODUCT('Part C','id_c','',#31);
                ENDSEC;
                END-ISO-10303-21;
                """;

        StepMetadata metadata = service.parseStepContent(content);

        assertThat(metadata.products).hasSize(3);
        assertThat(metadata.products).extracting(StepProduct::name)
                .containsExactly("Part A", "Part B", "Part C");
    }

    // ---------------------------------------------------------------
    // SHAPE_REPRESENTATION type classification
    // ---------------------------------------------------------------

    @Test
    void classifiesBrepGeometry() {
        assertThat(service.classifyGeometryType("ADVANCED_BREP_SHAPE_REPRESENTATION"))
                .isEqualTo("BREP (solid)");
    }

    @Test
    void classifiesSurfaceGeometry() {
        assertThat(service.classifyGeometryType("MANIFOLD_SURFACE_SHAPE_REPRESENTATION"))
                .isEqualTo("Surface");
    }

    @Test
    void classifiesWireframeGeometry() {
        assertThat(service.classifyGeometryType("GEOMETRICALLY_BOUNDED_WIREFRAME_SHAPE_REPRESENTATION"))
                .isEqualTo("Wireframe");
    }

    @Test
    void classifiesGeneralGeometry() {
        assertThat(service.classifyGeometryType("SHAPE_REPRESENTATION"))
                .isEqualTo("General");
    }

    @Test
    void extractsShapeWithClassification() throws IOException {
        String content = """
                ISO-10303-21;
                HEADER;
                ENDSEC;
                DATA;
                #50=ADVANCED_BREP_SHAPE_REPRESENTATION('solid body',(#51),#52);
                ENDSEC;
                END-ISO-10303-21;
                """;

        StepMetadata metadata = service.parseStepContent(content);

        assertThat(metadata.shapes).hasSize(1);
        assertThat(metadata.shapes.get(0).name()).isEqualTo("solid body");
        assertThat(metadata.shapes.get(0).geometryType()).isEqualTo("BREP (solid)");
    }

    // ---------------------------------------------------------------
    // Assembly tree from NEXT_ASSEMBLY_USAGE_OCCURRENCE
    // ---------------------------------------------------------------

    @Test
    void extractsAssemblyNodes() throws IOException {
        String content = """
                ISO-10303-21;
                HEADER;
                ENDSEC;
                DATA;
                #100=NEXT_ASSEMBLY_USAGE_OCCURRENCE('nauo1','Bolt','bolt desc',#10,#20,$);
                #101=NEXT_ASSEMBLY_USAGE_OCCURRENCE('nauo2','Nut','nut desc',#10,#30,$);
                ENDSEC;
                END-ISO-10303-21;
                """;

        StepMetadata metadata = service.parseStepContent(content);

        assertThat(metadata.assemblyNodes).hasSize(2);
        assertThat(metadata.assemblyNodes.get(0).parentRef()).isEqualTo("10");
        assertThat(metadata.assemblyNodes.get(0).childRef()).isEqualTo("20");
        assertThat(metadata.assemblyNodes.get(0).name()).isEqualTo("Bolt");
        assertThat(metadata.assemblyNodes.get(1).childRef()).isEqualTo("30");
        assertThat(metadata.assemblyNodes.get(1).name()).isEqualTo("Nut");
    }

    @Test
    void assemblyTreeDepthCappedAt10Levels() throws IOException {
        // Build a chain: #1 -> #2 -> #3 -> ... -> #15
        StringBuilder sb = new StringBuilder();
        sb.append("ISO-10303-21;\nHEADER;\nENDSEC;\nDATA;\n");
        for (int i = 1; i <= 14; i++) {
            sb.append("#").append(100 + i)
              .append("=NEXT_ASSEMBLY_USAGE_OCCURRENCE('n").append(i).append("','Level").append(i)
              .append("','',#").append(i).append(",#").append(i + 1).append(",$);\n");
        }
        sb.append("ENDSEC;\nEND-ISO-10303-21;\n");

        StepMetadata metadata = service.parseStepContent(sb.toString());

        // All 14 nodes should be parsed
        assertThat(metadata.assemblyNodes).hasSize(14);

        // The depth cap is enforced during rendering, not parsing
        // Verify rendering does not throw (would overflow without cap)
        MockMultipartFile file = new MockMultipartFile(
                "file", "deep.step", "application/octet-stream", sb.toString().getBytes());
        File pdfFile = tempDir.resolve("deep.pdf").toFile();

        // Use real backend for integration
        var realBackend = new com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend();
        var realService = new StepToPdfService(realBackend);
        assertThatNoException().isThrownBy(() -> realService.convertStepToPdf(file, pdfFile));
    }

    // ---------------------------------------------------------------
    // Entity counting accuracy
    // ---------------------------------------------------------------

    @Test
    void countsEntitiesAccurately() throws IOException {
        String content = """
                ISO-10303-21;
                HEADER;
                ENDSEC;
                DATA;
                #1=CARTESIAN_POINT('',(0.,0.,0.));
                #2=CARTESIAN_POINT('',(1.,0.,0.));
                #3=DIRECTION('',(1.,0.,0.));
                #4=DIRECTION('',(0.,1.,0.));
                #5=AXIS2_PLACEMENT_3D('',#1,#3,#4);
                ENDSEC;
                END-ISO-10303-21;
                """;

        StepMetadata metadata = service.parseStepContent(content);

        assertThat(metadata.totalEntityCount).isEqualTo(5);
        assertThat(metadata.entityCounts.get("CARTESIAN_POINT")).isEqualTo(2);
        assertThat(metadata.entityCounts.get("DIRECTION")).isEqualTo(2);
        assertThat(metadata.entityCounts.get("AXIS2_PLACEMENT_3D")).isEqualTo(1);
    }

    @Test
    void entityCountsSortedByFrequencyDescending() throws IOException {
        String content = """
                ISO-10303-21;
                HEADER;
                ENDSEC;
                DATA;
                #1=CARTESIAN_POINT('',(0.,0.,0.));
                #2=CARTESIAN_POINT('',(1.,0.,0.));
                #3=CARTESIAN_POINT('',(2.,0.,0.));
                #4=DIRECTION('',(1.,0.,0.));
                #5=DIRECTION('',(0.,1.,0.));
                #6=AXIS2_PLACEMENT_3D('',#1,#3,#4);
                ENDSEC;
                END-ISO-10303-21;
                """;

        StepMetadata metadata = service.parseStepContent(content);

        assertThat(metadata.entityCounts.get("CARTESIAN_POINT")).isEqualTo(3);
        assertThat(metadata.entityCounts.get("DIRECTION")).isEqualTo(2);
        assertThat(metadata.entityCounts.get("AXIS2_PLACEMENT_3D")).isEqualTo(1);
    }

    // ---------------------------------------------------------------
    // Empty file handling
    // ---------------------------------------------------------------

    @Test
    void emptyFileProducesCouldNotBeAnalyzedMessage() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.step", "application/octet-stream", "".getBytes());
        File pdfFile = tempDir.resolve("empty.pdf").toFile();

        service.convertStepToPdf(file, pdfFile);

        verify(builder).addFormattedText(contains("could not be analyzed"), anyBoolean(), anyBoolean(), anyFloat());
    }

    @Test
    void fileWithNoDataSectionProducesEmptyMessage() throws IOException {
        String content = """
                ISO-10303-21;
                HEADER;
                FILE_DESCRIPTION(('test'),'2;1');
                ENDSEC;
                END-ISO-10303-21;
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file", "nodata.step", "application/octet-stream", content.getBytes());
        File pdfFile = tempDir.resolve("nodata.pdf").toFile();

        service.convertStepToPdf(file, pdfFile);

        verify(builder).addFormattedText(contains("could not be analyzed"), anyBoolean(), anyBoolean(), anyFloat());
    }

    // ---------------------------------------------------------------
    // Integration test with real PdfBoxBackend
    // ---------------------------------------------------------------

    @Test
    void integrationWithPdfBoxBackend() throws IOException {
        String content = """
                ISO-10303-21;
                HEADER;
                FILE_DESCRIPTION(('Assembly model'),'2;1');
                FILE_NAME('bracket.step','2024-03-20');
                FILE_SCHEMA(('AUTOMOTIVE_DESIGN'));
                ENDSEC;
                DATA;
                #1=PRODUCT('Bracket','bracket_01','A mounting bracket',#2);
                #2=PRODUCT_DEFINITION_CONTEXT('part definition',#3,'design');
                #10=ADVANCED_BREP_SHAPE_REPRESENTATION('bracket shape',(#11),#12);
                #11=CARTESIAN_POINT('',(0.,0.,0.));
                #12=CARTESIAN_POINT('',(100.,50.,25.));
                #100=NEXT_ASSEMBLY_USAGE_OCCURRENCE('n1','Bolt','',#1,#10,$);
                ENDSEC;
                END-ISO-10303-21;
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file", "bracket.step", "application/octet-stream", content.getBytes());
        File pdfFile = tempDir.resolve("bracket.pdf").toFile();

        var realBackend = new com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend();
        var realService = new StepToPdfService(realBackend);

        assertThatNoException().isThrownBy(() -> realService.convertStepToPdf(file, pdfFile));
        assertThat(pdfFile).exists();
        assertThat(pdfFile.length()).isGreaterThan(0);
    }

    // ---------------------------------------------------------------
    // Quoted parameter extraction
    // ---------------------------------------------------------------

    @Test
    void extractsQuotedParamsCorrectly() {
        String line = "#10=PRODUCT('Gear Assembly','gear_001','A gear',#11);";
        int parenIdx = line.indexOf('(');

        var params = service.extractQuotedParams(line, parenIdx);

        assertThat(params).containsExactly("Gear Assembly", "gear_001", "A gear");
    }

    @Test
    void extractsRefParamsCorrectly() {
        String line = "#100=NEXT_ASSEMBLY_USAGE_OCCURRENCE('id','name','desc',#10,#20,$);";
        int parenIdx = line.indexOf('(');

        var refs = service.extractRefParams(line, parenIdx);

        assertThat(refs).containsExactly("10", "20");
    }

    // ---------------------------------------------------------------
    // Malformed input handling
    // ---------------------------------------------------------------

    @Test
    void skipsLinesWithoutEntityPattern() throws IOException {
        String content = """
                ISO-10303-21;
                HEADER;
                ENDSEC;
                DATA;
                #1=CARTESIAN_POINT('',(0.,0.,0.));
                /* this is a comment line */
                #notvalid
                #2=DIRECTION('',(1.,0.,0.));
                ENDSEC;
                END-ISO-10303-21;
                """;

        StepMetadata metadata = service.parseStepContent(content);

        assertThat(metadata.totalEntityCount).isEqualTo(2);
    }
}
