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

    public static GridPane makeVideoDetailsPane(String idName) {
        GridPane pane = new GridPane();
        pane.setId(idName);
        pane.setPrefSize(500, 100);
        pane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

        TextField title = new TextField();
        title.setId(idName + "_title");
        title.setPromptText("Video title");
        title.setEditable(false);

        TextArea description = new TextArea();
        description.setId(idName + "_description");
        description.setPromptText("Video description");
        description.setEditable(false);
        description.setWrapText(true);

        ArrayList<Categories> categories = new ArrayList<>(EnumSet.allOf(Categories.class));
        ArrayList<String> categoryStrings = new ArrayList<>();
        for (int i = 0; i < categories.size(); i++) {
            categoryStrings.add(categories.get(i).getName());
        }
        ChoiceBox<String> category = new ChoiceBox<>(FXCollections.observableArrayList(categoryStrings));
        category.setId(idName + "_category");
        category.getSelectionModel().select(0);
        category.setTooltip(new Tooltip("Youtube Video Category"));

        TextArea tags = new TextArea();
        tags.setId(idName + "_tags");
        tags.setPromptText("list, of, tags, separated, with, comma, and, space");
        tags.setEditable(false);

        TextField playlist = new TextField();
        playlist.setId(idName + "_playlist");
        playlist.setPromptText("playlist url");
        playlist.setEditable(false);

        ArrayList<VisibilityStatus> statuses = new ArrayList<>(EnumSet.allOf(VisibilityStatus.class));
        ArrayList<String> visibilityStrings = new ArrayList<>();
        for (int i = 0; i < statuses.size(); i++) {
            visibilityStrings.add(statuses.get(i).getStatusName());
        }
        ChoiceBox<String> visibility = new ChoiceBox<>(FXCollections.observableArrayList(visibilityStrings));
        visibility.setId(idName + "_visibility");
        visibility.getSelectionModel().select(0);
        visibility.setTooltip(new Tooltip("how will the video be accessible?"));

        ArrayList<String> tellSubsOptions = new ArrayList<>();
        tellSubsOptions.add("Notify Subscribers");
        tellSubsOptions.add("Do not Notify Subscribers");
        ChoiceBox<String> tellSubs = new ChoiceBox<>(FXCollections.observableArrayList(tellSubsOptions));
        tellSubs.setId(idName + "_tellSubs");
        tellSubs.getSelectionModel().select(0);
        tellSubs.setTooltip(new Tooltip(
                "Should the channel's subscribers be notified that a new video has been uploaded? Not recommended then uploading alot of videos"));

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
        pane.setPrefHeight(130);

        TextField presetName = new TextField();
        presetName.setId(id + "_presetName");
        presetName.setPromptText("Preset name");
        presetName.setEditable(false);
        pane.add(presetName, 0, 5);

        return pane;
    }

    public static GridPane makeUploadPane(String id) {
        GridPane pane = makeVideoDetailsPane(id);
        pane.setPrefHeight(130);

        ProgressBar progressBar = new ProgressBar();
        progressBar.setId(id + "_progress");
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
