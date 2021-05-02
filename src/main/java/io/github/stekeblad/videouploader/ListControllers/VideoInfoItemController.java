package io.github.stekeblad.videouploader.ListControllers;

import io.github.stekeblad.videouploader.managers.CategoryManager;
import io.github.stekeblad.videouploader.managers.PlaylistManager;
import io.github.stekeblad.videouploader.models.NewVideoInfoBaseModel;
import io.github.stekeblad.videouploader.utils.FileUtils;
import io.github.stekeblad.videouploader.utils.background.OpenInBrowser;
import io.github.stekeblad.videouploader.utils.translation.TranslationBundles;
import io.github.stekeblad.videouploader.utils.translation.Translations;
import io.github.stekeblad.videouploader.utils.translation.TranslationsManager;
import io.github.stekeblad.videouploader.youtube.LocalCategory;
import io.github.stekeblad.videouploader.youtube.LocalPlaylist;
import io.github.stekeblad.videouploader.youtube.VisibilityStatus;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.io.*;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.lang.Integer.MAX_VALUE;

public abstract class VideoInfoItemController<T extends NewVideoInfoBaseModel> extends GridPane {
    // VideoInfoBase FX variables
    public TextField title;
    public TextArea description;
    public ChoiceBox<VisibilityStatus> visibility;
    public TextArea tags;
    public ChoiceBox<LocalPlaylist> playlist;
    public ChoiceBox<LocalCategory> category;
    public CheckBox notifySubscribers;
    public ImageView thumbnail;
    public CheckBox madeForKids;

    // Other FX variables
    protected GridPane innerPane; // pane for fields that should be enabled/disabled together (most nodes excluding buttons)
    protected HBox buttonBox; // Box for buttons, positioned below innerPane in the rootPane

    // Other fields
    protected T model;
    protected static Translations translations = null;

    // Private constants
    private static final String NODE_ID_TITLE = "title";
    private static final String NODE_ID_DESCRIPTION = "description";
    private static final String NODE_ID_CATEGORY = "category";
    private static final String NODE_ID_TAGS = "tags";
    private static final String NODE_ID_PLAYLIST = "playlist";
    private static final String NODE_ID_VISIBILITY = "visibility";
    private static final String NODE_ID_TELL_SUBS = "tellSubs";
    private static final String NODE_ID_THUMBNAIL = "thumbNail";
    private static final String NODE_ID_MADE_FOR_KIDS = "madeForKids";

    private static final List<String> THUMBNAIL_FILE_FORMAT = Arrays.asList("*.jpg", "*.png");
    private static final long MAX_THUMB_SIZE = 2 * 1024 * 1024;


    protected VideoInfoItemController(T infoItem, ReadOnlyDoubleProperty parentPrefWidthProperty) {
        if (translations == null)
            translations = TranslationsManager.getTranslation(TranslationBundles.PRESET_UPLOAD);
        if (infoItem == null)
            throw new NullPointerException("infoItem argument cannot be null");
        model = infoItem;
        constructPane();
        if (parentPrefWidthProperty != null)
            this.prefWidthProperty().bind(parentPrefWidthProperty.subtract(35));
    }

    /**
     * Copies properties from tIn to tOut. With he exception for UniqueId that is randomized on creation and cant be changed
     *
     * @param tIn  the instance of T to copy from
     * @param tOut the instance of T to copy to
     */
    protected void clone(T tIn, T tOut) {
        tOut.setVideoName(tIn.getVideoName());
        tOut.setVideoDescription(tIn.getVideoDescription());
        tOut.setVisibility(tIn.getVisibility());
        tOut.setVideoTags(tIn.getVideoTags());
        tOut.setSelectedPlaylist(tIn.getSelectedPlaylist());
        tOut.setSelectedCategory(tIn.getSelectedCategory());
        tOut.setTellSubs(tIn.isTellSubs());
        tOut.setThumbnailPath(tIn.getThumbnailPath());
        tOut.setMadeForKids(tIn.isMadeForKids());
    }

