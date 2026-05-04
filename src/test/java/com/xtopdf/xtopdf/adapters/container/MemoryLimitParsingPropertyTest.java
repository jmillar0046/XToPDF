package com.xtopdf.xtopdf.adapters.container;

import com.xtopdf.xtopdf.ports.ContainerConfig;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for DockerContainerAdapter.parseMemoryLimit().
 *
 * Property 10: Memory Limit Parsing — For any valid memory limit string in the
 * format "{number}{unit}" where unit is one of "k", "m", or "g", the
 * DockerContainerAdapter's parseMemoryLimit method SHALL return the correct byte
 * value equal to number × unit_multiplier (where k=1024, m=1024², g=1024³).
 *
 * **Validates: Requirements 8.6**
 */
class MemoryLimitParsingPropertyTest {

    private static final ContainerConfig DEFAULT_CONFIG = ContainerConfig.builder()
            .imageName("xtopdf-converter:latest")
            .memoryLimit("512m")
            .cpuLimit(1)
            .timeoutSeconds(300)
            .cleanupEnabled(true)
            .containerPort(8080)
            .build();

    private final DockerContainerAdapter adapter = new DockerContainerAdapter(DEFAULT_CONFIG, false);

    /**
     * Property 10: Memory Limit Parsing
     *
     * For any valid memory limit string in the format "{number}{unit}" where unit
     * is one of "k", "m", or "g", the DockerContainerAdapter's parseMemoryLimit
     * method SHALL return the correct byte value equal to number × unit_multiplier
     * (where k=1024, m=1024², g=1024³).
     *
     * **Validates: Requirements 8.6**
     */
    @Property
    @Label("Property 10: parseMemoryLimit returns correct byte value for any valid {number}{unit}")
    void parseMemoryLimitReturnsCorrectByteValue(
            @ForAll("validNumbers") int number,
            @ForAll("memoryUnits") String unit) {

        String memoryLimit = number + unit;
        long expected = computeExpectedBytes(number, unit);

        long result = adapter.parseMemoryLimit(memoryLimit);

        assertThat(result)
                .as("parseMemoryLimit(\"%s\") should return %d", memoryLimit, expected)
                .isEqualTo(expected);
    }

    /**
     * Property 10 (case-insensitive variant): parseMemoryLimit handles uppercase units.
     *
     * **Validates: Requirements 8.6**
     */
    @Property
    @Label("Property 10: parseMemoryLimit is case-insensitive for unit suffix")
    void parseMemoryLimitIsCaseInsensitive(
            @ForAll("validNumbers") int number,
            @ForAll("memoryUnits") String unit) {

        String lowerLimit = number + unit.toLowerCase();
        String upperLimit = number + unit.toUpperCase();

        long lowerResult = adapter.parseMemoryLimit(lowerLimit);
        long upperResult = adapter.parseMemoryLimit(upperLimit);

        assertThat(lowerResult)
                .as("parseMemoryLimit(\"%s\") should equal parseMemoryLimit(\"%s\")",
                        lowerLimit, upperLimit)
                .isEqualTo(upperResult);
    }

    // ---- Arbitraries ----

    @Provide
    Arbitrary<Integer> validNumbers() {
        return Arbitraries.integers().between(1, 8192);
    }

    @Provide
    Arbitrary<String> memoryUnits() {
        return Arbitraries.of("k", "m", "g");
    }

    // ---- Helpers ----

    private long computeExpectedBytes(int number, String unit) {
        return switch (unit.toLowerCase()) {
            case "k" -> (long) number * 1024L;
            case "m" -> (long) number * 1024L * 1024L;
            case "g" -> (long) number * 1024L * 1024L * 1024L;
            default -> throw new IllegalArgumentException("Unknown unit: " + unit);
        };
    }
}
