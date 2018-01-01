package io.github.stekeblad.youtubeuploader.youtube;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import io.github.stekeblad.youtubeuploader.youtube.constants.Categories;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class VideoUpload extends VideoInformationBase{

    public static final String VIDEO_FILE_FORMAT = "video/*";
    public static final String NODE_ID_PROGRESS = "_progress";

    private File videoFile;
    private GridPane uploadPane;

    public File getVideoFile() {
        return this.videoFile;
    }
    public GridPane getUploadPane() {
        return this.uploadPane;
    }

    public VideoUpload(String videoName, String videoDescription, String visibility, List<String> videoTags,
                       Categories category, boolean tellSubs, File videoFile, File thumbNail, String paneName) {

        super(videoName, videoDescription, visibility, videoTags, category, tellSubs, thumbNail, paneName);
        this.videoFile = videoFile;
        makeUploadPane();
    }

    // Should not need to be copied in the way VideoPreset is. Do not upload identical videos.

    public static class Builder {
        private String videoName;
        private String videoDescription;
        private String visibility;
        private List<String> videoTags;
        private Categories category;
        private boolean tellSubs;
        private File videoFile;
        private File thumbNail;
        private String paneName;

        public VideoUpload.Builder setVideoName(String videoName) {
            this.videoName = videoName;
            return this;
        }

        public VideoUpload.Builder setVideoDescription(String videoDescription) {
            this.videoDescription = videoDescription;
            return this;
        }

        public VideoUpload.Builder setVisibility(String visibility) {
            this.visibility = visibility;
            return this;
        }

        public VideoUpload.Builder setVideoTags(List<String> videoTags) {
            this.videoTags = videoTags;
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

        public VideoUpload.Builder setVideoFile(File videoFile) {
            this.videoFile = videoFile;
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

        public VideoUpload build() {
            return new VideoUpload(videoName, videoDescription, visibility, videoTags, category, tellSubs,
                    videoFile, thumbNail, paneName);
        }
    }

    protected void makeUploadPane() {
        uploadPane = super.getPane();
        uploadPane.setPrefHeight(150);

        ProgressBar progressBar = new ProgressBar();
        progressBar.setId("#" + getPaneId() + NODE_ID_PROGRESS);
        progressBar.setPrefWidth(200);

        uploadPane.add(progressBar, 0, 5);
    }

    public Video uploadToTheTube() throws IOException{

            Credential creds = Auth.authUser();
        YouTube myTube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, creds).setApplicationName(
                "Stekeblads Youtube Uploader").build();

            Video videoObject = new Video();

            videoObject.setStatus(new VideoStatus().setPrivacyStatus(getVisibility()));

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
                        System.out.println("Initiation Started");
                        break;
                    case INITIATION_COMPLETE:
                        System.out.println("Initiation Completed");
                        setPaneProgressBarProgress(0);
                        break;
                    case MEDIA_IN_PROGRESS:
                        System.out.println("Upload in progress");
                        System.out.println("Upload percentage: " + uploader1.getProgress());
                        setPaneProgressBarProgress(uploader1.getProgress());
                        break;
                    case MEDIA_COMPLETE:
                        System.out.println("Upload Completed!");
                        setPaneProgressBarProgress(1);
                        break;
                    case NOT_STARTED:
                        System.out.println("Upload Not Started!");
                        break;
                }
            };
            uploader.setProgressListener(progressListener);

            // finally ready for upload!
            return videoInsert.execute();
    }

    private void setPaneProgressBarProgress(double progress) {
        if (progress >= 0 && progress <= 1) {
            ((ProgressBar) this.uploadPane.lookup("#test" + NODE_ID_PROGRESS)).setProgress(progress);
        }
    }

    public void setEditable(boolean newEditStatus) {
        super.setEditable(newEditStatus);
    }
}
