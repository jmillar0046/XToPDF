package com.xtopdf.xtopdf.contract;

import static org.assertj.core.api.Assertions.assertThat;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Consumer contract test for the /api/convert endpoint.
 *
 * <p>Defines the contract that consumers expect from the XToPDF conversion API:
 * POST /api/convert with a multipart file, expecting a 200 response with PDF content-type.
 */
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "xtopdf-provider", pactVersion = au.com.dius.pact.core.model.PactSpecVersion.V4)
class PactConsumerTest {

  @Pact(consumer = "xtopdf-consumer")
  public V4Pact convertFileContract(PactDslWithProvider builder) {
    return builder
        .given("the conversion service is available")
        .uponReceiving("a request to convert a text file to PDF")
        .path("/api/convert")
        .method("POST")
        .headers(java.util.Map.of("Content-Type", "multipart/form-data; boundary=boundary"))
        .body(
            "--boundary\r\n"
                + "Content-Disposition: form-data; name=\"file\"; filename=\"test.txt\"\r\n"
                + "Content-Type: text/plain\r\n\r\n"
                + "Hello World\r\n"
                + "--boundary--\r\n")
        .willRespondWith()
        .status(200)
        .headers(java.util.Map.of("Content-Type", "application/pdf"))
        .toPact(V4Pact.class);
  }

  @Test
  @PactTestFor(pactMethod = "convertFileContract")
  void verifyConvertFileContract(MockServer mockServer) throws IOException, InterruptedException {
    // Build a multipart request to the mock server
    String boundary = "boundary";
    String body =
        "--" + boundary + "\r\n"
            + "Content-Disposition: form-data; name=\"file\"; filename=\"test.txt\"\r\n"
            + "Content-Type: text/plain\r\n\r\n"
            + "Hello World\r\n"
            + "--" + boundary + "--\r\n";

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(mockServer.getUrl() + "/api/convert"))
            .header("Content-Type", "multipart/form-data; boundary=" + boundary)
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

    HttpResponse<byte[]> response =
        HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofByteArray());

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.headers().firstValue("Content-Type")).isPresent();
    assertThat(response.headers().firstValue("Content-Type").get()).contains("application/pdf");
  }
}
