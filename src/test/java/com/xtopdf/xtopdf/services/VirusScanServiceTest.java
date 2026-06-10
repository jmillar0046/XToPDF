package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for VirusScanService implementations.
 * Tests NoOpVirusScanService (always clean) and ClamAvVirusScanService (mock-based).
 */
@ExtendWith(MockitoExtension.class)
class VirusScanServiceTest {

    @Nested
    @DisplayName("NoOpVirusScanService")
    class NoOpTests {

        private final NoOpVirusScanService service = new NoOpVirusScanService();

        @Test
        @DisplayName("should always return clean result")
        void alwaysReturnsClean() {
            var file = new MockMultipartFile("file", "test.txt", "text/plain", "hello".getBytes());
            var result = service.scan(file);

            assertThat(result.isClean()).isTrue();
            assertThat(result.message()).isEqualTo("OK");
        }

        @Test
        @DisplayName("should return clean for empty file")
        void cleanForEmptyFile() {
            var file = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);
            var result = service.scan(file);

            assertThat(result.isClean()).isTrue();
        }

        @Test
        @DisplayName("should return clean for large file")
        void cleanForLargeFile() {
            byte[] largeContent = new byte[10 * 1024 * 1024]; // 10MB
            var file = new MockMultipartFile("file", "large.bin", "application/octet-stream", largeContent);
            var result = service.scan(file);

            assertThat(result.isClean()).isTrue();
        }
    }

    @Nested
    @DisplayName("ClamAvVirusScanService")
    class ClamAvTests {

        @Test
        @DisplayName("should return clean when ClamAV responds OK")
        void returnsCleanOnOkResponse() throws Exception {
            var mockSocket = createMockSocket("stream: OK\0");
            var service = createServiceWithMockSocket(mockSocket);

            var file = new MockMultipartFile("file", "clean.txt", "text/plain", "safe content".getBytes());
            var result = service.scan(file);

            assertThat(result.isClean()).isTrue();
            assertThat(result.message()).isEqualTo("OK");
        }

        @Test
        @DisplayName("should return infected when ClamAV detects virus")
        void returnsInfectedOnVirusFound() throws Exception {
            var mockSocket = createMockSocket("stream: Win.Test.EICAR_HDB-1 FOUND\0");
            var service = createServiceWithMockSocket(mockSocket);

            var file = new MockMultipartFile("file", "virus.exe", "application/octet-stream", "EICAR".getBytes());
            var result = service.scan(file);

            assertThat(result.isClean()).isFalse();
            assertThat(result.message()).contains("Win.Test.EICAR_HDB-1");
        }

        @Test
        @DisplayName("should return infected when ClamAV is unavailable (fail-safe)")
        void returnsInfectedWhenUnavailable() {
            var service = new ClamAvVirusScanService("localhost", 9999, 1000) {
                @Override
                protected Socket createSocket() throws IOException {
                    throw new IOException("Connection refused");
                }
            };

            var file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());
            var result = service.scan(file);

            assertThat(result.isClean()).isFalse();
            assertThat(result.message()).isEqualTo("Virus scan unavailable");
        }

        @Test
        @DisplayName("should return infected on unexpected response (fail-safe)")
        void returnsInfectedOnUnexpectedResponse() throws Exception {
            var mockSocket = createMockSocket("stream: ERROR something went wrong\0");
            var service = createServiceWithMockSocket(mockSocket);

            var file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());
            var result = service.scan(file);

            assertThat(result.isClean()).isFalse();
            assertThat(result.message()).contains("Scan inconclusive");
        }

        @Test
        @DisplayName("should send file content via INSTREAM protocol")
        void sendsFileContentViaInstream() throws Exception {
            var outputCapture = new ByteArrayOutputStream();
            var responseStream = new ByteArrayInputStream("stream: OK\0".getBytes(StandardCharsets.US_ASCII));

            var mockSocket = mock(Socket.class);
            when(mockSocket.getOutputStream()).thenReturn(outputCapture);
            when(mockSocket.getInputStream()).thenReturn(responseStream);

            var service = createServiceWithMockSocket(mockSocket);

            var fileContent = "test file content".getBytes();
            var file = new MockMultipartFile("file", "test.txt", "text/plain", fileContent);
            service.scan(file);

            byte[] sentData = outputCapture.toByteArray();
            // First bytes should be the INSTREAM command
            String sentString = new String(sentData, 0, 10, StandardCharsets.US_ASCII);
            assertThat(sentString).startsWith("zINSTREAM");
        }

        private Socket createMockSocket(String response) throws Exception {
            var responseStream = new ByteArrayInputStream(response.getBytes(StandardCharsets.US_ASCII));
            var outputStream = new ByteArrayOutputStream();

            var mockSocket = mock(Socket.class);
            when(mockSocket.getOutputStream()).thenReturn(outputStream);
            when(mockSocket.getInputStream()).thenReturn(responseStream);
            return mockSocket;
        }

        private ClamAvVirusScanService createServiceWithMockSocket(Socket mockSocket) {
            return new ClamAvVirusScanService("localhost", 3310, 30000) {
                @Override
                protected Socket createSocket() {
                    return mockSocket;
                }
            };
        }
    }
}
