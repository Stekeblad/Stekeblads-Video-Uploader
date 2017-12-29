package io.github.stekeblad.youtubeuploader.youtube;

import io.github.stekeblad.youtubeuploader.youtube.constants.Categories;

import java.io.InputStream;
import java.util.List;

public class UploaderBuilder {
    private String videoName;
    private String videoDescription;
    private String visibility;
    private List<String> videoTags;
    private InputStream videoFile;
    private Categories category;
    private boolean tellSubs;

    public UploaderBuilder setVideoName(String videoName) {
        this.videoName = videoName;
        return this;
    }

    public UploaderBuilder setVideoDescription(String videoDescription) {
        this.videoDescription = videoDescription;
        return this;
    }

    public UploaderBuilder setVisibility(String visibility) {
        this.visibility = visibility;
        return this;
    }

    public UploaderBuilder setVideoTags(List<String> videoTags) {
        this.videoTags = videoTags;
        return this;
    }

    public UploaderBuilder setVideoFile(InputStream videoFile) {
        this.videoFile = videoFile;
        return this;
    }

    public UploaderBuilder setCategory(Categories category) {
        this.category = category;
        return this;
    }

    public UploaderBuilder setTellSubs(boolean tellSubs) {
        this.tellSubs = tellSubs;
        return this;
    }

    public Uploader createUploader() {
        return new Uploader(videoName, videoDescription, visibility, videoTags, videoFile, category, tellSubs);
    }
}