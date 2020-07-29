package io.github.stekeblad.videouploader.youtube.exceptions;

import com.google.api.client.googleapis.json.GoogleJsonError;

/**
 * For errors that do not have their own exception class or is like the reason "forbidden"
 * and can mean a lot of different things
 */
public class OtherYouTubeException extends YouTubeException {
    private String moreInfo;

    public OtherYouTubeException() {
        super();
    }

    public OtherYouTubeException(GoogleJsonError.ErrorInfo error) {
        super(error);
    }

    public OtherYouTubeException(GoogleJsonError.ErrorInfo error, Throwable cause) {
        super(error, cause);
    }

    public OtherYouTubeException(GoogleJsonError.ErrorInfo error, String moreInfo, Throwable cause) {
        super(error, cause);
    }

    public String getMessage() {
        if (moreInfo == null)
            return super.getMessage();

        if (errorInfo == null)
            return moreInfo + ": No more details available";

        return moreInfo + ": " + errorInfo.getMessage();
    }
}
