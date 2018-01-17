package io.github.stekeblad.youtubeuploader.youtube;

import io.github.stekeblad.youtubeuploader.youtube.constants.Categories;
import io.github.stekeblad.youtubeuploader.youtube.constants.VisibilityStatus;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.util.List;

public class VideoPreset extends VideoInformationBase {

    public static final String NODE_ID_PRESETNAME = "_presetName";
    public static final String NODE_ID_BUTTONSBOX = "_buttons";

    private GridPane presetPane;

    public String getPresetName() {
        return ((TextField) presetPane.lookup("#" + getPaneId() + NODE_ID_PRESETNAME)).getText();
    }
    public GridPane getPresetPane() {
        return presetPane;
    }

    public void setButton1(Button btn1) {
        ((HBox) presetPane.lookup("#" + getPaneId() + NODE_ID_BUTTONSBOX)).getChildren().set(0, btn1);
    }

    public void setButton2(Button btn2) {
        ((HBox) presetPane.lookup("#" + getPaneId() + NODE_ID_BUTTONSBOX)).getChildren().set(1, btn2);
    }

    public String getButton1Id() {
        return ((HBox) presetPane.lookup("#" + getPaneId() + NODE_ID_BUTTONSBOX)).getChildren().get(0).getId();
    }

    public String getButton2Id() {
        return ((HBox) presetPane.lookup("#" + getPaneId() + NODE_ID_BUTTONSBOX)).getChildren().get(1).getId();
    }

    public VideoPreset(String videoName, String videoDescription, VisibilityStatus visibility, List<String> videoTags,
                       String playlist, Categories category, boolean tellSubs, String thumbNailPath, String paneId, String presetName) {
        super(videoName, videoDescription, visibility, videoTags, playlist, category, tellSubs, thumbNailPath, paneId);
        makePresetPane(presetName);
    }

    public VideoPreset(String fromString, String paneId) throws Exception {
        super(fromString, paneId);

        String presetName = null;

        String[] lines = fromString.split("\n");
        for (String line : lines) {
            int colonIndex = line.indexOf(':');
            if (colonIndex > 0) {
                switch (line.substring(0, colonIndex)) {
                    case NODE_ID_PRESETNAME:
                        presetName = line.substring(colonIndex + 1);
                        break;
                    default:
                        // likely belong to parent
                }

            } //else continue, let the case handle multiline data
        }
        if (presetName == null) {
            throw new Exception("String representation of class does not have presetName");
        }
        makePresetPane(presetName);

    }

    public VideoPreset copy(String paneIdForCopy) {
        return new VideoPreset(getVideoName(), getVideoDescription(), getVisibility(), getVideoTags(), getPlaylist(),
                getCategory(), isTellSubs(), getThumbNail().getAbsolutePath(), paneIdForCopy, getPresetName());
    }

    public static class Builder {
        private String videoName;
        private String videoDescription;
        private VisibilityStatus visibility;
        private List<String> videoTags;
        private String playlist;
        private Categories category;
        private boolean tellSubs;
        private String thumbNailPath;
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

        public VideoPreset.Builder setVisibility(VisibilityStatus visibility) {
            this.visibility = visibility;
            return this;
        }

        public VideoPreset.Builder setVideoTags(List<String> videoTags) {
            this.videoTags = videoTags;
            return this;
        }

        public VideoPreset.Builder setPlaylist(String playlist) {
            this.playlist = playlist;
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

        public VideoPreset.Builder setThumbNailPath(String thumbNailPath) {
            this.thumbNailPath = thumbNailPath;
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
            return new VideoPreset(videoName, videoDescription, visibility, videoTags, playlist, category, tellSubs,
                    thumbNailPath, paneName, presetName);
        }
    }

    protected void makePresetPane(String name) {
        presetPane = super.getPane();
        presetPane.setPrefHeight(170);

        TextField presetName = new TextField();
        presetName.setId(getPaneId() + NODE_ID_PRESETNAME);
        presetName.setPromptText("Preset name");
        presetName.setText(name);
        presetName.setEditable(false);

        Button ghostBtn1 = new Button("");
        ghostBtn1.setVisible(false);
        Button ghostBtn2 = new Button("");
        ghostBtn2.setVisible(false);
        HBox buttonsBox = new HBox(5, ghostBtn1, ghostBtn2);
        buttonsBox.setId(getPaneId() + NODE_ID_BUTTONSBOX);

        presetPane.add(presetName, 0, 4);
        presetPane.add(buttonsBox, 1, 4);
    }

    public void setEditable(boolean newEditStatus) {
        super.setEditable(newEditStatus);
        ((TextField) presetPane.lookup("#" + getPaneId() + NODE_ID_PRESETNAME)).setEditable(newEditStatus);
    }

    public String toString() {
        return super.toString() + "\n" +
                NODE_ID_PRESETNAME + ":" + getPresetName();
    }
}