    // ------------------------------------------------------------------------
    // Public methods
    // ------------------------------------------------------------------------

    public abstract T getModel();

    public UUID getUniqueModelId() {
        return model.getUniqueId();
    }

    public void startEdit(ActionEvent actionEvent) {
        innerPane.setDisable(false);
    }

    public void commitEdit(ActionEvent actionEvent) {
        innerPane.setDisable(true);
        // update the model with values from the input fields
        model.setVideoName(title.getText());
        model.setVideoDescription(description.getText());
        model.setVisibility(visibility.getValue());
        model.setVideoTags(Arrays.stream(tags.getText().split(","))
                .map((s) -> s = s.strip()).collect(Collectors.toList()));
        model.setSelectedPlaylist(playlist.getValue());
        model.setSelectedCategory(category.getValue());
        model.setTellSubs(notifySubscribers.isSelected());
        model.setThumbnailPath(thumbnail.getImage().getUrl());
        model.setMadeForKids(madeForKids.isSelected());
    }

    public void cancelEdit(ActionEvent actionEvent) {
        innerPane.setDisable(true);
        // restore the input field values from the model
        title.setText(model.getVideoName());
        description.setText(model.getVideoDescription());
        visibility.getSelectionModel().select(model.getVisibility());
        tags.setText(String.join(", ", model.getVideoTags()));
        playlist.getSelectionModel().select(model.getSelectedPlaylist());
        category.getSelectionModel().select(model.getSelectedCategory());
        notifySubscribers.setSelected(model.isTellSubs());
        thumbnail.setImage(new Image(model.getThumbnailPath()));
        madeForKids.setSelected(model.isMadeForKids());
    }

    // ------------------------------------------------------------------------
    // Private methods
    // ------------------------------------------------------------------------

    private Image getSelectedOrDefaultImage(String thumbnailPath) {
        InputStream stream = null;
        Image image;
        try {
            try {
                stream = new FileInputStream(new File(thumbnailPath));
            } catch (FileNotFoundException | NullPointerException e) {
                stream = this.getClass().getResourceAsStream("/images/no_image.png");
            }
            image = new Image(stream);
        } finally {
            try {
                if (stream != null)
                    stream.close();
            } catch (IOException ignored) {
            }
        }
        return image;
    }

    /**
     * <p>For thumbnail: Changing cursor, open file picker and showing context menu</p>
     * <p>For madeForKids: Context menu</p>
     */
    private void addNodeEvents() {
        // I hope the list items will resize with the parent automatically
        // but if not I may be able to bind the size like this:
        //this.getListView().prefWidthProperty().bind(rootPane.widthProperty().subtract(35));

        // Thumbnail - Update cursor
        thumbnail.setOnMouseEntered((event) -> {
            event.consume();
            this.getScene().setCursor(Cursor.HAND);
        });
        thumbnail.setOnMouseExited((event -> {
            event.consume();
            this.getScene().setCursor(Cursor.DEFAULT);
        }));

        // Thumbnail - File picker
        thumbnail.setOnMouseClicked(event -> {
            event.consume();
            if (event.getButton() != MouseButton.PRIMARY) return; // Conflicting with context menu
            File pickedThumbnail = FileUtils.pickThumbnail(THUMBNAIL_FILE_FORMAT, MAX_THUMB_SIZE);
            if (pickedThumbnail != null) {
                thumbnail.setImage(getSelectedOrDefaultImage(pickedThumbnail.getPath()));
            }
        });

        // Thumbnail - Context menu
        ContextMenu thumbnailRClickMenu = new ContextMenu();
        MenuItem item1 = new MenuItem(translations.getString("resetToDefault"));
        item1.setOnAction(actionEvent -> {
            actionEvent.consume();
            thumbnail.setImage(getSelectedOrDefaultImage(null));
        });
        thumbnailRClickMenu.getItems().add(item1);
        thumbnail.setOnContextMenuRequested((event -> {
            event.consume();
            thumbnailRClickMenu.show(thumbnail, Side.BOTTOM, 0, -50);
        }));

        // Made for kids - Context menu
        ContextMenu mfkRClickMenu = new ContextMenu();
        MenuItem item2 = new MenuItem("Open help");
        item2.setOnAction((actionEvent) -> {
            actionEvent.consume();
            OpenInBrowser.openInBrowser("https://support.google.com/youtube/answer/9528076");
        });
        mfkRClickMenu.getItems().add(item2);
        madeForKids.setOnContextMenuRequested(event -> {
            event.consume();
            mfkRClickMenu.show(madeForKids, Side.BOTTOM, 0, 0);
        });
    }

