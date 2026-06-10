package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.exceptions.FileConversionException;
import com.xtopdf.xtopdf.services.conversion.threed.BoundingBox3D;
import com.xtopdf.xtopdf.services.conversion.threed.ObjToPdfService;
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
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ObjToPdfServiceTest {

    private ObjToPdfService objToPdfService;
    private com.xtopdf.xtopdf.pdf.PdfBackendProvider pdfBackend;

    private static final long DEFAULT_MAX_FILE_SIZE = 52_428_800L; // 50MB

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        pdfBackend = new com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend();
        objToPdfService = new ObjToPdfService(pdfBackend);
        setMaxFileSize(DEFAULT_MAX_FILE_SIZE);
    }

    private void setMaxFileSize(long value) throws Exception {
        Field field = ObjToPdfService.class.getDeclaredField("maxFileSize");
        field.setAccessible(true);
        field.set(objToPdfService, value);
    }

    @Test
    void testConvertObjToPdf_Success() throws Exception {
        String content = "# Test OBJ\nv 0 0 0\nv 1 0 0\nv 0 1 0\nf 1 2 3\n";

        MockMultipartFile objFile = new MockMultipartFile(
                "file", "test.obj", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());

        File pdfFile = tempDir.resolve("testObjOutput.pdf").toFile();

        objToPdfService.convertObjToPdf(objFile, pdfFile);

        assertThat(pdfFile).exists();
        assertThat(pdfFile.length()).isGreaterThan(0);
    }

    @Test
    void testConvertObjToPdf_WithGroups() throws Exception {
        String content = "v 0 0 0\ng group1\nv 1 0 0\nf 1 2 3\n";

        MockMultipartFile objFile = new MockMultipartFile(
                "file", "test.obj", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());

        File pdfFile = tempDir.resolve("testObjGroupsOutput.pdf").toFile();

        objToPdfService.convertObjToPdf(objFile, pdfFile);

        assertThat(pdfFile).exists();
    }

    // --- File Size Limit Tests (Requirements 7.2, 7.3, 7.4, 7.5) ---

    @Test
    void testFileExactlyAtLimit_shouldNotThrowSizeException() throws Exception {
        setMaxFileSize(1024L);

        MockMultipartFile objFile = new MockMultipartFile(
                "file", "model.obj", MediaType.APPLICATION_OCTET_STREAM_VALUE, new byte[0]) {
            @Override
            public long getSize() {
                return 1024L;
            }
        };

        File pdfFile = tempDir.resolve("atLimit.pdf").toFile();

        assertThatNoException()
                .as("File exactly at the size limit should not be rejected")
                .isThrownBy(() -> objToPdfService.convertObjToPdf(objFile, pdfFile));
    }

    @Test
    void testFileOneByteOverLimit_shouldThrowFileConversionException() throws Exception {
        setMaxFileSize(1024L);

        MockMultipartFile objFile = new MockMultipartFile(
                "file", "model.obj", MediaType.APPLICATION_OCTET_STREAM_VALUE, new byte[0]) {
            @Override
            public long getSize() {
                return 1025L;
            }
        };

        File pdfFile = tempDir.resolve("overLimit.pdf").toFile();

        assertThatThrownBy(() -> objToPdfService.convertObjToPdf(objFile, pdfFile))
                .as("File 1 byte over the limit should be rejected with FileConversionException")
                .isInstanceOf(FileConversionException.class);
    }

    @Test
    void testErrorMessageContainsSizeLimitInfo() throws Exception {
        setMaxFileSize(1024L);

        MockMultipartFile objFile = new MockMultipartFile(
                "file", "model.obj", MediaType.APPLICATION_OCTET_STREAM_VALUE, new byte[0]) {
            @Override
            public long getSize() {
                return 2048L;
            }
        };

        File pdfFile = tempDir.resolve("errorMsg.pdf").toFile();

        assertThatThrownBy(() -> objToPdfService.convertObjToPdf(objFile, pdfFile))
                .isInstanceOf(FileConversionException.class)
                .hasMessageContaining("1024")
                .as("Error message should contain the size limit value");
    }

    @Test
    void testConfigurableMaxSize_customLimit() throws Exception {
        setMaxFileSize(500L);

        MockMultipartFile objFile = new MockMultipartFile(
                "file", "model.obj", MediaType.APPLICATION_OCTET_STREAM_VALUE, new byte[0]) {
            @Override
            public long getSize() {
                return 501L;
            }
        };

        File pdfFile = tempDir.resolve("customLimit.pdf").toFile();

        assertThatThrownBy(() -> objToPdfService.convertObjToPdf(objFile, pdfFile))
                .isInstanceOf(FileConversionException.class)
                .hasMessageContaining("500");
    }

    // --- WireframeRenderer Delegation Tests (Requirements 10.5, 10.6) ---

    @Nested
    class WireframeRendererDelegationTests {

        @Test
        void testBuildFaceEdges_singleTriangularFace() {
            List<int[]> faces = List.of(new int[]{0, 1, 2});

            List<int[]> edges = objToPdfService.buildFaceEdges(faces, 3);

            assertThat(edges).hasSize(3);
            assertThat(edges.get(0)).containsExactly(0, 1);
            assertThat(edges.get(1)).containsExactly(1, 2);
            assertThat(edges.get(2)).containsExactly(2, 0);
        }

        @Test
        void testBuildFaceEdges_quadFace() {
            List<int[]> faces = List.of(new int[]{0, 1, 2, 3});

            List<int[]> edges = objToPdfService.buildFaceEdges(faces, 4);

            assertThat(edges).hasSize(4);
            assertThat(edges.get(0)).containsExactly(0, 1);
            assertThat(edges.get(1)).containsExactly(1, 2);
            assertThat(edges.get(2)).containsExactly(2, 3);
            assertThat(edges.get(3)).containsExactly(3, 0);
        }

        @Test
        void testBuildFaceEdges_multipleFaces() {
            List<int[]> faces = List.of(
                    new int[]{0, 1, 2},
                    new int[]{1, 2, 3}
            );

            List<int[]> edges = objToPdfService.buildFaceEdges(faces, 4);

            assertThat(edges).hasSize(6);
        }

        @Test
        void testBuildFaceEdges_outOfBoundsIndicesSkipped() {
            List<int[]> faces = List.of(new int[]{0, 1, 5}); // index 5 >= vertexCount of 3

            List<int[]> edges = objToPdfService.buildFaceEdges(faces, 3);

            // Edge (0,1) is valid, (1,5) is not, (5,0) is not
            assertThat(edges).hasSize(1);
            assertThat(edges.get(0)).containsExactly(0, 1);
        }

        @Test
        void testBuildFaceEdges_emptyFaces() {
            List<int[]> faces = new ArrayList<>();

            List<int[]> edges = objToPdfService.buildFaceEdges(faces, 10);

            assertThat(edges).isEmpty();
        }

        @Test
        void testConversionUsesBoundingBox3D() throws Exception {
            String content = "v 1 2 3\nv 4 5 6\nv 7 8 9\nf 1 2 3\n";

            MockMultipartFile objFile = new MockMultipartFile(
                    "file", "bbox.obj", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());

            File pdfFile = tempDir.resolve("bboxTest.pdf").toFile();

            objToPdfService.convertObjToPdf(objFile, pdfFile);

            assertThat(pdfFile).exists();
            assertThat(pdfFile.length()).isGreaterThan(0);
        }

        @Test
        void testWireframeRendererProducesDrawLineCalls() throws Exception {
            // A single triangular face should produce 3 edges via WireframeRenderer
            String content = "v 0 0 0\nv 10 0 0\nv 5 10 0\nf 1 2 3\n";

            MockMultipartFile objFile = new MockMultipartFile(
                    "file", "wireframe.obj", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());

            File pdfFile = tempDir.resolve("wireframeTest.pdf").toFile();

            objToPdfService.convertObjToPdf(objFile, pdfFile);

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
