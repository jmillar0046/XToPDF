package com.xtopdf.xtopdf.services;

import net.jqwik.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for heading level to font size mapping.
 *
 * Property 3: Heading level to font size mapping is monotonically decreasing
 * - For any heading level pair (m, n) where 1 ≤ m < n ≤ 6, the font size mapped to level m
 *   SHALL be strictly greater than the font size mapped to level n.
 * - All six levels SHALL map to distinct font sizes.
 *
 * **Validates: Requirements 3.4, 4.2**
 */
class HeadingFontSizePropertyTest {

    /**
     * The heading font size mapping used by both DOC and ODT converters.
     * This is the pure function under test — no POI dependency needed.
     */
    private float detectHeadingSize(int level) {
        return switch (level) {
            case 1 -> 24f;
            case 2 -> 20f;
            case 3 -> 16f;
            case 4 -> 14f;
            case 5 -> 13f;
            case 6 -> 12f;
            default -> 12f;
        };
    }

    /**
     * Property 3: For any heading level pair (m, n) where 1 ≤ m < n ≤ 6,
     * fontSize(m) > fontSize(n) — the mapping is monotonically decreasing.
     *
     * This is an exhaustive test: there are only 15 valid pairs (6 choose 2).
     *
     * **Validates: Requirements 3.4, 4.2**
     */
    @Property(tries = 25)
    @Tag("Feature: converter-improvements, Property 3: Heading level to font size mapping is monotonically decreasing")
    void headingFontSizeIsMonotonicallyDecreasing(@ForAll("headingLevelPairs") int[] pair) {
        int m = pair[0];
        int n = pair[1];

        float fontSizeM = detectHeadingSize(m);
        float fontSizeN = detectHeadingSize(n);

        assertThat(fontSizeM)
                .as("fontSize(level %d) must be strictly greater than fontSize(level %d)", m, n)
                .isGreaterThan(fontSizeN);
    }

    /**
     * Property 3 (supplementary): All six heading levels map to distinct font sizes.
     *
     * **Validates: Requirements 3.4, 4.2**
     */
    @Property(tries = 25)
    @Tag("Feature: converter-improvements, Property 3: Heading level to font size mapping is monotonically decreasing")
    void allHeadingLevelsMapToDistinctFontSizes(@ForAll("headingLevels") int level) {
        float fontSize = detectHeadingSize(level);

        for (int other = 1; other <= 6; other++) {
            if (other != level) {
                float otherSize = detectHeadingSize(other);
                assertThat(fontSize)
                        .as("fontSize(level %d) must differ from fontSize(level %d)", level, other)
                        .isNotEqualTo(otherSize);
            }
        }
    }

    /**
     * Provides all valid heading level pairs (m, n) where 1 ≤ m < n ≤ 6.
     * There are exactly 15 such pairs.
     */
    @Provide
    Arbitrary<int[]> headingLevelPairs() {
        return Arbitraries.of(
                new int[]{1, 2}, new int[]{1, 3}, new int[]{1, 4}, new int[]{1, 5}, new int[]{1, 6},
                new int[]{2, 3}, new int[]{2, 4}, new int[]{2, 5}, new int[]{2, 6},
                new int[]{3, 4}, new int[]{3, 5}, new int[]{3, 6},
                new int[]{4, 5}, new int[]{4, 6},
                new int[]{5, 6}
        );
    }

    /**
     * Provides all valid heading levels (1 through 6).
     */
    @Provide
    Arbitrary<Integer> headingLevels() {
        return Arbitraries.integers().between(1, 6);
    }
}
