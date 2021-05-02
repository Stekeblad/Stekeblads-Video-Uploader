package io.github.stekeblad.videouploader.youtube;

/**
 * A Enum for the different video visibility statues that Youtube have
 */
public enum VisibilityStatus {
    PUBLIC("public"),
    PRIVATE("private"),
    UNLISTED("unlisted");

    private final String status;

    VisibilityStatus(String status) {
        this.status = status;
    }

    public String getStatusName() {
        return status;
    }

    public boolean equals(String str) {
        return str.equals(this.status);
    }
}

