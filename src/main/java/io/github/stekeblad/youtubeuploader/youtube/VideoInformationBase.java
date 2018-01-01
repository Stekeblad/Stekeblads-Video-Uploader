package io.github.stekeblad.youtubeuploader.youtube;

import io.github.stekeblad.youtubeuploader.youtube.constants.Categories;
import io.github.stekeblad.youtubeuploader.youtube.constants.VisibilityStatus;
import javafx.collections.FXCollections;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public class VideoInformationBase {

    // Constants
    public static final List<String> THUMBNAIL_FILE_FORMAT = Arrays.asList("*.jpg", "*.png"); //fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Video Files", THUMBNAIL_FILE_FORMAT));
    public static final String NODE_ID_TITLE = "_title";
    public static final String NODE_ID_DESCRIPTION = "_description";
    public static final String NODE_ID_CATEGORY = "_category";
    public static final String NODE_ID_TAGS = "_tags";
    public static final String NODE_ID_PLAYLIST = "_playlist";
    public static final String NODE_ID_VISIBILITY = "_visibility";
    public static final String NODE_ID_TELLSUBS = "_tellSubs";

    // Variables
    private String videoName;
    private String videoDescription;
    private String visibility;
    private List<String> videoTags;
    private Categories category;
    private boolean tellSubs;
    private File thumbNail;
    private String paneId;
    private GridPane videoBasePane;

    public String getVideoName() {
        return ((TextField) videoBasePane.lookup("#" + paneId + NODE_ID_TITLE)).getText();
    }
    public String getVideoDescription() {
        return ((TextArea) videoBasePane.lookup("#" + paneId + NODE_ID_DESCRIPTION)).getText();
    }
    @SuppressWarnings("unchecked")
    public String getVisibility() {
        return ((ChoiceBox<String>) videoBasePane.lookup("#" + paneId + NODE_ID_VISIBILITY)).getSelectionModel().getSelectedItem();
    }
    public List<String> getVideoTags() {
        return new ArrayList<>(Arrays.asList(((TextArea) videoBasePane.lookup("#" + paneId + NODE_ID_TAGS)).getText().split(", ")));
    }
    @SuppressWarnings("unchecked")
    public Categories getCategory() {
        return Categories.valueOf(((ChoiceBox<String>) videoBasePane.lookup("#" + paneId + NODE_ID_CATEGORY)).getSelectionModel().getSelectedItem());
    }
    @SuppressWarnings("unchecked")
    public boolean isTellSubs() { // only two choices, do notify subscribers is the second choice (index 1)
        return (((ChoiceBox<String>) videoBasePane.lookup("#" + paneId + NODE_ID_TELLSUBS)).getSelectionModel().isSelected(1));
    }
    public File getThumbNail() {
        return thumbNail;
    }
    public String getPaneId() {
        return paneId;
    }
    public GridPane getPane() {
        return videoBasePane;
    }

    public VideoInformationBase(String videoName, String videoDescription, String visibility, List<String> videoTags,
                                Categories category, boolean tellSubs, File thumbNail, String paneId) {
        this.videoName = videoName;
        this.videoDescription = videoDescription;
        if (visibility == null) { // optional, default to public
            this.visibility = VisibilityStatus.PUBLIC.getStatusName();
        } else {
            this.visibility = visibility;
        }
        this.videoTags = videoTags;
        this.category = category;
        this.tellSubs = tellSubs;
        this.thumbNail = thumbNail;
        this.paneId = paneId;

        makeVideoBasePane();
    }

    public VideoInformationBase copy(String paneIdForCopy) {
        return new VideoInformationBase(getVideoName(), getVideoDescription(), getVisibility(), getVideoTags(), getCategory(), isTellSubs(), getThumbNail(), paneIdForCopy);
    }

    public static class Builder {
        private String videoName;
        private String videoDescription;
        private String visibility;
        private List<String> videoTags;
        private Categories category;
        private boolean tellSubs;
        private File thumbNail;
        private String paneName;

        public VideoInformationBase.Builder setVideoName(String videoName) {
            this.videoName = videoName;
            return this;
        }

        public VideoInformationBase.Builder setVideoDescription(String videoDescription) {
            this.videoDescription = videoDescription;
            return this;
        }

        public VideoInformationBase.Builder setVisibility(String visibility) {
            this.visibility = visibility;
            return this;
        }

        public VideoInformationBase.Builder setVideoTags(List<String> videoTags) {
            this.videoTags = videoTags;
            return this;
        }

        public VideoInformationBase.Builder setCategory(Categories category) {
            this.category = category;
            return this;
        }

        public VideoInformationBase.Builder setTellSubs(boolean tellSubs) {
            this.tellSubs = tellSubs;
            return this;
        }

        public VideoInformationBase.Builder setThumbNail(File thumbNail) {
            this.thumbNail = thumbNail;
            return this;
        }

        public VideoInformationBase.Builder setPaneName(String paneName) {
            this.paneName = paneName;
            return this;
        }

        public VideoInformationBase build() {
            return new VideoInformationBase(videoName, videoDescription, visibility, videoTags, category,
                    tellSubs, thumbNail, paneName);
        }
    }

     protected void makeVideoBasePane() {
        videoBasePane = new GridPane();
        videoBasePane.setId(paneId);
        videoBasePane.setPrefSize(500, 100);
        videoBasePane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

        TextField title = new TextField();
        title.setId(paneId + NODE_ID_TITLE);
        title.setPromptText("Video title");
        title.setText(this.videoName);
        title.setEditable(false);

        TextArea description = new TextArea();
        description.setId(paneId + NODE_ID_DESCRIPTION);
        description.setPromptText("Video description");
        description.setText(this.videoDescription);
        description.setEditable(false);
        description.setWrapText(true);

        ArrayList<Categories> categories = new ArrayList<>(EnumSet.allOf(Categories.class));
        ArrayList<String> categoryStrings = new ArrayList<>();
        for (Categories category1 : categories) {
            categoryStrings.add(category1.getName());
        }
        ChoiceBox<String> categoryChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList(categoryStrings));
        categoryChoiceBox.setId(paneId + NODE_ID_CATEGORY);
        categoryChoiceBox.getSelectionModel().select(this.category.getName());
        categoryChoiceBox.setTooltip(new Tooltip("Youtube Video Category"));
        categoryChoiceBox.setDisable(true);

        TextArea tags = new TextArea();
        tags.setId(paneId + NODE_ID_TAGS);
        tags.setPromptText("list, of, tags, separated, with, comma, and, space");
        StringBuilder tagsString = new StringBuilder();
        for (int i = 0; i < this.videoTags.size() - 1; i++) {
            tagsString.append(this.videoTags.get(i)).append(", ");
        }
        if (videoTags.size() > 0) {
            tagsString.append(this.videoTags.get(this.videoTags.size() - 1));
        }
        tags.setText(tagsString.toString());
        tags.setEditable(false);

        TextField playlist = new TextField();
        playlist.setId(paneId + NODE_ID_PLAYLIST);
        playlist.setPromptText("playlist url"); //todo playlist
        playlist.setEditable(false);

        ArrayList<VisibilityStatus> statuses = new ArrayList<>(EnumSet.allOf(VisibilityStatus.class));
        ArrayList<String> visibilityStrings = new ArrayList<>();
        for (VisibilityStatus status : statuses) {
            visibilityStrings.add(status.getStatusName());
        }
        ChoiceBox<String> visibility = new ChoiceBox<>(FXCollections.observableArrayList(visibilityStrings));
        visibility.setId(paneId + NODE_ID_VISIBILITY);
        visibility.getSelectionModel().select(this.visibility);
        visibility.setTooltip(new Tooltip("how will the video be accessible?"));
        visibility.setDisable(true);

        ArrayList<String> tellSubsOptions = new ArrayList<>();
        tellSubsOptions.add("Do not Notify Subscribers");
        tellSubsOptions.add("Notify Subscribers");
        ChoiceBox<String> tellSubsChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList(tellSubsOptions));
        tellSubsChoiceBox.setId(paneId + NODE_ID_TELLSUBS);
        if (tellSubs) {
            tellSubsChoiceBox.getSelectionModel().select(1);
        } else {
            tellSubsChoiceBox.getSelectionModel().select(0);
        }
        tellSubsChoiceBox.setTooltip(new Tooltip(
                "Should the channel's subscribers be notified that a new video has been uploaded? Not recommended then uploading alot of videos \nBut why are you using this program then?"));
        tellSubsChoiceBox.setDisable(true);

        videoBasePane.add(title, 0, 0);
        videoBasePane.add(description, 0, 1, 1, 3);
        videoBasePane.add(categoryChoiceBox, 1, 0);
        videoBasePane.add(tags, 1, 1, 1, 2);
        videoBasePane.add(playlist, 1, 3);
        videoBasePane.add(visibility, 1, 4);
        videoBasePane.add(tellSubsChoiceBox, 0, 4);
    }

    public void setEditable(boolean newEditStatus) {
        ((TextField) videoBasePane.lookup("#" + paneId + NODE_ID_TITLE)).setEditable(newEditStatus);
        ((TextArea) videoBasePane.lookup("#" + paneId + NODE_ID_DESCRIPTION)).setEditable(newEditStatus);
        videoBasePane.lookup("#" + paneId + NODE_ID_CATEGORY).setDisable(!newEditStatus);
        ((TextArea) videoBasePane.lookup("#" + paneId + NODE_ID_TAGS)).setEditable(newEditStatus);
        ((TextField) videoBasePane.lookup("#" + paneId + NODE_ID_PLAYLIST)).setEditable(newEditStatus);
        videoBasePane.lookup("#" + paneId + NODE_ID_VISIBILITY).setDisable(!newEditStatus);
        videoBasePane.lookup("#" + paneId + NODE_ID_TELLSUBS).setDisable(!newEditStatus);
    }
}
