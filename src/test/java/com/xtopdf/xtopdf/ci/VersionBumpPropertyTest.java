package com.xtopdf.xtopdf.ci;

import com.xtopdf.xtopdf.utils.VersionBump;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for semantic version bump computation.
 *
 * Feature: codebase-hardening, Property 8: Semantic version bump computation
 *
 * For any valid semantic version (MAJOR.MINOR.PATCH where each component is a non-negative integer)
 * and any commit message string:
 * - If the message contains "[major]", the result SHALL be (MAJOR+1).0.0
 * - If the message contains "[minor]" but not "[major]", the result SHALL be MAJOR.(MINOR+1).0
 * - Otherwise, the result SHALL be MAJOR.MINOR.(PATCH+1)
 *
 * Validates: Requirements 21.1, 21.2, 21.3
 */
@Tag("Feature: codebase-hardening, Property 8: Semantic version bump computation")
class VersionBumpPropertyTest {

    /**
     * Property 8a: [major] keyword bumps major version and resets minor+patch.
     *
     * Validates: Requirements 21.1
     */
    @Property(tries = 25)
    @Label("Commit with [major] bumps major version and resets minor and patch to 0")
    void majorKeywordBumpsMajorAndResets(
            @ForAll @IntRange(min = 0, max = 99) int major,
            @ForAll @IntRange(min = 0, max = 99) int minor,
            @ForAll @IntRange(min = 0, max = 99) int patch,
            @ForAll("majorCommitMessages") String commitMessage) {

        String currentVersion = major + "." + minor + "." + patch;
        String result = VersionBump.computeNextVersion(currentVersion, commitMessage);

        String expected = (major + 1) + ".0.0";
        assertThat(result)
                .as("Version %s with [major] commit should become %s", currentVersion, expected)
                .isEqualTo(expected);
    }

    /**
     * Property 8b: [minor] keyword (without [major]) bumps minor version and resets patch.
     *
     * Validates: Requirements 21.2
     */
    @Property(tries = 25)
    @Label("Commit with [minor] (no [major]) bumps minor version and resets patch to 0")
    void minorKeywordBumpsMinorAndResetsPatch(
            @ForAll @IntRange(min = 0, max = 99) int major,
            @ForAll @IntRange(min = 0, max = 99) int minor,
            @ForAll @IntRange(min = 0, max = 99) int patch,
            @ForAll("minorOnlyCommitMessages") String commitMessage) {

        String currentVersion = major + "." + minor + "." + patch;
        String result = VersionBump.computeNextVersion(currentVersion, commitMessage);

        String expected = major + "." + (minor + 1) + ".0";
        assertThat(result)
                .as("Version %s with [minor] commit should become %s", currentVersion, expected)
                .isEqualTo(expected);
    }

    /**
     * Property 8c: No keyword bumps patch version only.
     *
     * Validates: Requirements 21.3
     */
    @Property(tries = 25)
    @Label("Commit without [major] or [minor] bumps patch version only")
    void noKeywordBumpsPatchOnly(
            @ForAll @IntRange(min = 0, max = 99) int major,
            @ForAll @IntRange(min = 0, max = 99) int minor,
            @ForAll @IntRange(min = 0, max = 99) int patch,
            @ForAll("patchCommitMessages") String commitMessage) {

        String currentVersion = major + "." + minor + "." + patch;
        String result = VersionBump.computeNextVersion(currentVersion, commitMessage);

        String expected = major + "." + minor + "." + (patch + 1);
        assertThat(result)
                .as("Version %s with patch commit should become %s", currentVersion, expected)
                .isEqualTo(expected);
    }

    // --- Arbitraries ---

    @Provide
    Arbitrary<String> majorCommitMessages() {
        return Arbitraries.of(
                "feat: breaking change [major]",
                "[major] Complete API redesign",
                "refactor: [major] drop v1 support",
                "feat: new architecture [major] [minor]"
        );
    }

    @Provide
    Arbitrary<String> minorOnlyCommitMessages() {
        return Arbitraries.of(
                "feat: add new endpoint [minor]",
                "[minor] Add watermark support",
                "feat: [minor] new PDF operations",
                "feature: pagination support [minor]"
        );
    }

    @Provide
    Arbitrary<String> patchCommitMessages() {
        return Arbitraries.of(
                "fix: correct null check",
                "chore: update dependencies",
                "docs: update README",
                "fix: handle edge case in converter",
                "refactor: extract helper method"
        );
    }
}
