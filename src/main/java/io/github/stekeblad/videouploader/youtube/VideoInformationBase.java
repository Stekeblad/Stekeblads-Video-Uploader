package io.github.stekeblad.videouploader.youtube;

import io.github.stekeblad.videouploader.youtube.utils.CategoryUtils;
import io.github.stekeblad.videouploader.youtube.utils.VisibilityStatus;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static java.lang.Integer.MAX_VALUE;
import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;

/**
 * Base class to VideoUpload and VideoPreset that contains all their common features
 */
public class VideoInformationBase {

    // Constants
    public static final List<String> THUMBNAIL_FILE_FORMAT = Arrays.asList("*.jpg", "*.png");

    private static final String NODE_ID_TITLE = "_title";
    private static final String NODE_ID_DESCRIPTION = "_description";
    private static final String NODE_ID_CATEGORY = "_category";
    private static final String NODE_ID_TAGS = "_tags";
    private static final String NODE_ID_PLAYLIST = "_playlist";
    private static final String NODE_ID_VISIBILITY = "_visibility";
    private static final String NODE_ID_TELLSUBS = "_tellSubs";
    private static final String NODE_ID_THUMBNAIL = "_thumbNail";

    // Variables
    private GridPane videoBasePane;
    private String paneId;
    private File thumbNailFile;
    private boolean allowEdit;
    private CategoryUtils categoryUtils = CategoryUtils.INSTANCE;

    // Getters

    /**
     * @return the content of the video name TextField
     */
    public String getVideoName() {
        return ((TextField) videoBasePane.lookup("#" + paneId + NODE_ID_TITLE)).getText();
    }

    /**
     * @return returns the content of the video description TextArea
     */
    public String getVideoDescription() {
        return ((TextArea) videoBasePane.lookup("#" + paneId + NODE_ID_DESCRIPTION)).getText();
    }

    /**
     * @return returns the selected value of the visibility ChoiceBox as a uppercase String
     */
    @SuppressWarnings("unchecked")
    public VisibilityStatus getVisibility() {
        return VisibilityStatus.valueOf(((ChoiceBox<String>) videoBasePane.lookup("#" + paneId + NODE_ID_VISIBILITY)).getSelectionModel().getSelectedItem().toUpperCase());
    }

    /**
     * @return returns the tags in the tags TextArea as a ArrayList of Strings, the tags are split on ", "
     */
    public List<String> getVideoTags() {
        return new ArrayList<>(Arrays.asList(((TextArea) videoBasePane.lookup("#" + paneId + NODE_ID_TAGS)).getText().split(", ")));
    }

    /**
     * @return returns the name of the selected playlist in the playlist ChoiceBox
     */
    @SuppressWarnings("unchecked")
    public String getPlaylist() {
        return ((ChoiceBox<String>) videoBasePane.lookup("#" + paneId + NODE_ID_PLAYLIST)).getSelectionModel().getSelectedItem();
    }

    /**
     * @return returns the name of the category selected in the categories ChoiceBox
     */
    @SuppressWarnings("unchecked")
    public String getCategory() {
        return ((ChoiceBox<String>) videoBasePane.lookup("#" + paneId + NODE_ID_CATEGORY)).getSelectionModel().getSelectedItem();
    }

    /**
     * @return returns true if do tell subscribers in the tellSubs ChoiceBox is selected and false if do not tell subscribers is selected
     */
    @SuppressWarnings("unchecked")
    public boolean isTellSubs() { // only two choices, do notify subscribers is the second choice (index 1)
        return (((ChoiceBox<String>) videoBasePane.lookup("#" + paneId + NODE_ID_TELLSUBS)).getSelectionModel().isSelected(1));
    }

    /**
     * @return returns the selected thumbnail file or null if no custom thumbnail is selected
     */
    public File getThumbNail() {
        if(thumbNailFile == null || thumbNailFile.toURI().toString().endsWith("_")) {
            return null;
        } else {
            return thumbNailFile;
        }
    }

