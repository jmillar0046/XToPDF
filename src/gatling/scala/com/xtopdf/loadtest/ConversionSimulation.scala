package com.xtopdf.loadtest

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

/**
 * Gatling load test simulation for XToPDF conversion API.
 *
 * Scenario:
 * - Ramps up 10 users over 30 seconds
 * - Each user sends 5 conversion requests
 * - Validates response codes are 200 (success) or 429 (rate limited)
 *
 * Run with:
 *   ./gradlew gatlingRun
 *   or
 *   ./gradlew gatlingRun-com.xtopdf.loadtest.ConversionSimulation
 */
class ConversionSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/pdf")
    .contentTypeHeader("multipart/form-data")

  // Create a minimal text file payload for conversion
  val textFileBody = "Hello World - Load Test Content\nLine 2\nLine 3"

  val convertScenario = scenario("File Conversion Load Test")
    .repeat(5) {
      exec(
        http("Convert Text File")
          .post("/api/convert")
          .bodyPart(
            StringBodyPart("file", textFileBody)
              .fileName("loadtest.txt")
              .contentType("text/plain")
          )
          .check(status.in(200, 429))
      )
      .pause(1.second, 3.seconds)
    }

  setUp(
    convertScenario.inject(
      rampUsers(10).during(30.seconds)
    )
  ).protocols(httpProtocol)
    .assertions(
      global.responseTime.max.lt(30000),      // Max response time < 30s
      global.successfulRequests.percent.gt(50) // At least 50% succeed (rest may be rate-limited)
    )
}
