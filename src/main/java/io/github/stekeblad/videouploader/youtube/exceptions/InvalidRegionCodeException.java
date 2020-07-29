package io.github.stekeblad.videouploader.youtube.exceptions;

import com.google.api.client.googleapis.json.GoogleJsonError;

public class InvalidRegionCodeException extends YouTubeException {
    public InvalidRegionCodeException() {
        super();
    }

    public InvalidRegionCodeException(GoogleJsonError.ErrorInfo error) {
        super(error);
    }

    public InvalidRegionCodeException(GoogleJsonError.ErrorInfo error, Throwable cause) {
        super(error, cause);
    }
}