    /**
     * @return returns the id of the Pane
     */
    public String getPaneId() {
        return paneId;
    }

    /**
     * @return returns the entire UI Pane.
     */
    public GridPane getPane() {
        return videoBasePane;
    }

    //Setters

    /**
     * Enables / Disables editing of all the fields on the pane
     * @param newEditStatus true to allow edit, false to not allow
     */
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

    /**
     * Sets the thumbnail
     * @param thumbnail the thumbnail image file
     * @throws Exception if editing is not allowed
     */
    public void setThumbNailFile(File thumbnail) throws Exception {
        if (!allowEdit) {
            throw new Exception("Edit not allowed");
        } else {
            ((ImageView) videoBasePane.lookup("#" + paneId + NODE_ID_THUMBNAIL)).setImage(
                    new Image(new FileInputStream(thumbnail)));
            thumbNailFile = thumbnail;
        }
    }

    /**
     * Define something to happen then the thumbnail is clicked, can be used with lambda like this:
     * <pre>
     *     setOnThumbnailClicked(event -> {System.out.println("Example");}
     * </pre>
     *
     * @param clickEvent a handler for the click event
     */
    public void setOnThumbnailClicked(EventHandler<MouseEvent> clickEvent) {
        videoBasePane.lookup("#" + paneId + NODE_ID_THUMBNAIL).setOnMouseClicked(clickEvent);
    }

    /**
     * Sets the playlists that will appear in the playlists ChoiceBox
     * @param playlistNames A ArrayList with the names of all playlists
     */
    @SuppressWarnings("unchecked")
    public void setPlaylists(ArrayList<String> playlistNames) {
        if (playlistNames != null) {
            ((ChoiceBox<String>) videoBasePane.lookup("#" + paneId + NODE_ID_PLAYLIST)).setItems(FXCollections.observableArrayList(playlistNames));
        }
    }

    /**
     * Define something to happen then the playlist choice box is clicked, can be used with lambda like this:
     * <pre>
     *     setOnPlaylistsClicked(event -> {System.out.println("Example");}
     * </pre>
     *
     * @param clickEvent a handler for the click event
     */
    public void setOnPlaylistsClicked(EventHandler<MouseEvent> clickEvent) {
        videoBasePane.lookup("#" + paneId + NODE_ID_PLAYLIST).setOnMouseClicked(clickEvent);
    }

    /**
     * Sets the categories that will appear in the categories ChoiceBox
     * @param categories A ArrayList with the names of all categories
     */
    @SuppressWarnings("unchecked")
    public void setCategories(ArrayList<String> categories) {
        if(categories != null) {
            ((ChoiceBox<String>) videoBasePane.lookup("#" + paneId + NODE_ID_CATEGORY)).setItems(FXCollections.observableArrayList(categories));
        }
    }

    /**
     * Define something to happen then the categories choice box is clicked, can be used with lambda like this:
     * <pre>
     *     setOnCategoriesClicked(event -> {System.out.println("Example");}
     * </pre>
     *
     * @param clickEvent a handler for the click event
     */
    public void setOnCategoriesClicked(EventHandler<MouseEvent> clickEvent) {
        videoBasePane.lookup("#" + paneId + NODE_ID_CATEGORY).setOnMouseClicked(clickEvent);
    }

    // other methods

    /**
     * Constructor for VideoInformationBase. There is also the VideoInformationBase.Builder class if that is preferred.
     * @param videoName Title of the video
     * @param videoDescription Content of the description field
     * @param visibility If the video should be public, private or unlisted
     * @param videoTags A List of string with all tags for the video
     * @param playlist The name of a playlist for this video to be in
     * @param category Name of a Youtube category
     * @param tellSubs Set to true if subscribers should be notified
     * @param thumbNailPath File path to a image to use as thumbnail
     * @param paneId A string used for naming all UI elements
     */
    public VideoInformationBase(String videoName, String videoDescription, VisibilityStatus visibility, List<String> videoTags,
                                String playlist, String category, boolean tellSubs, String thumbNailPath, String paneId) {

        if (visibility == null) { // optional, default to public
            visibility = VisibilityStatus.PUBLIC;
        }
        this.paneId = paneId;
        if(thumbNailPath != null) {
            this.thumbNailFile = new File(thumbNailPath);
        } else {
            this.thumbNailFile = null;
        }
        makeVideoBasePane(videoName, videoDescription, visibility, videoTags, playlist, category, tellSubs, thumbNailPath);
        allowEdit = false;
    }

