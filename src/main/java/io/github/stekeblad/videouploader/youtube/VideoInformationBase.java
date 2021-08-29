package io.github.stekeblad.videouploader.youtube;

import io.github.stekeblad.videouploader.utils.background.OpenInBrowser;
import io.github.stekeblad.videouploader.youtube.utils.CategoryUtils;
import io.github.stekeblad.videouploader.youtube.utils.PlaylistUtils;
import io.github.stekeblad.videouploader.youtube.utils.VisibilityStatus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
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
import java.util.function.Consumer;

import static java.lang.Integer.MAX_VALUE;
import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;

/**
 * Base class to VideoUpload and VideoPreset that contains all their common features
 */
public class VideoInformationBase {

    // Constants
    public static final List<String> THUMBNAIL_FILE_FORMAT = Arrays.asList("*.jpg", "*.png");
    public static final long MAX_THUMB_SIZE = 2 * 1024 * 1024;

    private static final String NODE_ID_TITLE = "_title";
    private static final String NODE_ID_DESCRIPTION = "_description";
    private static final String NODE_ID_CATEGORY = "_category";
    private static final String NODE_ID_TAGS = "_tags";
    private static final String NODE_ID_PLAYLIST = "_playlist";
    private static final String NODE_ID_VISIBILITY = "_visibility";
    private static final String NODE_ID_TELL_SUBS = "_tellSubs";
    private static final String NODE_ID_THUMBNAIL = "_thumbNail";
    private static final String NODE_ID_MADE_FOR_KIDS = "_madeForKids";

    private static final String NODE_ID_BUTTONSBOX = "_buttons";

    // Variables
    private GridPane videoBasePane;
    private final String paneId;
    private File thumbNailFile;
    private boolean allowEdit;
    private final CategoryUtils categoryUtils = CategoryUtils.INSTANCE;
    private final PlaylistUtils playlistUtils = PlaylistUtils.INSTANCE;
    Consumer<Boolean> thumbnailCursorEventHandler = null;

    // Getters

    /**
     * @return returns the Id of the button in the first button slot. Can be used to know what button is there at the moment
     */
    public String getButton1Id() {
        HBox buttonBox = ensureButtonBox();
        ensureButtonExistence(buttonBox, 1);
        return buttonBox.getChildren().get(0).getId();
    }

    /**
     * @return returns the Id of the button in the second button slot. Can be used to know what button is there at the moment
     */
    public String getButton2Id() {
        HBox buttonBox = ensureButtonBox();
        ensureButtonExistence(buttonBox, 2);
        return buttonBox.getChildren().get(1).getId();
    }

    /**
     * @return returns the Id of the button in the third button slot. Can be used to know what button is there at the moment
     */
    public String getButton3Id() {
        HBox buttonBox = ensureButtonBox();
        ensureButtonExistence(buttonBox, 3);
        return buttonBox.getChildren().get(2).getId();
    }

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
    public String getSelectedPlaylist() {
        return ((ChoiceBox<String>) videoBasePane.lookup("#" + paneId + NODE_ID_PLAYLIST)).getSelectionModel().getSelectedItem();
    }

    /**
     * @return a list of all strings that currently is in the playlist ChoiceBox
     */
    @SuppressWarnings("unchecked")
    private List<String> getPlaylistChoices() {
        return ((ChoiceBox<String>) videoBasePane.lookup("#" + paneId + NODE_ID_PLAYLIST)).getItems();
    }

    /**
     * @return returns the name of the category selected in the categories ChoiceBox
     */
    @SuppressWarnings("unchecked")
    public String getCategory() {
        return ((ChoiceBox<String>) videoBasePane.lookup("#" + paneId + NODE_ID_CATEGORY)).getSelectionModel().getSelectedItem();
    }

    /**
     * @return a list of all strings that currently is in the category ChoiceBox
     */
    @SuppressWarnings("unchecked")
    private List<String> getCategoryChoices() {
        return ((ChoiceBox<String>) videoBasePane.lookup("#" + paneId + NODE_ID_CATEGORY)).getItems();
    }

    /**
     * @return returns true if do tell subscribers in the tellSubs ChoiceBox is selected and false if do not tell subscribers is selected
     */
    @SuppressWarnings("unchecked")
    public boolean isTellSubs() { // only two choices, do notify subscribers is the second choice (index 1)
        return (((ChoiceBox<String>) videoBasePane.lookup("#" + paneId + NODE_ID_TELL_SUBS)).getSelectionModel().isSelected(1));
    }