    private void constructPane() {
        innerPane = new GridPane();
        buttonBox = new HBox(5);

        // border
        this.setBorder(new Border(new BorderStroke(
                Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

        // row and column constraints
        RowConstraints emptyRowConstr = new RowConstraints();
        RowConstraints stdRowConstr = new RowConstraints(30);
        this.getRowConstraints().setAll(emptyRowConstr, stdRowConstr);
        innerPane.getRowConstraints().setAll(stdRowConstr, stdRowConstr, stdRowConstr, stdRowConstr, stdRowConstr);
        ColumnConstraints rightColConstr = new ColumnConstraints(170, 170, 170);
        ColumnConstraints stdColConstr = new ColumnConstraints(100, USE_COMPUTED_SIZE, MAX_VALUE);
        // no column constrains for rootPane
        innerPane.getColumnConstraints().setAll(stdColConstr, stdColConstr, rightColConstr);

        // Add the nodes for the rootPane
        this.add(innerPane, 0, 0);
        this.add(buttonBox, 0, 1);

        // create the nodes for the video info properties
        title = new TextField(model.getVideoName());
        title.setId(NODE_ID_TITLE);
        description = new TextArea(model.getVideoDescription());
        description.setId(NODE_ID_DESCRIPTION);
        description.setWrapText(true);
        visibility = new ChoiceBox<>(FXCollections.observableArrayList(EnumSet.allOf(VisibilityStatus.class)));
        visibility.setId(NODE_ID_VISIBILITY);
        visibility.getSelectionModel().select(model.getVisibility());
        tags = new TextArea(String.join(", ", model.getVideoTags()));
        tags.setId(NODE_ID_TAGS);
        tags.setWrapText(true);
        tags.textProperty().addListener((observable, oldValue, newValue) -> {
            //Prevent newlines, allow text wrap
            tags.setText(newValue.replaceAll("\\R", ""));
        });
        playlist = new ChoiceBox<>(PlaylistManager.getPlaylistManager().getVisiblePlaylists());
        playlist.setId(NODE_ID_PLAYLIST);
        playlist.getSelectionModel().select(model.getSelectedPlaylist());
        category = new ChoiceBox<>(CategoryManager.getCategoryManager().getCategories());
        category.setId(NODE_ID_CATEGORY);
        category.getSelectionModel().select(model.getSelectedCategory());
        notifySubscribers = new CheckBox();
        notifySubscribers.setId(NODE_ID_TELL_SUBS);
        notifySubscribers.setSelected(model.isTellSubs());
        thumbnail = new ImageView(model.getThumbnailPath());
        thumbnail.setId(NODE_ID_THUMBNAIL);
        thumbnail.setFitHeight(90);
        thumbnail.setFitWidth(160);
        thumbnail.setPreserveRatio(true);
        madeForKids = new CheckBox();
        madeForKids.setId(NODE_ID_MADE_FOR_KIDS);
        madeForKids.setSelected(model.isMadeForKids());

        // add the video info nodes to the inner pane
        innerPane.add(title, 0, 0);
        innerPane.add(category, 1, 0);
        innerPane.add(playlist, 2, 0);

        innerPane.add(description, 0, 1, 1, 4);
        innerPane.add(tags, 1, 1, 1, 2);
        innerPane.add(madeForKids, 1, 3);

        innerPane.add(thumbnail, 2, 1, 1, 3);

        innerPane.add(notifySubscribers, 1, 4);
        innerPane.add(visibility, 2, 4);

        addNodeEvents();
    }
}





























