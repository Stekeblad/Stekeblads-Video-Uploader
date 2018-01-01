package io.github.stekeblad.youtubeuploader.youtube;

import io.github.stekeblad.youtubeuploader.youtube.constants.Categories;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.io.File;
import java.util.List;

public class VideoPreset extends VideoInformationBase {

    public static final String NODE_ID_PRESETNAME = "_presetName";

    private GridPane presetPane;

    public String getPresetName() {
        return ((TextField) presetPane.lookup("#" + getPaneId() + NODE_ID_PRESETNAME)).getText();
    }
    public GridPane getPresetPane() {
        return presetPane;
    }

    public VideoPreset(String videoName, String videoDescription, String visibility, List<String> videoTags,
                       Categories category, boolean tellSubs, File thumbNail, String paneId, String presetName) {
        super(videoName, videoDescription, visibility, videoTags, category, tellSubs, thumbNail, paneId);
        makePresetPane(presetName);
    }

    public VideoPreset copy(String paneIdForCopy) {
        return new VideoPreset(getVideoName(), getVideoDescription(), getVisibility(), getVideoTags(), getCategory(),
                isTellSubs(), getThumbNail(), paneIdForCopy, getPresetName());
    }

    public static class Builder {
        private String videoName;
        private String videoDescription;
        private String visibility;
        private List<String> videoTags;
        private Categories category;
        private boolean tellSubs;
        private File thumbNail;
        private String presetName;
        private String paneName;

        public VideoPreset.Builder setVideoName(String videoName) {
            this.videoName = videoName;
            return this;
        }

        public VideoPreset.Builder setVideoDescription(String videoDescription) {
            this.videoDescription = videoDescription;
            return this;
        }

        public VideoPreset.Builder setVisibility(String visibility) {
            this.visibility = visibility;
            return this;
        }

        public VideoPreset.Builder setVideoTags(List<String> videoTags) {
            this.videoTags = videoTags;
            return this;
        }

        public VideoPreset.Builder setCategory(Categories category) {
            this.category = category;
            return this;
        }

        public VideoPreset.Builder setTellSubs(boolean tellSubs) {
            this.tellSubs = tellSubs;
            return this;
        }

        public VideoPreset.Builder setThumbNail(File thumbNail) {
            this.thumbNail = thumbNail;
            return this;
        }

        public VideoPreset.Builder setPresetName(String presetName) {
            this.presetName = presetName;
            return this;
        }

        public VideoPreset.Builder setPaneName(String paneName) {
            this.paneName = paneName;
            return this;
        }

        public VideoPreset build() {
            return new VideoPreset(videoName, videoDescription, visibility, videoTags, category, tellSubs,
                    thumbNail, paneName, presetName);
        }
    }

    protected void makePresetPane(String name) {
        presetPane = super.getPane();
        presetPane.setPrefHeight(150);

        TextField presetName = new TextField();
        presetName.setId(getPaneId() + NODE_ID_PRESETNAME);
        presetName.setPromptText("Preset name");
        presetName.setText(name);
        presetName.setEditable(false);
        presetPane.add(presetName, 0, 5);
    }

    public void setEditable(boolean newEditStatus) {
        super.setEditable(newEditStatus);
        ((TextField) presetPane.lookup("#" + getPaneId() + NODE_ID_PRESETNAME)).setEditable(newEditStatus);
    }
}
