package io.github.stekeblad.videouploader.youtube.exceptions;

import com.google.api.client.googleapis.json.GoogleJsonError;

public class PlaylistNotFoundException extends YouTubeException {
    public PlaylistNotFoundException() {
        super();
    }

    public PlaylistNotFoundException(GoogleJsonError.ErrorInfo error) {
        super(error);
    }

    public PlaylistNotFoundException(GoogleJsonError.ErrorInfo error, Throwable cause) {
        super(error, cause);
    }
}
