package io.github.stekeblad.youtubeuploader.settings;

import io.github.stekeblad.youtubeuploader.fxml.PaneFactory;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {

    public AnchorPane SettingsWindow;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        GridPane newPreset = PaneFactory.makePresetPane("newPreset");
        newPreset.setLayoutX(10);
        newPreset.setLayoutY(40);
        newPreset.setPrefSize(500, 140);
        ((TextField) newPreset.lookup("#newPreset_title")).setEditable(true);
        ((TextArea) newPreset.lookup("#newPreset_description")).setEditable(true);
        ((TextArea) newPreset.lookup("#newPreset_tags")).setEditable(true);
        ((TextField) newPreset.lookup("#newPreset_playlist")).setEditable(true);
        ((TextField) newPreset.lookup("#newPreset_presetName")).setEditable(true);
        SettingsWindow.getChildren().add(newPreset);
    }
}
