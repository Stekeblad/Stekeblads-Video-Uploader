package io.github.stekeblad.youtubeuploader.youtube;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import io.github.stekeblad.youtubeuploader.youtube.constants.Categories;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class Uploader {

    private final String FILE_FORMAT = "video/*";

    private String videoName;
    private String videoDescription;
    private String visibility;
    private List<String> videoTags;
    private InputStream videoFile;
    private Categories category;
    private boolean tellSubs;

    public Uploader(String videoName, String videoDescription, String visibility,
             List<String> videoTags, InputStream videoFile, Categories category,
             boolean tellSubs) {
        this.videoName = videoName;
        this.videoDescription = videoDescription;
        if (visibility == null) { // optional, default to public
            this.visibility = "public";
        } else {
            this.visibility = visibility;
        }
        this.videoTags = videoTags;
        this.videoFile = videoFile;
        this.category = category;
        this.tellSubs = tellSubs;
    }

    public static class Builder {
        private String videoName;
        private String videoDescription;
        private String visibility;
        private List<String> videoTags;
        private InputStream videoFile;
        private Categories category;
        private boolean tellSubs;

        public Uploader.Builder setVideoName(String videoName) {
            this.videoName = videoName;
            return this;
        }

        public Uploader.Builder setVideoDescription(String videoDescription) {
            this.videoDescription = videoDescription;
            return this;
        }

        public Uploader.Builder setVisibility(String visibility) {
            this.visibility = visibility;
            return this;
        }

        public Uploader.Builder setVideoTags(List<String> videoTags) {
            this.videoTags = videoTags;
            return this;
        }

        public Uploader.Builder setVideoFile(InputStream videoFile) {
            this.videoFile = videoFile;
            return this;
        }

        public Uploader.Builder setCategory(Categories category) {
            this.category = category;
            return this;
        }

        public Uploader.Builder setTellSubs(boolean tellSubs) {
            this.tellSubs = tellSubs;
            return this;
        }

        public Uploader build() {
            return new Uploader(videoName, videoDescription, visibility, videoTags, videoFile, category, tellSubs);
        }
    }

    public Video uploadToTheTube() throws IOException{

            Credential creds = Auth.authUser();
        YouTube myTube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, creds).setApplicationName(
                "Stekeblads YoutubeUploader").build();

            Video videoObject = new Video();

            videoObject.setStatus(new VideoStatus().setPrivacyStatus(visibility));

            VideoSnippet videoMetaData = new VideoSnippet();
            videoMetaData.setTitle(videoName);
            videoMetaData.setDescription(videoDescription);
            videoMetaData.setTags(videoTags);
            videoMetaData.setCategoryId(Integer.toString(category.getId()));

            videoObject.setSnippet(videoMetaData);

            InputStreamContent videoFileStream = new InputStreamContent(FILE_FORMAT,
                    videoFile);

            YouTube.Videos.Insert videoInsert = myTube.videos()
                    .insert("snippet,statistics,status", videoObject, videoFileStream);
            videoInsert.setNotifySubscribers(tellSubs);

            MediaHttpUploader uploader = videoInsert.getMediaHttpUploader();
            uploader.setDirectUploadEnabled(false); // makes the upload resumable!

            MediaHttpUploaderProgressListener progressListener = uploader1 -> {
                switch (uploader1.getUploadState()) {
                    case INITIATION_STARTED:
                        System.out.println("Initiation Started");
                        break;
                    case INITIATION_COMPLETE:
                        System.out.println("Initiation Completed");
                        break;
                    case MEDIA_IN_PROGRESS:
                        System.out.println("Upload in progress");
                        System.out.println("Upload percentage: " + uploader1.getProgress());
                        break;
                    case MEDIA_COMPLETE:
                        System.out.println("Upload Completed!");
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
}
