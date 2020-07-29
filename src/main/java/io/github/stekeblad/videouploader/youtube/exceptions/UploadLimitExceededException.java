package io.github.stekeblad.videouploader.youtube.exceptions;

import com.google.api.client.googleapis.json.GoogleJsonError;

public class UploadLimitExceededException extends YouTubeException {
    public UploadLimitExceededException() {
        super();
    }

    public UploadLimitExceededException(GoogleJsonError.ErrorInfo error) {
        super(error);
    }

    public UploadLimitExceededException(GoogleJsonError.ErrorInfo error, Throwable cause) {
        super(error, cause);
    }
}
