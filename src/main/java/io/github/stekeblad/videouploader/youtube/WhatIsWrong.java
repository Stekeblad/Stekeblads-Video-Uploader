package io.github.stekeblad.videouploader.youtube;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import io.github.stekeblad.videouploader.youtube.exceptions.*;

import java.io.IOException;
import java.util.List;

public class WhatIsWrong {
    /**
     * If the provided exception is an instance of GoogleJsonResponseException then
     * the exception reason is inspected and a custom, more specific exception is thrown.
     *
     * @param exception an exception to inspect.
     * @throws YouTubeException if the given exception is a GoogleJsonResponseException
     *                          and it has a response error set.
     */
    public static void classifyException(IOException exception) throws YouTubeException {
        if (!(exception instanceof GoogleJsonResponseException))
            return; //throw new IOException("Unclassifiable exception");

        GoogleJsonResponseException gjre = (GoogleJsonResponseException) exception;
        GoogleJsonError details = gjre.getDetails();
        if (details == null) // if true then this is not an error(?)
            return;

        List<GoogleJsonError.ErrorInfo> errors = details.getErrors();
        if (errors.isEmpty())
            throw new OtherYouTubeException(null, "No error details received", exception);

        // Let's hope that there never is more than one error,
        // or that the first error in the list is the most useful.
        GoogleJsonError.ErrorInfo errorInfo = errors.get(0);

        switch (errorInfo.getReason()) {
            // Can happen anytime section
            case "quotaExceeded":
                throw new QuotaLimitExceededException(errorInfo, exception);
            case "forbidden":
                throw new OtherYouTubeException(errorInfo, "Forbidden / Not allowed", exception);

                // Related to categories
            case "invalidRegionCode":
            case "unsupportedRegionCode":
                throw new InvalidRegionCodeException(errorInfo, exception);
            case "invalidLanguage":
            case "unsupportedLanguageCode":
                throw new InvalidLanguageException(errorInfo, exception);

                // Related to playlists
            case "playlistContainsMaximumNumberOfVideos":
                throw new PlaylistFullException(errorInfo, exception);
            case "playlistNotFound":
                throw new PlaylistNotFoundException(errorInfo, exception);

                // Related to thumbnails
            case "invalidImage":
            case "mediaBodyRequired":
                throw new InvalidMissingImageException(errorInfo, exception);

                // Related to video upload
            case "invalidDescription":
            case "invalidFilename":
            case "invalidTags":
            case "invalidTitle":
                throw new InvalidVideoDetailsException(errorInfo, exception);
            case "uploadLimitExceeded":
                throw new UploadLimitExceededException(errorInfo, exception);

            default:
                throw new OtherYouTubeException(errorInfo, exception);
        }


    }
}
