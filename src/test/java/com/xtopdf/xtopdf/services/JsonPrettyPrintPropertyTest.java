package com.xtopdf.xtopdf.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.jqwik.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for JSON pretty-print idempotence.
 *
 * // Feature: converter-improvements, Property 6: JSON pretty-print idempotence
 *
 * Property 6: JSON pretty-print idempotence
 * - For any valid JSON input string, pretty-printing it via Jackson ObjectMapper
 *   (parse → writeWithDefaultPrettyPrinter) and then pretty-printing the result again
 *   SHALL produce byte-identical output.
 * - That is: prettyPrint(prettyPrint(json)) == prettyPrint(json).
 *
 * **Validates: Requirements 8.1, 8.3, 8.4**
 */
class JsonPrettyPrintPropertyTest {

    // Feature: converter-improvements, Property 6: JSON pretty-print idempotence

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Pretty-prints JSON using the same approach as JsonToPdfService:
     * parse with ObjectMapper, then serialize with default pretty printer.
     */
    private String prettyPrint(String json) throws Exception {
        Object jsonTree = objectMapper.readValue(json, Object.class);
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonTree);
    }

    /**
     * Property 6: JSON pretty-print is idempotent.
     * Pretty-printing any valid JSON document once and then pretty-printing the result again
     * produces identical output: prettyPrint(prettyPrint(json)) == prettyPrint(json).
     *
     * **Validates: Requirements 8.1, 8.3, 8.4**
     */
    @Property(tries = 25)
    @Tag("Feature: converter-improvements, Property 6: JSON pretty-print idempotence")
    void jsonPrettyPrintIsIdempotent(@ForAll("validJsonDocuments") String json) throws Exception {
        String firstPrettyPrint = prettyPrint(json);
        String secondPrettyPrint = prettyPrint(firstPrettyPrint);

        assertThat(secondPrettyPrint)
                .as("prettyPrint(prettyPrint(json)) must equal prettyPrint(json) - pretty-printing must be idempotent")
                .isEqualTo(firstPrettyPrint);
    }

    @Provide
    Arbitrary<String> validJsonDocuments() {
        return Arbitraries.oneOf(
                jsonObjects(),
                jsonArrays(),
                nestedJsonObjects(),
                jsonWithMixedTypes(),
                minifiedJson()
        );
    }

    /**
     * Generates simple JSON objects with string key-value pairs.
     */
    private Arbitrary<String> jsonObjects() {
        Arbitrary<String> keys = Arbitraries.strings()
                .alpha().ofMinLength(1).ofMaxLength(10);
        Arbitrary<String> values = Arbitraries.strings()
                .alpha().ofMinLength(0).ofMaxLength(20);
        Arbitrary<Integer> fieldCount = Arbitraries.integers().between(1, 5);

        return Combinators.combine(keys, values, fieldCount)
                .as((key, value, count) -> {
                    StringBuilder sb = new StringBuilder("{");
                    for (int i = 0; i < count; i++) {
                        if (i > 0) sb.append(",");
                        sb.append("\"").append(key).append(i).append("\":")
                                .append("\"").append(value).append("\"");
                    }
                    sb.append("}");
                    return sb.toString();
                });
    }

    /**
     * Generates JSON arrays with mixed primitive values.
     */
    private Arbitrary<String> jsonArrays() {
        Arbitrary<Integer> size = Arbitraries.integers().between(1, 6);
        Arbitrary<String> stringValues = Arbitraries.strings()
                .alpha().ofMinLength(1).ofMaxLength(10);
        Arbitrary<Integer> intValues = Arbitraries.integers().between(-1000, 1000);
        Arbitrary<Boolean> boolValues = Arbitraries.of(true, false);

        return Combinators.combine(size, stringValues, intValues, boolValues)
                .as((count, str, num, bool) -> {
                    StringBuilder sb = new StringBuilder("[");
                    for (int i = 0; i < count; i++) {
                        if (i > 0) sb.append(",");
                        switch (i % 4) {
                            case 0 -> sb.append("\"").append(str).append("\"");
                            case 1 -> sb.append(num);
                            case 2 -> sb.append(bool);
                            case 3 -> sb.append("null");
                        }
                    }
                    sb.append("]");
                    return sb.toString();
                });
    }

    /**
     * Generates nested JSON objects (2-4 levels deep).
     */
    private Arbitrary<String> nestedJsonObjects() {
        Arbitrary<String> keys = Arbitraries.strings()
                .alpha().ofMinLength(1).ofMaxLength(8);
        Arbitrary<String> values = Arbitraries.strings()
                .alpha().ofMinLength(1).ofMaxLength(10);
        Arbitrary<Integer> depth = Arbitraries.integers().between(2, 4);

        return Combinators.combine(keys, values, depth)
                .as((key, value, d) -> {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < d; i++) {
                        sb.append("{\"").append(key).append(i).append("\":");
                    }
                    sb.append("\"").append(value).append("\"");
                    sb.append("}".repeat(d));
                    return sb.toString();
                });
    }

    /**
     * Generates JSON objects with mixed value types (strings, numbers, booleans, nulls, arrays, nested objects).
     */
    private Arbitrary<String> jsonWithMixedTypes() {
        Arbitrary<String> keys = Arbitraries.strings()
                .alpha().ofMinLength(1).ofMaxLength(8);
        Arbitrary<String> strValues = Arbitraries.strings()
                .alpha().ofMinLength(1).ofMaxLength(10);
        Arbitrary<Integer> numValues = Arbitraries.integers().between(-999, 999);
        Arbitrary<Boolean> boolValues = Arbitraries.of(true, false);

        return Combinators.combine(keys, strValues, numValues, boolValues)
                .as((key, str, num, bool) ->
                        "{" +
                        "\"" + key + "str\":\"" + str + "\"," +
                        "\"" + key + "num\":" + num + "," +
                        "\"" + key + "bool\":" + bool + "," +
                        "\"" + key + "null\":null," +
                        "\"" + key + "arr\":[1,2,3]," +
                        "\"" + key + "obj\":{\"nested\":true}" +
                        "}"
                );
    }

    /**
     * Generates minified (single-line, no whitespace) JSON that should be expanded by pretty-print.
     */
    private Arbitrary<String> minifiedJson() {
        Arbitrary<String> keys = Arbitraries.strings()
                .alpha().ofMinLength(1).ofMaxLength(8);
        Arbitrary<String> values = Arbitraries.strings()
                .alpha().ofMinLength(1).ofMaxLength(10);
        Arbitrary<Integer> numValues = Arbitraries.integers().between(0, 100);

        return Combinators.combine(keys, values, numValues)
                .as((key, value, num) ->
                        "{\"" + key + "\":{\"items\":[{\"name\":\"" + value + "\",\"count\":" + num + "},{\"name\":\"" + value + "b\",\"count\":" + (num + 1) + "}],\"total\":" + (num * 2) + "}}"
                );
    }
}
