package io.github.stekeblad.youtubeuploader.youtube;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import io.github.stekeblad.youtubeuploader.youtube.constants.Categories;
import io.github.stekeblad.youtubeuploader.youtube.constants.VisibilityStatus;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class VideoUpload extends VideoInformationBase{

    public static final String VIDEO_FILE_FORMAT = "video/";
    public static final String NODE_ID_PROGRESS = "_progress";
    public static final String NODE_ID_UPLOADSTATUS = "_status";
    public static final String NODE_ID_BUTTONSBOX = "_buttons";

    private File videoFile;
    private GridPane uploadPane;

    public File getVideoFile() {
        return this.videoFile;
    }
    public GridPane getUploadPane() {
        return this.uploadPane;
    }

    public void setButton1(Button btn1) {
        ((HBox) uploadPane.lookup("#" + getPaneId() + NODE_ID_BUTTONSBOX)).getChildren().set(0, btn1);
    }

    public void setButton2(Button btn2) {
        ((HBox) uploadPane.lookup("#" + getPaneId() + NODE_ID_BUTTONSBOX)).getChildren().set(1, btn2);
    }

    public void setButton3(Button btn3) {
        ((HBox) uploadPane.lookup("#" + getPaneId() + NODE_ID_BUTTONSBOX)).getChildren().set(2, btn3);
    }

    public VideoUpload(String videoName, String videoDescription, VisibilityStatus visibility, List<String> videoTags,
                       String playlist, Categories category, boolean tellSubs, File thumbNail, String paneName, File videoFile) {

        super(videoName, videoDescription, visibility, videoTags, playlist, category, tellSubs, thumbNail, paneName);
        this.videoFile = videoFile;
        makeUploadPane();
    }

    public static class Builder {
        private String videoName;
        private String videoDescription;
        private VisibilityStatus visibility;
        private List<String> videoTags;
        private String playlist;
        private Categories category;
        private boolean tellSubs;
        private File thumbNail;
        private String paneName;
        private File videoFile;

        public VideoUpload.Builder setVideoName(String videoName) {
            this.videoName = videoName;
            return this;
        }

        public VideoUpload.Builder setVideoDescription(String videoDescription) {
            this.videoDescription = videoDescription;
            return this;
        }

        public VideoUpload.Builder setVisibility(VisibilityStatus visibility) {
            this.visibility = visibility;
            return this;
        }

        public VideoUpload.Builder setVideoTags(List<String> videoTags) {
            this.videoTags = videoTags;
            return this;
        }

        public VideoUpload.Builder setPlaylist(String playlist) {
            this.playlist = playlist;
            return this;
        }

        public VideoUpload.Builder setCategory(Categories category) {
            this.category = category;
            return this;
        }

        public VideoUpload.Builder setTellSubs(boolean tellSubs) {
            this.tellSubs = tellSubs;
            return this;
        }

        public VideoUpload.Builder setThumbNail(File thumbNail) {
            this.thumbNail = thumbNail;
            return this;
        }

        public VideoUpload.Builder setPaneName(String paneName) {
            this.paneName = paneName;
            return this;
        }

        public VideoUpload.Builder setVideoFile(File videoFile) {
            this.videoFile = videoFile;
            return this;
        }

        public VideoUpload build() {
            return new VideoUpload(videoName, videoDescription, visibility, videoTags, playlist, category, tellSubs,
                    thumbNail, paneName, videoFile);
        }
    }

    protected void makeUploadPane() {
        uploadPane = super.getPane();
        uploadPane.setPrefHeight(170);

        ProgressBar progressBar = new ProgressBar();
        progressBar.setId("#" + getPaneId() + NODE_ID_PROGRESS);
        progressBar.setPrefWidth(200);

        Label uploadStatus = new Label("Upload not started");
        uploadStatus.setId(getPaneId() + NODE_ID_UPLOADSTATUS);

        Button ghostBtn1 = new Button("");
        ghostBtn1.setVisible(false);
        Button ghostBtn2 = new Button("");
        ghostBtn2.setVisible(false);
        Button ghostBtn3 = new Button("");
        ghostBtn3.setVisible(false);
        HBox buttonsBox = new HBox(5, ghostBtn1, ghostBtn2, ghostBtn3);
        buttonsBox.setId(getPaneId() + NODE_ID_BUTTONSBOX);

        uploadPane.add(progressBar, 0, 4);
        uploadPane.add(uploadStatus, 1, 4);
        uploadPane.add(buttonsBox, 2, 4);
    }

    public void uploadToTheTube() throws IOException{

        Credential creds = Auth.authUser();
        YouTube myTube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, creds).setApplicationName(
                "Stekeblads Youtube Uploader").build();

        Video videoObject = new Video();

        videoObject.setStatus(new VideoStatus().setPrivacyStatus(getVisibility().getStatusName()));

        VideoSnippet videoMetaData = new VideoSnippet();
        videoMetaData.setTitle(getVideoName());
        videoMetaData.setDescription(getVideoDescription());
        videoMetaData.setTags(getVideoTags());
        videoMetaData.setCategoryId(Integer.toString(getCategory().getId()));
        videoMetaData.setThumbnails(new ThumbnailDetails().setMaxres(new Thumbnail()
                .setUrl(getThumbNail().getCanonicalPath())));

        videoObject.setSnippet(videoMetaData);

        InputStreamContent videoFileStream = new InputStreamContent(VIDEO_FILE_FORMAT,
                new BufferedInputStream(new FileInputStream(videoFile)));

        YouTube.Videos.Insert videoInsert = myTube.videos()
                .insert("snippet,statistics,status", videoObject, videoFileStream);
        videoInsert.setNotifySubscribers(isTellSubs());

        MediaHttpUploader uploader = videoInsert.getMediaHttpUploader();
        uploader.setDirectUploadEnabled(false); // makes the upload resumable!

        MediaHttpUploaderProgressListener progressListener = uploader1 -> {
            switch (uploader1.getUploadState()) {
                case INITIATION_STARTED:
                    setStatusLabelText("Preparing to Upload...");
                    break;
                case INITIATION_COMPLETE:
                    setPaneProgressBarProgress(0);
                    setStatusLabelText("Starting...");
                    break;
                case MEDIA_IN_PROGRESS:
                    setPaneProgressBarProgress(uploader1.getProgress());
                    setStatusLabelText("Uploading: " + Math.floor((uploader1.getProgress()) * 100) + "%");
                    break;
                case MEDIA_COMPLETE:
                    setPaneProgressBarProgress(1);
                    setStatusLabelText("Upload Completed!");
                    break;
                case NOT_STARTED:
                    setStatusLabelText("Upload Not Started");
                    break;
            }
        };
        uploader.setProgressListener(progressListener);

        // finally ready for upload!
        Video uploadedVideo = videoInsert.execute();

        // Set thumbnail if selected
        if (getThumbNail() != null) {
            setStatusLabelText("Setting Thumbnail...");
            File thumbFile = getThumbNail();
            String contentType = Files.probeContentType(Paths.get(thumbFile.toURI()));

            InputStreamContent thumbnailFileContent = new InputStreamContent(
                    contentType, new BufferedInputStream(new FileInputStream(thumbFile)));
            thumbnailFileContent.setLength(thumbFile.length());
            YouTube.Thumbnails.Set thumbnailSet = myTube.thumbnails().set(uploadedVideo.getId(), thumbnailFileContent);
            thumbnailSet.execute();
        }
        // Add to playlist if selected
        if(!getPlaylist().equals("select a playlist") && !getPlaylist().equals("")) {
            setStatusLabelText("Adding to playlist \"" + getPlaylist() + "\"");
            ResourceId resourceId = new ResourceId();
            resourceId.setKind("youtube#video");
            resourceId.setVideoId(uploadedVideo.getId());

            PlaylistUtils playlistUtils = PlaylistUtils.INSTANCE;
            PlaylistItemSnippet playlistSnippet = new PlaylistItemSnippet();
            playlistSnippet.setPlaylistId(playlistUtils.getPlaylistId(getPlaylist()));
            playlistSnippet.setResourceId(resourceId);

            PlaylistItem playlistItem = new PlaylistItem();
            playlistItem.setSnippet(playlistSnippet);
            YouTube.PlaylistItems.Insert playlistInsert = myTube.playlistItems().insert("snippet,contentDetails", playlistItem);
            playlistInsert.execute();
        }
        setStatusLabelText("Upload Completed");
    }

    private void setPaneProgressBarProgress(double progress) {
        if (progress >= 0 && progress <= 1) {
            ((ProgressBar) this.uploadPane.lookup(getPaneId() + NODE_ID_PROGRESS)).setProgress(progress);
        }
    }

    private void setStatusLabelText(String text) {
        ((Label) this.uploadPane.lookup("#" + getPaneId() + NODE_ID_UPLOADSTATUS)).setText(text);
    }

    public void setEditable(boolean newEditStatus) {
        super.setEditable(newEditStatus);
    }

    public String toString() {return super.toString();}
}
