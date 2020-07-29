package io.github.stekeblad.videouploader.youtube.exceptions;

import com.google.api.client.googleapis.json.GoogleJsonError;

public class InvalidMissingImageException extends YouTubeException {
    public InvalidMissingImageException() {
        super();
    }

    public InvalidMissingImageException(GoogleJsonError.ErrorInfo error) {
        super(error);
    }

    public InvalidMissingImageException(GoogleJsonError.ErrorInfo error, Throwable cause) {
        super(error, cause);
    }
}
