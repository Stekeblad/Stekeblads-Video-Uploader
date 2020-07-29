package io.github.stekeblad.videouploader.youtube.exceptions;

import com.google.api.client.googleapis.json.GoogleJsonError;

public class InvalidLanguageException extends YouTubeException {
    public InvalidLanguageException() {
        super();
    }

    public InvalidLanguageException(GoogleJsonError.ErrorInfo error) {
        super(error);
    }

    public InvalidLanguageException(GoogleJsonError.ErrorInfo error, Throwable cause) {
        super(error, cause);
    }
}
