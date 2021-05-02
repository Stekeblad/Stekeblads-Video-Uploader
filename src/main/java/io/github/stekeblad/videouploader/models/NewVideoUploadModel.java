package io.github.stekeblad.videouploader.models;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.File;

public class NewVideoUploadModel extends NewVideoInfoBaseModel {

    private final ObjectProperty<File> videoFile = new SimpleObjectProperty<>();

    public File getVideoFile() {
        return videoFile.get();
    }

    public ObjectProperty<File> videoFileProperty() {
        return videoFile;
    }

    public void setVideoFile(File video) {
        videoFile.set(video);
    }
}
