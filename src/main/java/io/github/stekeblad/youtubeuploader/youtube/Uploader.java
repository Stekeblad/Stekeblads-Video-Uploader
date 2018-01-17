package io.github.stekeblad.youtubeuploader.youtube;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
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

import static io.github.stekeblad.youtubeuploader.youtube.VideoUpload.*;

public class Uploader {
    private HashMap<String, Future> tasks = new HashMap<>();

    private Consumer<String> uploadFinishedCallback = null;

    private ExecutorService exec = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r);
        return t ;
    });

    // the callback will be called with the cancelName of the task that finished
    public void setUploadFinishedCallback(Consumer<String> callback) {
        this.uploadFinishedCallback = callback;
    }

    public boolean abortUpload(String cancelName) {
        return tasks.get(cancelName).cancel(true);
    }

    public boolean getIsActive() {
        return !tasks.keySet().isEmpty();
    }

    // returns the cancelName of all unfinished uploads
    public Set<String> kill() {
        exec.shutdownNow();
        return tasks.keySet();
    }

    public void add(VideoUpload video, String cancelName) {
        Future upload = exec.submit(new Task<Void>() {
            @Override
            public Void call() {
                try {
                    upload(video);
                } catch (Exception e) {
                    if(e.getClass().getName().equals("InterruptedException")) {
                        System.out.println("Interrupted upload of: " + video.getVideoName());
                    } else {
                        e.printStackTrace();
                    }
                }
                if(uploadFinishedCallback != null) {
                    uploadFinishedCallback.accept(cancelName);
                }
                tasks.remove(cancelName);
                return null ;
            }
        });
        tasks.put(cancelName, upload); // save the future to be able to abort upload
    }

    private void upload(VideoUpload video) throws IOException {

        Credential creds = Auth.authUser();
        YouTube myTube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, creds).setApplicationName(
                "Stekeblads Youtube Uploader").build();

        Video videoObject = new Video();

        videoObject.setStatus(new VideoStatus().setPrivacyStatus(video.getVisibility().getStatusName()));

        VideoSnippet videoMetaData = new VideoSnippet();
        videoMetaData.setTitle(video.getVideoName());
        videoMetaData.setDescription(video.getVideoDescription());
        videoMetaData.setTags(video.getVideoTags());
        videoMetaData.setCategoryId(Integer.toString(video.getCategory().getId()));

        videoObject.setSnippet(videoMetaData);

        InputStreamContent videoFileStream = new InputStreamContent(VIDEO_FILE_FORMAT,
                new BufferedInputStream(new FileInputStream(video.getVideoFile())));

        YouTube.Videos.Insert videoInsert = myTube.videos()
                .insert("snippet,statistics,status", videoObject, videoFileStream);
        videoInsert.setNotifySubscribers(video.isTellSubs());

        MediaHttpUploader uploader = videoInsert.getMediaHttpUploader();
        uploader.setDirectUploadEnabled(false); // makes the upload resumable?

        MediaHttpUploaderProgressListener progressListener = uploader1 -> {
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
                    setStatusLabelText("Uploading: " + Math.floor(
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
        if (!video.getPlaylist().equals("select a playlist") && !video.getPlaylist().equals("")) {
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


    private void setPaneProgressBarProgress(double progress, VideoUpload video) {
        if (progress >= 0 && progress <= 1) {
            Platform.runLater(() ->
            ((ProgressBar) video.getUploadPane().lookup("#" + video.getPaneId() + NODE_ID_PROGRESS)).setProgress(progress));
        }
    }

    private void setStatusLabelText(String text, VideoUpload video) {
        Platform.runLater(() ->
        ((Label) video.getUploadPane().lookup("#" + video.getPaneId() + NODE_ID_UPLOADSTATUS)).setText(text));
    }

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
