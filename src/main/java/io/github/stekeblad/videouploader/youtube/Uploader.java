package io.github.stekeblad.videouploader.youtube;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.HttpBackOffIOExceptionHandler;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import io.github.stekeblad.videouploader.youtube.utils.CategoryUtils;
import io.github.stekeblad.videouploader.youtube.utils.PlaylistUtils;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.awt.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import static io.github.stekeblad.videouploader.youtube.VideoUpload.*;

/**
 * Uploader handles the actual uploading to Youtube and contains a queue for all uploads. New uploads can be added,
 * existing once can be aborted all at the same time or just a specific. It is possible to get if their is a upload
 * in progress and set a method to be called for all finished uploads with the Id of the upload as the only parameter
 */
public class Uploader {
    private HashMap<String, Future> tasks = new HashMap<>();
    private CategoryUtils categoryUtils = CategoryUtils.INSTANCE;

    private Consumer<String> uploadFinishedCallback = null;

    private ExecutorService exec = Executors.newSingleThreadExecutor(Thread::new);

    /**
     * Sets a method to be called every time a upload finishes. The parameter given to the callback will be the cancelName
     * that was given in the add() method.
     * @param callback the callback to be called after every finished upload.
     */
    public void setUploadFinishedCallback(Consumer<String> callback) {
        this.uploadFinishedCallback = callback;
    }

    /**
     * Aborts a single upload, scheduled or active
     * @param cancelName the cancelName that was given when the add() method was called
     * @return true if the upload was aborted, false if it for some reason is not possible to abort it.
     */
    public boolean abortUpload(String cancelName) {
        return tasks.get(cancelName).cancel(true);
    }

    /**
     *
     * @return true if a upload is in progress, false if not.
     */
    public boolean getIsActive() {
        return !tasks.keySet().isEmpty();
    }

    /**
     * Aborts all uploads
     * @return the cancelName of all unfinished uploads
     */
    public Set<String> kill() {
        exec.shutdownNow();
        return tasks.keySet();
    }

    /**
     * Adds video to the upload list
     * @param video video to upload
     * @param cancelName String to use for aborting the upload and used to report that its finished
     */
    public void add(VideoUpload video, String cancelName) {
        // Create the task
        Future upload = exec.submit(new Task<Void>() {
            @Override
            // Define what it does
            public Void call() {
                try {
                    // Do the uploading
                    upload(video);
                } catch (Exception e) {
                    // if not interrupted by the user, print the error
                    if (! e.getMessage().equals("INTERRUPTED")) {
                        e.printStackTrace();
                    }
                    return null; // do not use callback on upload that throws exception
                }
                // If upload finished without errors and callback is set, give the cancel name to the callback
                if(uploadFinishedCallback != null) {
                    uploadFinishedCallback.accept(cancelName);
                }
                // Remove the task from the list
                tasks.remove(cancelName);
                return null ;
            }
        });
        tasks.put(cancelName, upload); // save the future to be able to abort upload
    }

    /**
     * Does the uploading
     * @param video a VideoUpload with all the details needed for uploading
     * @throws IOException if the user aborts the upload while it is uploading, there is a exception while reading the video
     * or thumbnail file or there is a network error that could not be handled.
     */
    private void upload(VideoUpload video) throws IOException {

        // Auth the user and create the Youtube object
        Credential creds = Auth.authUser();
        YouTube myTube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, request -> {
            creds.initialize(request);
            // Tell Youtube to attempt resume upload if a network error occur.
            request.setIOExceptionHandler(new HttpBackOffIOExceptionHandler(new ExponentialBackOff()));
        }).setApplicationName("Stekeblads Video Uploader").build();

        // Start building the Youtube video object
        Video videoObject = new Video();

        videoObject.setStatus(new VideoStatus().setPrivacyStatus(video.getVisibility().getStatusName()));

        VideoSnippet videoMetaData = new VideoSnippet();
        videoMetaData.setTitle(video.getVideoName());
        videoMetaData.setDescription(video.getVideoDescription());
        videoMetaData.setTags(video.getVideoTags());
        videoMetaData.setCategoryId(categoryUtils.getCategoryId(video.getCategory()));

        videoObject.setSnippet(videoMetaData);

        InputStreamContent videoFileStream = new InputStreamContent(VIDEO_FILE_FORMAT,
                new BufferedInputStream(new FileInputStream(video.getVideoFile())));

        YouTube.Videos.Insert videoInsert = myTube.videos()
                .insert("snippet,statistics,status", videoObject, videoFileStream);
        videoInsert.setNotifySubscribers(video.isTellSubs());

        // getMediaHttpUploader for being able to report progress
        MediaHttpUploader uploader = videoInsert.getMediaHttpUploader();
        uploader.setDirectUploadEnabled(false); // makes the upload resumable?

