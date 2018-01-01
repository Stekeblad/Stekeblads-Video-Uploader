package io.github.stekeblad.youtubeuploader.fxml;

import io.github.stekeblad.youtubeuploader.youtube.constants.Categories;
import io.github.stekeblad.youtubeuploader.youtube.constants.VisibilityStatus;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.EnumSet;

public class PaneFactory {

    public static final String NODE_ID_TITLE = "_title";
    public static final String NODE_ID_DESCRIPTION = "_description";
    public static final String NODE_ID_CATEGORY = "_category";
    public static final String NODE_ID_TAGS = "_tags";
    public static final String NODE_ID_PLAYLIST = "_playlist";
    public static final String NODE_ID_VISIBILITY = "_visibility";
    public static final String NODE_ID_TELLSUBS = "_tellSubs";
    public static final String NODE_ID_PRESETNAME = "_presetName";
    public static final String NODE_ID_PROGRESS = "_progress";

    public static GridPane makeVideoDetailsPane(String idName) {
        GridPane pane = new GridPane();
        pane.setId(idName);
        pane.setPrefSize(500, 100);
        pane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

        TextField title = new TextField();
        title.setId(idName + NODE_ID_TITLE);
        title.setPromptText("Video title");
        title.setEditable(false);

        TextArea description = new TextArea();
        description.setId(idName + NODE_ID_DESCRIPTION);
        description.setPromptText("Video description");
        description.setEditable(false);
        description.setWrapText(true);

        ArrayList<Categories> categories = new ArrayList<>(EnumSet.allOf(Categories.class));
        ArrayList<String> categoryStrings = new ArrayList<>();
        for (Categories category1 : categories) {
            categoryStrings.add(category1.getName());
        }
        ChoiceBox<String> category = new ChoiceBox<>(FXCollections.observableArrayList(categoryStrings));
        category.setId(idName + NODE_ID_CATEGORY);
        category.getSelectionModel().select(0);
        category.setTooltip(new Tooltip("Youtube Video Category"));
        category.setDisable(true);

        TextArea tags = new TextArea();
        tags.setId(idName + NODE_ID_TAGS);
        tags.setPromptText("list, of, tags, separated, with, comma, and, space");
        tags.setEditable(false);

        TextField playlist = new TextField();
        playlist.setId(idName + NODE_ID_PLAYLIST);
        playlist.setPromptText("playlist url");
        playlist.setEditable(false);

        ArrayList<VisibilityStatus> statuses = new ArrayList<>(EnumSet.allOf(VisibilityStatus.class));
        ArrayList<String> visibilityStrings = new ArrayList<>();
        for (VisibilityStatus status : statuses) {
            visibilityStrings.add(status.getStatusName());
        }
        ChoiceBox<String> visibility = new ChoiceBox<>(FXCollections.observableArrayList(visibilityStrings));
        visibility.setId(idName + NODE_ID_VISIBILITY);
        visibility.getSelectionModel().select(0);
        visibility.setTooltip(new Tooltip("how will the video be accessible?"));
        visibility.setDisable(true);

        ArrayList<String> tellSubsOptions = new ArrayList<>();
        tellSubsOptions.add("Do not Notify Subscribers");
        tellSubsOptions.add("Notify Subscribers");
        ChoiceBox<String> tellSubs = new ChoiceBox<>(FXCollections.observableArrayList(tellSubsOptions));
        tellSubs.setId(idName + NODE_ID_TELLSUBS);
        tellSubs.getSelectionModel().select(0);
        tellSubs.setTooltip(new Tooltip(
                "Should the channel's subscribers be notified that a new video has been uploaded? Not recommended then uploading alot of videos \nBut why are you using this program then?"));
        tellSubs.setDisable(true);

        pane.add(title, 0, 0);
        pane.add(description, 0, 1, 1, 3);
        pane.add(category, 1, 0);
        pane.add(tags, 1, 1, 1, 2);
        pane.add(playlist, 1, 3);
        pane.add(visibility, 1, 4);
        pane.add(tellSubs, 0, 4);
        return pane;
    }

    public static GridPane makePresetPane(String id) {
        GridPane pane = makeVideoDetailsPane(id);
        pane.setPrefHeight(150);

        TextField presetName = new TextField();
        presetName.setId(id + NODE_ID_PRESETNAME);
        presetName.setPromptText("Preset name");
        presetName.setEditable(false);
        pane.add(presetName, 0, 5);

        return pane;
    }

    public static GridPane makeUploadPane(String id) {
        GridPane pane = makeVideoDetailsPane(id);
        pane.setPrefHeight(150);

        ProgressBar progressBar = new ProgressBar();
        progressBar.setId(id + NODE_ID_PROGRESS);
        progressBar.setPrefWidth(200);

        pane.add(progressBar, 0, 5);

        return pane;
    }

    public static Pane makeSingleLabelPane(String id) {
        Pane pane = new Pane();
        pane.setId(id);
        pane.setPrefHeight(20);

        Label textLabel = new Label();
        textLabel.setId(id + "_label");

        pane.getChildren().add(textLabel);

        return pane;
    }

}
