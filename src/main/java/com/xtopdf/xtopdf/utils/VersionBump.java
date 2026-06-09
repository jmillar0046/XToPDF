package com.xtopdf.xtopdf.utils;

/**
 * Utility for computing semantic version bumps based on commit messages.
 *
 * <p>Version bump rules:
 * <ul>
 *   <li>If commit message contains "[major]" → increment major, reset minor and patch to 0</li>
 *   <li>If commit message contains "[minor]" (but not "[major]") → increment minor, reset patch to 0</li>
 *   <li>Otherwise → increment patch</li>
 * </ul>
 */
public final class VersionBump {

    private VersionBump() {
        // utility class
    }

    /**
     * Computes the next semantic version from the current version and a commit message.
     *
     * @param currentVersion the current version in "MAJOR.MINOR.PATCH" format
     * @param commitMessage the commit message to parse for bump keywords
     * @return the next version string
     * @throws IllegalArgumentException if currentVersion is not valid semver
     */
    public static String computeNextVersion(String currentVersion, String commitMessage) {
        String[] parts = currentVersion.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid version format: " + currentVersion);
        }

        int major = Integer.parseInt(parts[0]);
        int minor = Integer.parseInt(parts[1]);
        int patch = Integer.parseInt(parts[2]);

        if (commitMessage.contains("[major]")) {
            major++;
            minor = 0;
            patch = 0;
        } else if (commitMessage.contains("[minor]")) {
            minor++;
            patch = 0;
        } else {
            patch++;
        }

        return major + "." + minor + "." + patch;
    }
}
