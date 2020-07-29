package io.github.stekeblad.videouploader.youtube.exceptions;

import com.google.api.client.googleapis.json.GoogleJsonError;

public class QuotaLimitExceededException extends YouTubeException {
    public QuotaLimitExceededException() {
        super();
    }

    public QuotaLimitExceededException(GoogleJsonError.ErrorInfo error) {
        super(error);
    }

    public QuotaLimitExceededException(GoogleJsonError.ErrorInfo error, Throwable cause) {
        super(error, cause);
    }
}
