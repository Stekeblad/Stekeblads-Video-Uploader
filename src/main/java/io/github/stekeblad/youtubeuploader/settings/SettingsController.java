package io.github.stekeblad.youtubeuploader.settings;

import io.github.stekeblad.youtubeuploader.fxml.PaneFactory;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.ResourceBundle;

import static io.github.stekeblad.youtubeuploader.fxml.PaneFactory.*;

public class SettingsController implements Initializable {

    public AnchorPane SettingsWindow;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String newPresetId = "newPreset";
        GridPane newPreset = PaneFactory.makePresetPane(newPresetId);
        newPreset.setLayoutX(10);
        newPreset.setLayoutY(40);

        // Default value for PaneFactory is to not allow editing in UI, enable it
        newPreset.setPrefSize(500, 140);
        ((TextField) newPreset.lookup(newPresetId + VIDEO_TITLE)).setEditable(true);
        ((TextArea) newPreset.lookup(newPresetId + VIDEO_DESCRIPTION)).setEditable(true);
        newPreset.lookup(newPresetId + VIDEO_CATEGORY).setDisable(false);
        ((TextArea) newPreset.lookup(newPresetId + VIDEO_TAGS)).setEditable(true);
        ((TextField) newPreset.lookup(newPresetId + VIDEO_PLAYLIST)).setEditable(true);
        newPreset.lookup(newPresetId + VIDEO_VISIBILITY).setDisable(false);
        newPreset.lookup(newPresetId + VIDEO_TELLSUBS).setDisable(false);
        ((TextField) newPreset.lookup(newPresetId + VIDEO_PRESETNAME)).setEditable(true);
        SettingsWindow.getChildren().add(newPreset);
    }
}
