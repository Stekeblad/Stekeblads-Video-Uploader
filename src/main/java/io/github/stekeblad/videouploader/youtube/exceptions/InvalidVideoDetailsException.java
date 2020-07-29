package io.github.stekeblad.videouploader.youtube.exceptions;

import com.google.api.client.googleapis.json.GoogleJsonError;

public class InvalidVideoDetailsException extends YouTubeException {
    public InvalidVideoDetailsException() {
        super();
    }

    public InvalidVideoDetailsException(GoogleJsonError.ErrorInfo error) {
        super(error);
    }

    public InvalidVideoDetailsException(GoogleJsonError.ErrorInfo error, Throwable cause) {
        super(error, cause);
    }
}
