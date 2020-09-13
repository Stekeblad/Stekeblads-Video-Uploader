package io.github.stekeblad.videouploader.utils.exceptionHistory;

import java.time.Duration;
import java.time.Instant;

/**
 * ExceptionHistoryPost represents one exception that has happened and contains its stacktrace and the
 * {@link java.time.Instant} the post was created. ExceptionHistoryPost is used by
 * {@link io.github.stekeblad.videouploader.utils.exceptionHistory.ExceptionHistory} to track exceptions that recently
 * been caught and prevent multiple error dialogs from displaying the same error message in rapid succession.
 */
class ExceptionHistoryPost {
    private final String Stacktrace;
    private final Instant TimeAdded;

    ExceptionHistoryPost(String stacktrace) {
        Stacktrace = stacktrace;
        TimeAdded = Instant.now();
    }

    String getStacktrace() {
        return Stacktrace;
    }

    /**
     * @return the time between the current instant and the instant the ExceptionHistoryPost was created
     */
    Duration getTimeSinceAdded() {
        return Duration.between(TimeAdded, Instant.now());
    }
}
