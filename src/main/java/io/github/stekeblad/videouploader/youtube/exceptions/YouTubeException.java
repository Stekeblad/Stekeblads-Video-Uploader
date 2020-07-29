package io.github.stekeblad.videouploader.youtube.exceptions;

import com.google.api.client.googleapis.json.GoogleJsonError;

import java.io.IOException;

/**
 * Base class for all custom exceptions implemented for different
 * errors returned by the YouTube API
 */
public abstract class YouTubeException extends IOException {
    protected GoogleJsonError.ErrorInfo errorInfo;

    YouTubeException() {
        super();
    }

    YouTubeException(GoogleJsonError.ErrorInfo error) {
        super();
        errorInfo = error;
    }

    YouTubeException(GoogleJsonError.ErrorInfo error, Throwable cause) {
        super(cause);
        errorInfo = error;
    }

    public GoogleJsonError.ErrorInfo getErrorInfo() {
        if (errorInfo == null)
            return null;
        return errorInfo;
    }

    public String getReason() {
        if (errorInfo == null)
            return "";
        return errorInfo.getReason();
    }

    public String getMessage() {
        if (errorInfo == null)
            return getClass().getName() + ": No more details available";
        return errorInfo.getReason() + ": " + errorInfo.getMessage();
    }

    public String toString() {
        return getMessage();
    }
}