    /**
     * Reconstructs a VideoInformationBase form its string version created by calling toString()
     * @param fromString The string representation of a VideoInformationBase
     * @param paneId A string used for naming all UI elements
     * @throws Exception If the string could not be converted to a VideoInformationBase
     */
    public VideoInformationBase(String fromString, String paneId) throws Exception {

        this.paneId = paneId;

        String videoName = null;
        String videoDescription = null;
        VisibilityStatus visibility = null;
        ArrayList<String> videoTags = null;
        String playlist = null;
        String category = null;
        boolean tellSubs = false;
        String thumbnailPath = null;

        // Splitting up the data for easy reading, one thing per row -> on thing per array element
        String[] lines = fromString.split("\n");
        String line;
        // For all lines
        for(int i = 0; i < lines.length; i++) {
            line = lines[i];
            // Locate the separator between field name and value
            int colonIndex = line.indexOf(':');
            if (colonIndex < 0) {
                System.err.println(fromString);
                throw new Exception("Malformed string representation of class");
            } else {
                // Switch on field name
                switch (line.substring(0, colonIndex)) {
                    case NODE_ID_TITLE:
                        videoName = line.substring(colonIndex + 1);
                        break;
                    case NODE_ID_DESCRIPTION:
                        // A bit special to allow descriptions to be multi-lined with actual enters in.
                        StringBuilder descBuilder = new StringBuilder();
                        descBuilder.append(line.substring(colonIndex + 1));
                        // Skips lines in the outer loop because they are not valid
                        i++;
                        // As long as the next line not starts with "_" treat it as a part of the description
                        while(!lines[i].startsWith("_")) {
                            descBuilder.append("\n").append(lines[i]);
                            i++;
                        }
                        // Line started with _ ,go back one so the increment in the loop does not cause this line to be skipped
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
                        category = line.substring(colonIndex + 1);
                        break;
                    case NODE_ID_TELLSUBS:
                        tellSubs = Boolean.valueOf(line.substring(colonIndex + 1));
                        break;
                    case NODE_ID_THUMBNAIL:
                        thumbnailPath = line.substring(colonIndex + 1);
                        if(thumbnailPath.equals("_")) {
                            this.thumbNailFile = null; // use default
                        } else {
                            this.thumbNailFile = new File(thumbnailPath);
                        }
                        break;
                    default:
                        //ignore, might be a child value
                }
            }
        }
        makeVideoBasePane(videoName, videoDescription, visibility, videoTags, playlist, category, tellSubs, thumbnailPath);
    }

    /**
     * Returns a copy of this VideoInformationBase with a potentially different node naming
     * @param paneIdForCopy The paneId used for naming the nodes in the copy, use null to get the same as original.
     *                      if null is given then the original and the copy may not be able to be on screen at the same time,
     *                      the nodes will be considered to be the same and when placing the second one of them it will cause the
     *                      nodes from the first to be moved to the location of the second.
     * @return A copy of this VideoInformationBase
     */
    public VideoInformationBase copy(String paneIdForCopy) {
        if (paneIdForCopy == null) {
            paneIdForCopy = getPaneId();
        }
        return new VideoInformationBase(getVideoName(), getVideoDescription(), getVisibility(), getVideoTags(),
                getPlaylist(), getCategory(), isTellSubs(), getThumbNail().getAbsolutePath(), paneIdForCopy);
    }

