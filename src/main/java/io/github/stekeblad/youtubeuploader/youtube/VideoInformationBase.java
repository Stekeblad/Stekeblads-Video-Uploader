package io.github.stekeblad.youtubeuploader.youtube;

import io.github.stekeblad.youtubeuploader.youtube.constants.Categories;
import io.github.stekeblad.youtubeuploader.youtube.constants.VisibilityStatus;
import javafx.collections.FXCollections;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public class VideoInformationBase {

    // Constants
    public static final List<String> THUMBNAIL_FILE_FORMAT = Arrays.asList("*.jpg", "*.png");
    public static final String NODE_ID_TITLE = "_title";
    public static final String NODE_ID_DESCRIPTION = "_description";
    public static final String NODE_ID_CATEGORY = "_category";
    public static final String NODE_ID_TAGS = "_tags";
    public static final String NODE_ID_PLAYLIST = "_playlist";
    public static final String NODE_ID_VISIBILITY = "_visibility";
    public static final String NODE_ID_TELLSUBS = "_tellSubs";
    public static final String NODE_ID_THUMBNAIL = "_thumbNail";

    // Variables
    private GridPane videoBasePane;
    private String paneId;
    private File thumbNailFile;
    private boolean allowEdit;

    // Getters
    public String getVideoName() {
        return ((TextField) videoBasePane.lookup("#" + paneId + NODE_ID_TITLE)).getText();
    }
    public String getVideoDescription() {
        return ((TextArea) videoBasePane.lookup("#" + paneId + NODE_ID_DESCRIPTION)).getText();
    }
    @SuppressWarnings("unchecked")
    public VisibilityStatus getVisibility() {
        return VisibilityStatus.valueOf(((ChoiceBox<String>) videoBasePane.lookup("#" + paneId + NODE_ID_VISIBILITY)).getSelectionModel().getSelectedItem().toUpperCase());
    }
    public List<String> getVideoTags() {
        return new ArrayList<>(Arrays.asList(((TextArea) videoBasePane.lookup("#" + paneId + NODE_ID_TAGS)).getText().split(", ")));
    }
    @SuppressWarnings("unchecked")
    public String getPlaylist() {
        return ((ChoiceBox<String>) videoBasePane.lookup("#" + paneId + NODE_ID_PLAYLIST)).getSelectionModel().getSelectedItem();
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
        return thumbNailFile;
    }
    public String getPaneId() {
        return paneId;
    }
    public GridPane getPane() {
        return videoBasePane;
    }

    //Setters (mostly done directly on the GridPane)
    public void setThumbNailFile(File thumbnail) throws Exception {
        if (!allowEdit) {
            throw new Exception("Edit not allowed");
        } else {
            thumbNailFile = thumbnail;
            ((ImageView) videoBasePane.lookup("#" + paneId + NODE_ID_THUMBNAIL)).setImage(
                    new Image(new FileInputStream(thumbnail)));
        }
    }

    @SuppressWarnings("unchecked")
    public void setPlaylists(ArrayList<String> playlistNames) {
        if (playlistNames != null) {
            ((ChoiceBox<String>) videoBasePane.lookup("#" + paneId + NODE_ID_PLAYLIST)).setItems(FXCollections.observableArrayList(playlistNames));
        }
    }

    // other methods

    public VideoInformationBase(String videoName, String videoDescription, VisibilityStatus visibility, List<String> videoTags,
                                String playlist, Categories category, boolean tellSubs, File thumbNail, String paneId) {

        if (visibility == null) { // optional, default to public
            visibility = VisibilityStatus.PUBLIC;
        }
        this.paneId = paneId;
        this.thumbNailFile = thumbNail;
        makeVideoBasePane(videoName, videoDescription, visibility, videoTags, playlist, category, tellSubs, thumbNail);
        allowEdit = false;
    }

    public VideoInformationBase(String fromString, String paneId) throws Exception {

        this.paneId = paneId;

        String videoName = null;
        String videoDescription = null;
        VisibilityStatus visibility = null;
        ArrayList<String> videoTags = null;
        String playlist = null;
        Categories category = null;
        boolean tellSubs = false;
        File thumbnail = null;

        String[] lines = fromString.split("\n");
        String line;
        //for (String line : lines) {
        for(int i = 0; i < lines.length; i++) {
            line = lines[i];
            int colonIndex = line.indexOf(':');
            if (colonIndex < 0) {
                System.err.println(fromString);
                throw new Exception("Malformed string representation of class");
            } else {
                switch (line.substring(0, colonIndex)) {
                    case NODE_ID_TITLE:
                        videoName = line.substring(colonIndex + 1);
                        break;
                    case NODE_ID_DESCRIPTION:
                        StringBuilder descBuilder = new StringBuilder();
                        descBuilder.append(line.substring(colonIndex + 1));
                        i++;
                        while(!lines[i].startsWith("_")) {
                            descBuilder.append("\n").append(lines[i]);
                            i++;
                        }
                        i--;
                        videoDescription = descBuilder.toString();
                        break;
                    case NODE_ID_VISIBILITY:
                        visibility = VisibilityStatus.valueOf(line.substring(colonIndex + 1));
                        break;
                    case NODE_ID_TAGS:
                        line = line.substring(colonIndex + 2, line.length() - 1); // remove brackets
                        videoTags = new ArrayList<>(Arrays.asList(line.split(",")));
                        break;
                    case NODE_ID_PLAYLIST:
                        playlist = line.substring(colonIndex + 1);
                        break;
                    case NODE_ID_CATEGORY:
                        category = Categories.valueOf(line.substring(colonIndex + 1));
                        break;
                    case NODE_ID_TELLSUBS:
                        tellSubs = Boolean.valueOf(line.substring(colonIndex + 1));
                        break;
                    case NODE_ID_THUMBNAIL:
                        if (line.equals("_")) {
                            thumbnail = null; //use default
                        } else {
                            thumbnail = new File(line.substring(colonIndex + 1));
                        }
                        break;
                    default:
                        //ignore, might be a child value
                }
            }
        }
        makeVideoBasePane(videoName, videoDescription, visibility, videoTags, playlist, category, tellSubs, thumbnail);
    }

    public VideoInformationBase copy(String paneIdForCopy) {
        return new VideoInformationBase(getVideoName(), getVideoDescription(), getVisibility(), getVideoTags(), getPlaylist(), getCategory(), isTellSubs(), getThumbNail(), paneIdForCopy);
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

        public VideoInformationBase.Builder setVideoName(String videoName) {
            this.videoName = videoName;
            return this;
        }

        public VideoInformationBase.Builder setVideoDescription(String videoDescription) {
            this.videoDescription = videoDescription;
            return this;
        }

        public VideoInformationBase.Builder setVisibility(VisibilityStatus visibility) {
            this.visibility = visibility;
            return this;
        }

        public VideoInformationBase.Builder setVideoTags(List<String> videoTags) {
            this.videoTags = videoTags;
            return this;
        }

        public VideoInformationBase.Builder setPlaylist(String playlist) {
            this.playlist = playlist;
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
            return new VideoInformationBase(videoName, videoDescription, visibility, videoTags, playlist, category,
                    tellSubs, thumbNail, paneName);
        }
    }

     protected void makeVideoBasePane(String videoName, String videoDescription, VisibilityStatus visibility, List<String> videoTags,
                                      String playlist, Categories category, boolean tellSubs, File thumbNail) {
        videoBasePane = new GridPane();
        videoBasePane.setId(paneId);
        videoBasePane.setPrefSize(680, 100);
        videoBasePane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

        if(videoName == null) videoName = "";
        if(videoDescription == null) videoDescription = "";


        TextField title = new TextField();
        title.setId(paneId + NODE_ID_TITLE);
        title.setPromptText("Video title");
        title.setText(videoName);
        title.setEditable(false);

        TextArea description = new TextArea();
        description.setId(paneId + NODE_ID_DESCRIPTION);
        description.setPromptText("Video description");
        description.setText(videoDescription);
        description.setEditable(false);
        description.setWrapText(true);

        ArrayList<Categories> categories = new ArrayList<>(EnumSet.allOf(Categories.class));
        ArrayList<String> categoryStrings = new ArrayList<>();
        for (Categories category1 : categories) {
            categoryStrings.add(category1.getName());
        }
        ChoiceBox<String> categoryChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList(categoryStrings));
        categoryChoiceBox.setId(paneId + NODE_ID_CATEGORY);
        categoryChoiceBox.getSelectionModel().select(category.getName());
        categoryChoiceBox.setTooltip(new Tooltip("Youtube Video Category"));
        categoryChoiceBox.setDisable(true);

        TextArea tags = new TextArea();
        tags.setId(paneId + NODE_ID_TAGS);
        tags.setPromptText("list, of, tags, separated, with, comma, and, space");
        StringBuilder tagsString = new StringBuilder();
        if(videoTags != null && videoTags.size() > 0) {
            for (int i = 0; i < videoTags.size() - 1; i++) {
                tagsString.append(videoTags.get(i)).append(", ");
            }
            if (videoTags.size() > 0) {
                tagsString.append(videoTags.get(videoTags.size() - 1));
            }
        }
        tags.setText(tagsString.toString());
        tags.setEditable(false);
        tags.setWrapText(true);
        tags.textProperty().addListener((observable, oldValue, newValue) -> { //Prevent newlines, allow text wrap
                 tags.setText(newValue.replaceAll("\\R", ""));
         });




        ArrayList<String> playlistStrings = new ArrayList<>();
        if (playlist != null && !playlist.equals("")) {
            playlistStrings.add(playlist);
        } else {
            playlistStrings.add("select a playlist");
        }
        ChoiceBox<String> playlistChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList(playlistStrings));
        playlistChoiceBox.setId(paneId + NODE_ID_PLAYLIST);
        playlistChoiceBox.getSelectionModel().select(0);
        playlistChoiceBox.setTooltip(new Tooltip("Select a playlist to add this video to"));
        playlistChoiceBox.setDisable(true);

        ArrayList<VisibilityStatus> statuses = new ArrayList<>(EnumSet.allOf(VisibilityStatus.class));
        ArrayList<String> visibilityStrings = new ArrayList<>();
        for (VisibilityStatus status : statuses) {
            visibilityStrings.add(status.getStatusName());
        }
        ChoiceBox<String> visibilityChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList(visibilityStrings));
         visibilityChoiceBox.setId(paneId + NODE_ID_VISIBILITY);
         visibilityChoiceBox.getSelectionModel().select(visibility.getStatusName());
         visibilityChoiceBox.setTooltip(new Tooltip("how will the video be accessible?"));
         visibilityChoiceBox.setDisable(true);

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

         Image thumbNailImage;
         try {
             thumbNailImage = new Image(new BufferedInputStream(new FileInputStream(thumbNail)));
         } catch (FileNotFoundException | NullPointerException e) {
             InputStream thumbStream = this.getClass().getResourceAsStream("/images/no_image.png");
             thumbNailImage = new Image(thumbStream);
         }
         ImageView thumbNailFrame = new ImageView(thumbNailImage);
         thumbNailFrame.setFitWidth(160);
         thumbNailFrame.setFitHeight(90);
         thumbNailFrame.setId(paneId + NODE_ID_THUMBNAIL);
         thumbNailFrame.setPreserveRatio(true);

         videoBasePane.add(title, 0, 0);
         videoBasePane.add(categoryChoiceBox, 1, 0);
         videoBasePane.add(playlistChoiceBox, 2, 0);

         videoBasePane.add(description, 0, 1, 1, 3);
         videoBasePane.add(tags, 1, 1, 1, 2);
         videoBasePane.add(thumbNailFrame, 2, 1);

         videoBasePane.add(tellSubsChoiceBox, 1, 3);
         videoBasePane.add(visibilityChoiceBox, 2, 3);
    }

    public void setEditable(boolean newEditStatus) {
        allowEdit = newEditStatus;
        ((TextField) videoBasePane.lookup("#" + paneId + NODE_ID_TITLE)).setEditable(newEditStatus);
        ((TextArea) videoBasePane.lookup("#" + paneId + NODE_ID_DESCRIPTION)).setEditable(newEditStatus);
        videoBasePane.lookup("#" + paneId + NODE_ID_CATEGORY).setDisable(!newEditStatus);
        ((TextArea) videoBasePane.lookup("#" + paneId + NODE_ID_TAGS)).setEditable(newEditStatus);
        videoBasePane.lookup("#" + paneId + NODE_ID_PLAYLIST).setDisable(!newEditStatus);
        videoBasePane.lookup("#" + paneId + NODE_ID_VISIBILITY).setDisable(!newEditStatus);
        videoBasePane.lookup("#" + paneId + NODE_ID_TELLSUBS).setDisable(!newEditStatus);
    }

    public String toString() {
        StringBuilder classString = new StringBuilder();
        String thumbnailSave;
    try {
            if (thumbNailFile == null) {
                thumbnailSave = "_"; //no thumbnail set, default is selected
            } else {
                thumbnailSave = thumbNailFile.getCanonicalPath();
            }
            classString.append(NODE_ID_TITLE + ":").append(getVideoName()).append("\n")
                    .append(NODE_ID_DESCRIPTION).append(":").append(getVideoDescription()).append("\n")
                    .append(NODE_ID_VISIBILITY).append(":").append(getVisibility().getStatusName().toUpperCase()).append("\n")
                    .append(NODE_ID_TAGS).append(":").append(getVideoTags().toString()).append("\n")
                    .append(NODE_ID_PLAYLIST).append(":").append(getPlaylist()).append("\n")
                    .append(NODE_ID_CATEGORY).append(":").append(getCategory().getName()).append("\n")
                    .append(NODE_ID_TELLSUBS).append(":").append(Boolean.toString(isTellSubs())).append("\n")
                    .append(NODE_ID_THUMBNAIL).append(":").append(thumbnailSave);
            return classString.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";

    }
}
