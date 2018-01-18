package io.github.stekeblad.youtubeuploader.youtube;

import io.github.stekeblad.youtubeuploader.youtube.constants.Categories;
import io.github.stekeblad.youtubeuploader.youtube.constants.VisibilityStatus;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.io.File;
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

    public String getButton1Id() {
        return ((HBox) uploadPane.lookup("#" + getPaneId() + NODE_ID_BUTTONSBOX)).getChildren().get(0).getId();
    }

    public String getButton2Id() {
        return ((HBox) uploadPane.lookup("#" + getPaneId() + NODE_ID_BUTTONSBOX)).getChildren().get(1).getId();
    }

    public String getButton3Id() {
        return ((HBox) uploadPane.lookup("#" + getPaneId() + NODE_ID_BUTTONSBOX)).getChildren().get(2).getId();
    }

    public VideoUpload(String videoName, String videoDescription, VisibilityStatus visibility, List<String> videoTags,
                       String playlist, Categories category, boolean tellSubs, String thumbNailPath, String paneName, File videoFile) {

        super(videoName, videoDescription, visibility, videoTags, playlist, category, tellSubs, thumbNailPath, paneName);
        this.videoFile = videoFile;
        makeUploadPane();
    }

    public VideoUpload(String fromString, String paneId) throws Exception{
        super(fromString, paneId);
        String[] lines = fromString.split("\n");
        for (String line : lines) {
            int colonIndex = line.indexOf(':');
            if (colonIndex > 0) {
                switch (line.substring(0, colonIndex)) {
                    case "_videofile":
                        videoFile = new File(line.substring((colonIndex + 1)));
                        break;
                    default:
                        // likely belongs to parent
                }
            }
        }
        makeUploadPane();
    }

    public VideoUpload copy(String paneIdCopy) {
        if(paneIdCopy == null) {
            paneIdCopy = getPaneId();
        }
        String thumbnailPath;
        if(getThumbNail() == null) {
            thumbnailPath = null;
        } else {
            thumbnailPath = getThumbNail().getAbsolutePath();
        }
        return new VideoUpload(getVideoName(), getVideoDescription(), getVisibility(), getVideoTags(), getPlaylist(),
                getCategory(), isTellSubs(), thumbnailPath, paneIdCopy, getVideoFile());
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

        public VideoUpload.Builder setThumbNailPath(String thumbNailPath) {
            this.thumbNailPath = thumbNailPath;
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
                    thumbNailPath, paneName, videoFile);
        }
    }

    protected void makeUploadPane() {
        uploadPane = super.getPane();
        uploadPane.setPrefHeight(170);

        ProgressBar progressBar = new ProgressBar();
        progressBar.setId(getPaneId() + NODE_ID_PROGRESS);
        progressBar.setPrefWidth(200);
        progressBar.setVisible(false);

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
        buttonsBox.setMinWidth(200);

        uploadPane.add(progressBar, 0, 4);
        uploadPane.add(uploadStatus, 1, 4);
        uploadPane.add(buttonsBox, 2, 4);
    }

    public void setEditable(boolean newEditStatus) {
        super.setEditable(newEditStatus);
    }

    public String toString() {
        String classString = super.toString();
        classString += "\n_videofile:" + videoFile.getAbsolutePath();
        return classString;
    }
}