    /**
     * Used for building a VideoInformationBase one attribute at the time.
     * Call build() to get a real VideoInformationBase when you are done setting attributes.
     */
    public static class Builder {
        String videoName;
        String videoDescription;
        VisibilityStatus visibility;
        List<String> videoTags;
        String playlist;
        String category;
        boolean tellSubs;
        String thumbNailPath;
        String paneName;

        // Getters
        public String getVideoName() {
            return videoName;
        }

        public String getVideoDescription() {
            return videoDescription;
        }

        public VisibilityStatus getVisibility() {
            return visibility;
        }

        public List<String> getVideoTags() {
            return videoTags;
        }

        public String getPlaylist() {
            return playlist;
        }

        public String getCategory() {
            return category;
        }

        public boolean isTellSubs() {
            return tellSubs;
        }

        public String getThumbNailPath() {
            return thumbNailPath;
        }

        public String getPaneName() {
            return paneName;
        }

        // Setters
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

        public VideoInformationBase.Builder setCategory(String category) {
            this.category = category;
            return this;
        }

        public VideoInformationBase.Builder setTellSubs(boolean tellSubs) {
            this.tellSubs = tellSubs;
            return this;
        }

        public VideoInformationBase.Builder setThumbNailPath(String thumbNailPath) {
            this.thumbNailPath = thumbNailPath;
            return this;
        }

        public VideoInformationBase.Builder setPaneName(String paneName) {
            this.paneName = paneName;
            return this;
        }

        public VideoInformationBase build() {
            return new VideoInformationBase(videoName, videoDescription, visibility, videoTags, playlist, category,
                    tellSubs, thumbNailPath, paneName);
        }
    }

    /**
     * Creates the UI Pane so it can be be retrieved by front end code with getPane()
     * @param videoName Content of the video title field
     * @param videoDescription Content of the video description field
     * @param visibility The VisibilityStatus to be selected by default
     * @param videoTags The tags to be inside th tags field
     * @param playlist The name of the element to be selected by default
     * @param category The category to be selected by default
     * @param tellSubs The default value of tellSubs
     * @param thumbNailPath The path to the selected thumbnail or null for the no thumbnail selected image
     */
     protected void makeVideoBasePane(String videoName, String videoDescription, VisibilityStatus visibility, List<String> videoTags,
                                      String playlist, String category, boolean tellSubs, String thumbNailPath) {
         // Creating the pane, id, size, border
         videoBasePane = new GridPane();
         videoBasePane.setId(paneId);
         videoBasePane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

         if(videoName == null) videoName = "";
         if(videoDescription == null) videoDescription = "";


         TextField title = new TextField();     // Creating the video title TextField
         title.setId(paneId + NODE_ID_TITLE);   // Naming it
         title.setPromptText("Video title");    // Faded text to tell the user what should be entered here
         title.setText(videoName);              // Set text to be shown
         title.setEditable(false);              // Do not allow the content in the TextField to be changed by the user

         TextArea description = new TextArea();
         description.setId(paneId + NODE_ID_DESCRIPTION);
         description.setPromptText("Video description");
         description.setText(videoDescription);
         description.setEditable(false);
         // Visually start a new line when the edge of TextArea is reached so the user do not need to scroll sideways to
         // read what they just entered. This type of automatic enter is only visible in the UI and is not a part of the
         // string returned by description.getText()
         description.setWrapText(true);

         ChoiceBox<String> categoryChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList(categoryUtils.getCategoryNames()));
         categoryChoiceBox.setId(paneId + NODE_ID_CATEGORY);
         categoryChoiceBox.getSelectionModel().select(category);
         categoryChoiceBox.setTooltip(new Tooltip("Youtube Video Category"));
         categoryChoiceBox.setDisable(true);    // prevent editing