        MediaHttpUploaderProgressListener progressListener = uploader1 -> {
            // If user has aborted this upload while it is uploading
            if(Thread.interrupted()) {
                // Throw a exception (Only IOException allowed)
                throw new IOException("INTERRUPTED");
            }
            switch (uploader1.getUploadState()) {
                case INITIATION_STARTED:
                    setStatusLabelText("Preparing to Upload...", video);
                    break;
                case INITIATION_COMPLETE:
                    setPaneProgressBarProgress(0, video);
                    setStatusLabelText("Starting...", video);
                    break;
                case MEDIA_IN_PROGRESS: // uploader1.getProgress() errors, this is not a perfect replacement as
                    // the upload is slightly larger than the video file, but for longer videos it will be close enough
                    setPaneProgressBarProgress((double) uploader1.getNumBytesUploaded() / video.getVideoFile().length(), video);
                    setStatusLabelText("Uploading: " + (int) Math.floor(
                            ((double) uploader1.getNumBytesUploaded() / video.getVideoFile().length()) * 100) + "%", video);
                    break;
                case MEDIA_COMPLETE:
                    setPaneProgressBarProgress(1, video);
                    setStatusLabelText("Upload Completed!", video);
                    break;
                case NOT_STARTED:
                    setStatusLabelText("Upload Not Started", video);
                    break;
            }
        };
        uploader.setProgressListener(progressListener);

        // finally ready for upload!
        Video uploadedVideo = videoInsert.execute();

        // Set thumbnail if selected
        if (video.getThumbNail() != null) {
            setStatusLabelText("Setting Thumbnail...", video);
            File thumbFile = video.getThumbNail();
            String contentType = Files.probeContentType(Paths.get(thumbFile.toURI()));

            InputStreamContent thumbnailFileContent = new InputStreamContent(
                    contentType, new BufferedInputStream(new FileInputStream(thumbFile)));
            thumbnailFileContent.setLength(thumbFile.length());
            YouTube.Thumbnails.Set thumbnailSet = myTube.thumbnails().set(uploadedVideo.getId(), thumbnailFileContent);
            thumbnailSet.execute();
        }
        // Add to playlist if selected
        String playlistString = video.getPlaylist();
        if (playlistString != null && !playlistString.equals("null") &&
                !playlistString.equals("select a playlist") && !playlistString.equals("")) {
            setStatusLabelText("Adding to playlist \"" + video.getPlaylist() + "\"", video);
            ResourceId resourceId = new ResourceId();
            resourceId.setKind("youtube#video");
            resourceId.setVideoId(uploadedVideo.getId());

            PlaylistUtils playlistUtils = PlaylistUtils.INSTANCE;
            PlaylistItemSnippet playlistSnippet = new PlaylistItemSnippet();
            playlistSnippet.setPlaylistId(playlistUtils.getPlaylistId(video.getPlaylist()));
            playlistSnippet.setResourceId(resourceId);

            PlaylistItem playlistItem = new PlaylistItem();
            playlistItem.setSnippet(playlistSnippet);
            YouTube.PlaylistItems.Insert playlistInsert = myTube.playlistItems().insert("snippet,contentDetails", playlistItem);
            playlistInsert.execute();
        }
        setStatusLabelText("Done! Video is here: https://youtu.be/" + uploadedVideo.getId(), video);
        makeLabelClickable("https://youtu.be/" + uploadedVideo.getId(), video);
    }

    /**
     * Updates the progress bar in the UI
     * @param progress the upload progress as a value between 0 and 1
     * @param video the video object with the progress bar to update
     */
    private void setPaneProgressBarProgress(double progress, VideoUpload video) {
        if (progress >= 0 && progress <= 1) {
            Platform.runLater(() ->
            ((ProgressBar) video.getUploadPane().lookup("#" + video.getPaneId() + NODE_ID_PROGRESS)).setProgress(progress));
        }
    }

    /**
     * Sets the text to be displayed on the status label in the UI
     * @param text the text to show
     * @param video the video object that owns the status label to update
     */
    private void setStatusLabelText(String text, VideoUpload video) {
        Platform.runLater(() ->
        ((Label) video.getUploadPane().lookup("#" + video.getPaneId() + NODE_ID_UPLOADSTATUS)).setText(text));
    }

    /**
     * Sets a URL to open when the status label is clicked
     * @param url the url to open
     * @param video the video object that owns the status label to make clickable.
     */
    private void makeLabelClickable(String url, VideoUpload video) {
        Platform.runLater(() ->
                video.getUploadPane().lookup("#" + video.getPaneId() + NODE_ID_UPLOADSTATUS)
                        .setOnMouseClicked(event -> {
                            if (Desktop.isDesktopSupported()) {
                                try {
                                    Desktop.getDesktop().browse(new URI(url));
                                } catch (Exception e) {
                                    System.err.println("Could not make upload label clickable");
                                }
                            }
                        }));
    }


}
