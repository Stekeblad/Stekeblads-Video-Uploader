package io.github.stekeblad.videouploader.updater;

import org.jetbrains.annotations.NotNull;

/**
 * Small class for storing a version number and comparing two version to see if one is greater than the other.
 * The version number is required to be exactly in three parts, eg. 1.4.0
 */
public class VersionFormat implements Comparable<VersionFormat> {
    private int major;
    private int minor;
    private int fix;

    /**
     * Create a new VersionFormat with parameters for major, minor and fix versions
     *
     * @param major major release number
     * @param minor minor release number
     * @param fix   fix number
     */
    public VersionFormat(int major, int minor, int fix) {
        this.major = major;
        this.minor = minor;
        this.fix = fix;
    }

    /**
     * Create a new Version format with the version initialized to 0.0.0
     */
    public VersionFormat() {
        this.major = 0;
        this.minor = 0;
        this.fix = 0;
    }

    /**
     * Parse a version number from a string. It needs to be exactly three parts separated by period '.' Each part needs
     * to be a positive integer between 0 and Integer.MAX_VALUE
     *
     * @param versionString Version number in its string format.
     * @throws IllegalArgumentException if versionString does not contain exactly three segments separated by a period
     *                                  or if any of the segments could not be parsed to a positive integer.
     */
    public VersionFormat(String versionString) {
        String[] segments = versionString.split("\\.");
        if (segments.length != 3)
            throw new IllegalArgumentException(
                    "The version needs to be on the format major.minor.fix where major, minor and fix are positive integers");
        try {
            this.major = Integer.parseUnsignedInt(segments[0]);
            this.minor = Integer.parseUnsignedInt(segments[1]);
            this.fix = Integer.parseUnsignedInt(segments[2]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "The version needs to be on the format major.minor.fix where major, minor and fix are positive integers");
        }
    }

    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public int getFix() {
        return fix;
    }

    public void setFix(int fix) {
        this.fix = fix;
    }

    /**
     * @return the VersionFormat as a string formatted as major.minor.fix
     */
    public String toString() {
        return this.major + "." + this.minor + "." + this.fix;
    }

    @Override
    public int compareTo(@NotNull VersionFormat that) {
        // negative if this is smaller than that
        // positive if this is greater than that
        // zero if this and that are equal
        if (this.major < that.major)
            return -1;
        else if (this.major > that.major)
            return 1;

        if (this.minor < that.minor)
            return -1;
        else if (this.minor > that.minor)
            return 1;

        if (this.fix < that.fix)
            return -1;
        else if (this.fix > that.fix)
            return 1;

        return 0;
    }
}