         TextArea tags = new TextArea();
         tags.setId(paneId + NODE_ID_TAGS);
         tags.setPromptText("list, of, tags, separated, with, comma, and, space");
         StringBuilder tagsString = new StringBuilder();
         if(videoTags != null && videoTags.size() > 0) {
             // Add the tags to the screen with a ", " separation between them ...
            for (int i = 0; i < videoTags.size() - 1; i++) {
                tagsString.append(videoTags.get(i)).append(", ");
            }
            // ... without ", " after the last one
             tagsString.append(videoTags.get(videoTags.size() - 1));
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
                 "Should the channel's subscribers be notified that a new video has been uploaded? " +
                         "Not recommended then uploading a lot of videos"));
         tellSubsChoiceBox.setDisable(true);

         Image thumbNailImage;
         try {
             thumbNailImage = new Image(new BufferedInputStream(new FileInputStream(new File(thumbNailPath))));
         } catch (FileNotFoundException | NullPointerException e) {
             InputStream thumbStream = this.getClass().getResourceAsStream("/images/no_image.png");
             thumbNailImage = new Image(thumbStream);
         }
         ImageView thumbNailFrame = new ImageView(thumbNailImage);
         thumbNailFrame.setFitWidth(160);
         thumbNailFrame.setFitHeight(90);
         thumbNailFrame.setId(paneId + NODE_ID_THUMBNAIL);
         thumbNailFrame.setPreserveRatio(true);

         // Place the different nodes on the pane
         videoBasePane.add(title, 0, 0);
         videoBasePane.add(categoryChoiceBox, 1, 0);
         videoBasePane.add(playlistChoiceBox, 2, 0);

         videoBasePane.add(description, 0, 1, 1, 2);
         videoBasePane.add(tags, 1, 1, 1, 1);
         videoBasePane.add(thumbNailFrame, 2, 1);

         videoBasePane.add(tellSubsChoiceBox, 1, 2);
         videoBasePane.add(visibilityChoiceBox, 2, 2);

         // Sizing
         ColumnConstraints rightConstraint = new ColumnConstraints(170, 170, 170);
         ColumnConstraints defaultConstraint = new ColumnConstraints(100, USE_COMPUTED_SIZE, MAX_VALUE);
         videoBasePane.getColumnConstraints().setAll(defaultConstraint, defaultConstraint, rightConstraint);

         RowConstraints r1 = new RowConstraints(30);
         RowConstraints r2 = new RowConstraints(90);
         RowConstraints r3 = new RowConstraints(30);
         videoBasePane.getRowConstraints().setAll(r1, r2, r3);
    }


    /**
     * Creates a string representation of the class that can be saved and later used to recreate the class as it
     * looked like before with the VideoInformationBase(String, String) constructor
     * @return A String representation of this class
     */
    public String toString() {
        StringBuilder classString = new StringBuilder();
        String thumbnailSave;

        if (thumbNailFile == null) {
            thumbnailSave = "_"; //no thumbnail set, default is selected
        } else {
            // Attempt to get the path of the thumbnail and if it fails fall back to the default thumbnail.
            // Earlier the entire Instance of the class was lost.
            try {
                thumbnailSave = thumbNailFile.getCanonicalPath();
            } catch (IOException e) {
                System.err.println("Failed getting the path of the thumbnail while creating a string of " + getPaneId() + " named " + getVideoName());
                thumbnailSave = "_";
            }
        }
        classString.append(NODE_ID_TITLE + ":").append(getVideoName()).append("\n")
                .append(NODE_ID_DESCRIPTION).append(":").append(getVideoDescription()).append("\n")
                .append(NODE_ID_VISIBILITY).append(":").append(getVisibility().getStatusName().toUpperCase()).append("\n")
                .append(NODE_ID_TAGS).append(":").append(getVideoTags().toString()).append("\n")
                .append(NODE_ID_PLAYLIST).append(":").append(getPlaylist()).append("\n")
                .append(NODE_ID_CATEGORY).append(":").append(getCategory()).append("\n")
                .append(NODE_ID_TELLSUBS).append(":").append(Boolean.toString(isTellSubs())).append("\n")
                .append(NODE_ID_THUMBNAIL).append(":").append(thumbnailSave);
        return classString.toString();

    }
}