    /**
     * @return returns the selected thumbnail file or null if no custom thumbnail is selected
     */
    public File getThumbNail() {
        if (thumbNailFile == null || thumbNailFile.getName().equals("_")) {
            return null;
        } else {
            return thumbNailFile;
        }
    }

    public boolean isMadeForKids() {
        return ((CheckBox) videoBasePane.lookup("#" + paneId + NODE_ID_MADE_FOR_KIDS)).isSelected();
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
     * Place a button in the first button slot with your own text, click behavior etc.
     *
     * @param btn1 A fully configured button
     */
    public void setButton1(Button btn1) {
        if (btn1 == null)
            throw new IllegalArgumentException("Button 1 parameter can not be null");

        HBox buttonBox = ensureButtonBox();
        ensureButtonExistence(buttonBox, 1);
        buttonBox.getChildren().set(0, btn1);
    }

    /**
     * Place a button in the second button slot with your own text, click behavior etc.
     *
     * @param btn2 A fully configured button
     */
    public void setButton2(Button btn2) {
        if (btn2 == null)
            throw new IllegalArgumentException("Button 2 parameter can not be null");

        HBox buttonBox = ensureButtonBox();
        ensureButtonExistence(buttonBox, 2);
        buttonBox.getChildren().set(1, btn2);
    }

    /**
     * Place a button in the third button slot with your own text, click behavior etc.
     *
     * @param btn3 A fully configured button
     */
    public void setButton3(Button btn3) {
        if (btn3 == null)
            throw new IllegalArgumentException("Button 3 parameter can not be null");

        HBox buttonBox = ensureButtonBox();
        ensureButtonExistence(buttonBox, 3);
        buttonBox.getChildren().set(2, btn3);
    }

    /**
     * In <a href="https://github.com/Stekeblad/Stekeblads-Video-Uploader/issues/36" target="_top">issue #36</a>
     * the buttonBox happened to be null for a user. Not being able to figure out how that is possible I have
     * to resort to testing for null and recreating it, risking other consequences...
     *
     * @return The buttonBox that holds the action buttons for this VideoInformationBase item.
     */
    private HBox ensureButtonBox() {
        Node buttonBoxNode = videoBasePane.lookup("#" + paneId + NODE_ID_BUTTONSBOX);
        if (buttonBoxNode instanceof HBox)
            return (HBox) buttonBoxNode;

        HBox buttonBox = new HBox(5);
        buttonBox.setId(paneId + NODE_ID_BUTTONSBOX);
        videoBasePane.add(buttonBox, 0, 5);
        return buttonBox;
    }

    /**
     * Inserts invisible ghost buttons to make sure the buttonBox contains at least minSize number of children.
     *
     * @param buttonBox The buttonBox yo verify.
     * @param minSize   the minimum number of children's in the buttonBox required in the context of the caller.
     */
    private void ensureButtonExistence(HBox buttonBox, int minSize) {
        ObservableList<Node> children = buttonBox.getChildren();
        while (children.size() < minSize) {
            Button ghostBtn = new Button("");
            ghostBtn.setVisible(false);
            children.add(ghostBtn);
        }
    }

    /**
     * Enables / Disables editing of all the fields on the pane.
     * Also disables left and right click functions if parameter is false, will not re-enable later if parameter true!
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
        videoBasePane.lookup("#" + paneId + NODE_ID_TELL_SUBS).setDisable(!newEditStatus);
        videoBasePane.lookup("#" + paneId + NODE_ID_MADE_FOR_KIDS).setDisable(!newEditStatus);
        if (newEditStatus) {
            // check valid status of category and playlist, update available choices if needed
            List<String> playlistChoices = playlistUtils.getVisiblePlaylistNames();
            if (!getPlaylistChoices().equals(playlistChoices)) {
                setPlaylistChoices(playlistChoices);
            }
            List<String> categories = categoryUtils.getCategoryNames();
            if (!getCategoryChoices().equals(categories)) {
                setCategories(categories);
            }
        } else {
            // Disable thumbnail click
            setOnThumbnailClicked(null);
            setThumbnailContextMenu(null);
        }
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
            if (thumbnail == null) {
                //reset to default
                thumbNailFile = null;
                ((ImageView) videoBasePane.lookup("#" + paneId + NODE_ID_THUMBNAIL)).setImage(
                        new Image(this.getClass().getResourceAsStream("/images/no_image.png")));
            } else {
                ((ImageView) videoBasePane.lookup("#" + paneId + NODE_ID_THUMBNAIL)).setImage(
                        new Image(new FileInputStream(thumbnail)));
                thumbNailFile = thumbnail;
            }
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

    public void setThumbnailContextMenu(ContextMenu menu) {
        videoBasePane.lookup("#" + paneId + NODE_ID_THUMBNAIL).setOnContextMenuRequested(event -> {
            event.consume();
            if (menu == null) {
                return;
            }
            menu.show(videoBasePane.lookup("#" + paneId + NODE_ID_THUMBNAIL), Side.BOTTOM, 0, -50);
        });
    }

    /**
     * Set a method to be called when the cursor enters or exits the thumbnail.
     * The method will only be called if allowEdit is true.
     * The set method will receive the value true as a parameter on enter events and false on exit events
     *
     * @param handler a method accepting a boolean as only parameter that should be called when the
     *                cursor enters and leaves the thumbnail
     */
    public void setThumbnailCursorEventHandler(Consumer<Boolean> handler) {
        thumbnailCursorEventHandler = handler;
    }

    /**
     * Sets the strings that will appear in the playlists ChoiceBox
     * If there already are some playlists set the method will attempt to keep the currently selected item selected
     * if it still exist, else it will selected the item at index 0.
     * If null or a empty list is provided no change will be done
     * @param playlistNames A ArrayList with the names of all playlists
     */
    @SuppressWarnings("unchecked")
    private void setPlaylistChoices(List<String> playlistNames) {
        if (playlistNames == null || playlistNames.size() == 0) {
            return;
        }
        // Get the ChoiceBox and currently selected item
        ChoiceBox<String> choiceBox = (ChoiceBox<String>) videoBasePane.lookup("#" + paneId + NODE_ID_PLAYLIST);
        String selected = choiceBox.getValue();
        // Change the items in the list
        choiceBox.itemsProperty().set(FXCollections.observableList(playlistNames));
        // Check if the selected item is still available and if so select it, else select item at index 0
        if (selected == null) {
            choiceBox.getSelectionModel().select(0);
        } else {
            if (choiceBox.getItems().contains(selected)) {
                choiceBox.getSelectionModel().select(selected);
            } else {
                choiceBox.getSelectionModel().select(0);
            }
        }
    }

    /**
     * Sets the categories that will appear in the categories ChoiceBox
     * @param categories A ArrayList with the names of all categories
     */
    @SuppressWarnings("unchecked")
    private void setCategories(List<String> categories) {
        if(categories != null) {
            ((ChoiceBox<String>) videoBasePane.lookup("#" + paneId + NODE_ID_CATEGORY)).setItems(FXCollections.observableArrayList(categories));
        }
    }

    public void setMadeForKids(boolean madeForKids) {
        ((CheckBox) videoBasePane.lookup("#" + paneId + NODE_ID_MADE_FOR_KIDS)).setSelected(madeForKids);
    }

    // other methods

    /**
     * Constructor for VideoInformationBase. There is also the VideoInformationBase.Builder class if that is preferred.
     * @param videoName Title of the video
     * @param videoDescription Content of the description field
     * @param visibility If the video should be public, private or unlisted
     * @param videoTags A List of string with all tags for the video
     * @param selectedPlaylist The name of a playlist for this video to be in
     * @param category Name of a Youtube category
     * @param tellSubs Set to true if subscribers should be notified
     * @param thumbNailPath File path to a image to use as thumbnail
     * @param paneId A string used for naming all UI elements
     */
    VideoInformationBase(String videoName, String videoDescription, VisibilityStatus visibility, List<String> videoTags,
                         String selectedPlaylist, String category, boolean tellSubs,
                         String thumbNailPath, boolean madeForKids, String paneId) {

        if (visibility == null) { // optional, default to public
            visibility = VisibilityStatus.PUBLIC;
        }
        this.paneId = paneId;
        if (thumbNailPath != null) {
            this.thumbNailFile = new File(thumbNailPath);
        } else {
            this.thumbNailFile = null;
        }
        makeVideoBasePane(videoName, videoDescription, visibility, videoTags, selectedPlaylist,
                category, tellSubs, thumbNailPath, madeForKids);
        allowEdit = false;
    }

    /**
     * Reconstructs a VideoInformationBase form its string version created by calling toString()
     * @param fromString The string representation of a VideoInformationBase
     * @param paneId A string used for naming all UI elements
     * @throws Exception If the string could not be converted to a VideoInformationBase
     */
    VideoInformationBase(String fromString, String paneId) throws Exception {

        this.paneId = paneId;

        String videoName = null;
        String videoDescription = null;
        VisibilityStatus visibility = null;
        ArrayList<String> videoTags = null;
        String selectedPlaylist = null;
        String category = null;
        boolean tellSubs = false;
        String thumbnailPath = null;
        boolean madeForKids = false;

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
                throw new Exception("Malformed string representation of class. Input: \n\"" + fromString + "\"");
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
                        selectedPlaylist = line.substring(colonIndex + 1);
                        break;
                    case NODE_ID_CATEGORY:
                        category = line.substring(colonIndex + 1);
                        break;
                    case NODE_ID_TELL_SUBS:
                        tellSubs = Boolean.parseBoolean(line.substring(colonIndex + 1));
                        break;
                    case NODE_ID_THUMBNAIL:
                        thumbnailPath = line.substring(colonIndex + 1);
                        if(thumbnailPath.equals("_")) {
                            this.thumbNailFile = null; // use default
                        } else {
                            this.thumbNailFile = new File(thumbnailPath);
                        }
                        break;
                    case NODE_ID_MADE_FOR_KIDS:
                        madeForKids = Boolean.parseBoolean(line.substring(colonIndex + 1));
                    default:
                        //ignore, might be a child value
                }
            }
        }
        makeVideoBasePane(videoName, videoDescription, visibility, videoTags, selectedPlaylist,
                category, tellSubs, thumbnailPath, madeForKids);
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
            paneIdForCopy = paneId;
        }
        return new VideoInformationBase(getVideoName(), getVideoDescription(), getVisibility(),
                getVideoTags(), getSelectedPlaylist(), getCategory(), isTellSubs(),
                getThumbNail().getAbsolutePath(), isMadeForKids(), paneIdForCopy);
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
        String selectedPlaylist;
        String category;
        boolean tellSubs;
        String thumbNailPath;
        String paneName;
        boolean madeForKids;

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

        public String getSelectedPlaylist() {
            return selectedPlaylist;
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

        public boolean isMadeForKids() {
            return madeForKids;
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

        public VideoInformationBase.Builder setSelectedPlaylist(String selectedPlaylist) {
            this.selectedPlaylist = selectedPlaylist;
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

        public VideoInformationBase.Builder setMadeForKids(boolean madeForKids) {
            this.madeForKids = madeForKids;
            return this;
        }

        public VideoInformationBase.Builder setPaneName(String paneName) {
            this.paneName = paneName;
            return this;
        }

        public VideoInformationBase build() {
            return new VideoInformationBase(videoName, videoDescription, visibility, videoTags, selectedPlaylist,
                    category, tellSubs, thumbNailPath, madeForKids, paneName);
        }
    }

    /**
     * Creates the UI Pane so it can be be retrieved by front end code with getPane()
     * @param videoName Content of the video title field
     * @param videoDescription Content of the video description field
     * @param visibility The VisibilityStatus to be selected by default
     * @param videoTags The tags to be inside th tags field
     * @param selectedPlaylist The name of the playlist to be selected by default
     * @param category The category to be selected by default
     * @param tellSubs The default value of tellSubs
     * @param thumbNailPath The path to the selected thumbnail or null for the no thumbnail selected image
     */
    private void makeVideoBasePane(String videoName, String videoDescription, VisibilityStatus visibility,
                                   List<String> videoTags, String selectedPlaylist,
                                   String category, boolean tellSubs, String thumbNailPath, boolean madeForKids) {
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
             // Add the tags to the screen with a ", " separation between them
             tags.setText(String.join(", ", videoTags));
         } else {
             tags.setText("");
         }
         tags.setEditable(false);
         tags.setWrapText(true);
         tags.textProperty().addListener((observable, oldValue, newValue) -> { //Prevent newlines, allow text wrap
                 tags.setText(newValue.replaceAll("\\R", ""));
         });

        ChoiceBox<String> playlistChoiceBox = new ChoiceBox<>(
                FXCollections.observableArrayList(PlaylistUtils.INSTANCE.getVisiblePlaylistNames()));
        if (selectedPlaylist == null) {
            playlistChoiceBox.getSelectionModel().select(0);
        } else {
            if (playlistChoiceBox.getItems().contains(selectedPlaylist)) {
                playlistChoiceBox.getSelectionModel().select(selectedPlaylist);
            } else {
                playlistChoiceBox.getSelectionModel().select(0);
            }
        }
         playlistChoiceBox.setId(paneId + NODE_ID_PLAYLIST);
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
        tellSubsChoiceBox.setId(paneId + NODE_ID_TELL_SUBS);
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
         thumbNailFrame.setOnMouseEntered(event -> {
             if (thumbnailCursorEventHandler != null && allowEdit) {
                 thumbnailCursorEventHandler.accept(true);
             }
         });
        thumbNailFrame.setOnMouseExited(event -> {
            if (thumbnailCursorEventHandler != null && allowEdit) {
                thumbnailCursorEventHandler.accept(false);
            }
        });

        CheckBox checkBoxMFK = new CheckBox("Flag video as \"Made for kids\"");
        checkBoxMFK.setId(paneId + NODE_ID_MADE_FOR_KIDS);
        checkBoxMFK.setOnContextMenuRequested(event -> {
            event.consume();
            OpenInBrowser.openInBrowser("https://support.google.com/youtube/answer/9528076");
        });
        checkBoxMFK.setTooltip(new Tooltip("Left click to toggle selection, right click to open help about Made for kids"));
        checkBoxMFK.setSelected(madeForKids);
        checkBoxMFK.setDisable(true);

        // Create empty buttons so setters/getters for the buttons do not throw NullPointerException
        Button ghostBtn1 = new Button("");
        ghostBtn1.setVisible(false);
        Button ghostBtn2 = new Button("");
        ghostBtn2.setVisible(false);
        Button ghostBtn3 = new Button("");
        ghostBtn3.setVisible(false);
        // The buttonBox gets recreated in ensureButtonBox, if changed here, also update there!
        HBox buttonsBox = new HBox(5, ghostBtn1, ghostBtn2, ghostBtn3);
        buttonsBox.setId(getPaneId() + NODE_ID_BUTTONSBOX);

         // Place the different nodes on the pane
         videoBasePane.add(title, 0, 0);
         videoBasePane.add(categoryChoiceBox, 1, 0);
         videoBasePane.add(playlistChoiceBox, 2, 0);

        videoBasePane.add(description, 0, 1, 1, 4);
        videoBasePane.add(tags, 1, 1, 1, 2);
        videoBasePane.add(checkBoxMFK, 1, 3);

        videoBasePane.add(thumbNailFrame, 2, 1, 1, 3);

        videoBasePane.add(tellSubsChoiceBox, 1, 4);
        videoBasePane.add(visibilityChoiceBox, 2, 4);

        videoBasePane.add(buttonsBox, 0, 5);

         // Sizing
         ColumnConstraints rightConstraint = new ColumnConstraints(170, 170, 170);
         ColumnConstraints defaultConstraint = new ColumnConstraints(100, USE_COMPUTED_SIZE, MAX_VALUE);
         videoBasePane.getColumnConstraints().setAll(defaultConstraint, defaultConstraint, rightConstraint);

        RowConstraints rowConstraint = new RowConstraints(30);
        videoBasePane.getRowConstraints().setAll(rowConstraint, rowConstraint,
                rowConstraint, rowConstraint, rowConstraint, rowConstraint);
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
                System.err.println("Failed getting the path of the thumbnail while creating a string of " + paneId + " named " + getVideoName());
                thumbnailSave = "_";
            }
        }
        classString.append(NODE_ID_TITLE + ":").append(getVideoName()).append("\n")
                .append(NODE_ID_DESCRIPTION).append(":").append(getVideoDescription()).append("\n")
                .append(NODE_ID_VISIBILITY).append(":").append(getVisibility().getStatusName().toUpperCase()).append("\n")
                .append(NODE_ID_TAGS).append(":").append(getVideoTags().toString()).append("\n")
                .append(NODE_ID_PLAYLIST).append(":").append(getSelectedPlaylist()).append("\n")
                .append(NODE_ID_CATEGORY).append(":").append(getCategory()).append("\n")
                .append(NODE_ID_TELL_SUBS).append(":").append(isTellSubs()).append("\n")
                .append(NODE_ID_THUMBNAIL).append(":").append(thumbnailSave).append("\n")
                .append(NODE_ID_MADE_FOR_KIDS).append(":").append(isMadeForKids());
        return classString.toString();

    }
}
