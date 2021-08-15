package io.github.stekeblad.videouploader.models;

import io.github.stekeblad.videouploader.extensions.jfx.objectProperties.FileObjectProperty;
import javafx.beans.property.*;

import java.io.File;

public class NewVideoUploadModel extends NewVideoInfoBaseModel {

    private final FileObjectProperty videoFile = new FileObjectProperty();
    private final StringProperty statusText = new SimpleStringProperty();
    private final StringProperty statusTextLink = new SimpleStringProperty();
    private final DoubleProperty uploadProgress = new SimpleDoubleProperty();

    public File getVideoFile() {
        return videoFile.get();
    }

    public ObjectProperty<File> videoFileProperty() {
        return videoFile;
    }

    public void setVideoFile(File video) {
        videoFile.set(video);
    }

    public String getStatusText() {
        return statusText.get();
    }

    public StringProperty statusTextProperty() {
        return statusText;
    }

    public void setStatusText(String statusLink) {
        this.statusTextLink.set(statusLink);
    }

    public String getStatusTextLink() {
        return statusTextLink.get();
    }

    public StringProperty statusTextLinkProperty() {
        return statusTextLink;
    }

    public void setStatusTextLink(String statusText) {
        this.statusText.set(statusText);
    }

    public double getUploadProgress() {
        return uploadProgress.get();
    }

    public DoubleProperty uploadProgressProperty() {
        return uploadProgress;
    }

    public void setUploadProgress(double uploadProgress) {
        this.uploadProgress.set(uploadProgress);
    }
}
