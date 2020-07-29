package io.github.stekeblad.videouploader.youtube.exceptions;

import com.google.api.client.googleapis.json.GoogleJsonError;

public class PlaylistFullException extends YouTubeException {
    public PlaylistFullException() {
        super();
    }

    public PlaylistFullException(GoogleJsonError.ErrorInfo error) {
        super(error);
    }

    public PlaylistFullException(GoogleJsonError.ErrorInfo error, Throwable cause) {
        super(error, cause);
    }
}
