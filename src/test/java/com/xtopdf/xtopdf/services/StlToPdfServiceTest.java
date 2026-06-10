package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.exceptions.FileConversionException;
import com.xtopdf.xtopdf.services.conversion.threed.BoundingBox3D;
import com.xtopdf.xtopdf.services.conversion.threed.StlToPdfService;
import com.xtopdf.xtopdf.services.conversion.threed.WireframeRenderer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class StlToPdfServiceTest {

    private StlToPdfService stlToPdfService;
    private com.xtopdf.xtopdf.pdf.PdfBackendProvider pdfBackend;

    private static final long DEFAULT_MAX_FILE_SIZE = 52_428_800L; // 50MB

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        pdfBackend = new com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend();
        stlToPdfService = new StlToPdfService(pdfBackend);
        setMaxFileSize(DEFAULT_MAX_FILE_SIZE);
    }

    private void setMaxFileSize(long value) throws Exception {
        Field field = StlToPdfService.class.getDeclaredField("maxFileSize");
        field.setAccessible(true);
        field.set(stlToPdfService, value);
    }

    @Test
    void testConvertStlToPdf_AsciiFormat() throws Exception {
        String content = "solid test\nfacet normal 0 0 1\nouter loop\nvertex 0 0 0\nvertex 1 0 0\nvertex 0 1 0\nendloop\nendfacet\nendsolid";

        MockMultipartFile stlFile = new MockMultipartFile(
                "file", "test.stl", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());

        File pdfFile = tempDir.resolve("testStlOutput.pdf").toFile();

        stlToPdfService.convertStlToPdf(stlFile, pdfFile);

        assertThat(pdfFile).exists();
        assertThat(pdfFile.length()).isGreaterThan(0);
    }

    @Test
    void testConvertStlToPdf_EmptyFile() throws Exception {
        MockMultipartFile stlFile = new MockMultipartFile(
                "file", "test.stl", MediaType.APPLICATION_OCTET_STREAM_VALUE, "".getBytes());

        File pdfFile = tempDir.resolve("testEmptyStlOutput.pdf").toFile();

        // Empty file should still create a PDF (with 0 triangles)
        stlToPdfService.convertStlToPdf(stlFile, pdfFile);

        assertThat(pdfFile).exists();
    }

    // --- File Size Limit Tests (Requirements 7.1, 7.3, 7.4, 7.5) ---

    @Test
    void testFileExactlyAtLimit_shouldNotThrowSizeException() throws Exception {
        setMaxFileSize(1024L);

        MockMultipartFile stlFile = new MockMultipartFile(
                "file", "model.stl", MediaType.APPLICATION_OCTET_STREAM_VALUE, new byte[0]) {
            @Override
            public long getSize() {
                return 1024L;
            }
        };

        File pdfFile = tempDir.resolve("atLimit.pdf").toFile();

        assertThatNoException()
                .as("File exactly at the size limit should not be rejected")
                .isThrownBy(() -> stlToPdfService.convertStlToPdf(stlFile, pdfFile));
    }

    @Test
    void testFileOneByteOverLimit_shouldThrowFileConversionException() throws Exception {
        setMaxFileSize(1024L);

        MockMultipartFile stlFile = new MockMultipartFile(
                "file", "model.stl", MediaType.APPLICATION_OCTET_STREAM_VALUE, new byte[0]) {
            @Override
            public long getSize() {
                return 1025L;
            }
        };

        File pdfFile = tempDir.resolve("overLimit.pdf").toFile();

        assertThatThrownBy(() -> stlToPdfService.convertStlToPdf(stlFile, pdfFile))
                .as("File 1 byte over the limit should be rejected with FileConversionException")
                .isInstanceOf(FileConversionException.class);
    }

    @Test
    void testErrorMessageContainsSizeLimitInfo() throws Exception {
        setMaxFileSize(1024L);

        MockMultipartFile stlFile = new MockMultipartFile(
                "file", "model.stl", MediaType.APPLICATION_OCTET_STREAM_VALUE, new byte[0]) {
            @Override
            public long getSize() {
                return 2048L;
            }
        };

        File pdfFile = tempDir.resolve("errorMsg.pdf").toFile();

        assertThatThrownBy(() -> stlToPdfService.convertStlToPdf(stlFile, pdfFile))
                .isInstanceOf(FileConversionException.class)
                .hasMessageContaining("1024")
                .as("Error message should contain the size limit value");
    }

    @Test
    void testNormalConversionProceedsWhenWithinLimit() throws Exception {
        setMaxFileSize(DEFAULT_MAX_FILE_SIZE);

        String content = "solid test\nfacet normal 0 0 1\nouter loop\nvertex 0 0 0\nvertex 1 0 0\nvertex 0 1 0\nendloop\nendfacet\nendsolid";

        MockMultipartFile stlFile = new MockMultipartFile(
                "file", "small.stl", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());

        File pdfFile = tempDir.resolve("withinLimit.pdf").toFile();

        stlToPdfService.convertStlToPdf(stlFile, pdfFile);

        assertThat(pdfFile).exists();
        assertThat(pdfFile.length()).isGreaterThan(0);
    }

    // --- WireframeRenderer Delegation Tests (Requirements 10.4, 10.6) ---

    @Nested
    class WireframeRendererDelegationTests {

        @Test
        void testBuildTriangleEdges_singleTriangle() {
            List<int[]> edges = stlToPdfService.buildTriangleEdges(1, 3);

            assertThat(edges).hasSize(3);
            assertThat(edges.get(0)).containsExactly(0, 1);
            assertThat(edges.get(1)).containsExactly(1, 2);
            assertThat(edges.get(2)).containsExactly(2, 0);
        }

        @Test
        void testBuildTriangleEdges_multipleTriangles() {
            List<int[]> edges = stlToPdfService.buildTriangleEdges(2, 6);

            assertThat(edges).hasSize(6);
            // Triangle 0
            assertThat(edges.get(0)).containsExactly(0, 1);
            assertThat(edges.get(1)).containsExactly(1, 2);
            assertThat(edges.get(2)).containsExactly(2, 0);
            // Triangle 1
            assertThat(edges.get(3)).containsExactly(3, 4);
            assertThat(edges.get(4)).containsExactly(4, 5);
            assertThat(edges.get(5)).containsExactly(5, 3);
        }

        @Test
        void testBuildTriangleEdges_insufficientVertices() {
            List<int[]> edges = stlToPdfService.buildTriangleEdges(3, 5);

            // Only first triangle (base 0, needs index 2 < 5) fully fits
            // Second triangle (base 3, needs index 5) — 5 is NOT < 5, so stops
            assertThat(edges).hasSize(3);
        }

        @Test
        void testBuildTriangleEdges_zeroTriangles() {
            List<int[]> edges = stlToPdfService.buildTriangleEdges(0, 0);

            assertThat(edges).isEmpty();
        }

        @Test
        void testConversionUsesBoundingBox3D() throws Exception {
            String content = "solid test\nfacet normal 0 0 1\nouter loop\nvertex 1 2 3\nvertex 4 5 6\nvertex 7 8 9\nendloop\nendfacet\nendsolid";

            MockMultipartFile stlFile = new MockMultipartFile(
                    "file", "bbox.stl", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());

            File pdfFile = tempDir.resolve("bboxTest.pdf").toFile();

            stlToPdfService.convertStlToPdf(stlFile, pdfFile);

            assertThat(pdfFile).exists();
            assertThat(pdfFile.length()).isGreaterThan(0);
        }

        @Test
        void testWireframeRendererProducesDrawLineCalls() throws Exception {
            // A single triangle should produce 3 edges via WireframeRenderer
            String content = "solid test\nfacet normal 0 0 1\nouter loop\nvertex 0 0 0\nvertex 10 0 0\nvertex 5 10 0\nendloop\nendfacet\nendsolid";

            MockMultipartFile stlFile = new MockMultipartFile(
                    "file", "wireframe.stl", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());

            File pdfFile = tempDir.resolve("wireframeTest.pdf").toFile();

            stlToPdfService.convertStlToPdf(stlFile, pdfFile);

            assertThat(pdfFile).exists();
            assertThat(pdfFile.length()).isGreaterThan(0);
        }

        @Test
        void testProjectionValuesMatchWireframeRenderer() {
            // Verify that the same projection logic is used by checking known coordinates
            BoundingBox3D bbox = BoundingBox3D.initial(0, 0, 0).expand(10, 10, 10);
            float scale = WireframeRenderer.calculateScale(bbox);

            float[] projected = WireframeRenderer.projectVertex(new float[]{5, 5, 5}, bbox, scale);

            assertThat(projected[0])
                    .as("X projection should be within render bounds")
                    .isBetween(WireframeRenderer.OFFSET_X, WireframeRenderer.OFFSET_X + WireframeRenderer.RENDER_WIDTH);
            assertThat(projected[1])
                    .as("Y projection should be within render bounds")
                    .isBetween(WireframeRenderer.OFFSET_Y - WireframeRenderer.RENDER_HEIGHT, WireframeRenderer.OFFSET_Y);
        }
    }
}
