package io.github.stekeblad.youtubeuploader.settings;

import io.github.stekeblad.youtubeuploader.youtube.VideoPreset;
import io.github.stekeblad.youtubeuploader.youtube.constants.Categories;
import io.github.stekeblad.youtubeuploader.youtube.constants.VisibilityStatus;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;


public class SettingsController implements Initializable {

    public AnchorPane SettingsWindow;
    public Button editPreset;
    public Button savePreset;
    public Button deletePreset;
    public ListView<GridPane> listPresets;

    private VideoPreset addNewPreset;
    private ArrayList<GridPane> videoPresets;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String newPresetId = "newPreset";
        addNewPreset = new VideoPreset("", "", VisibilityStatus.PUBLIC.getStatusName(),
                new ArrayList<String>(), Categories.DJUR_OCH_HUSDJUR, false, null, newPresetId, "");
        addNewPreset.setEditable(true);
        GridPane newPreset = addNewPreset.getPresetPane();
        newPreset.setLayoutX(10);
        newPreset.setLayoutY(40);
        newPreset.setPrefSize(500, 140);
        SettingsWindow.getChildren().add(newPreset);
        videoPresets = new ArrayList<>();
        listPresets.setItems(FXCollections.observableArrayList(videoPresets));
    }

    public void onPresetEdit(ActionEvent actionEvent) {

    }

    public void onPresetSave(ActionEvent actionEvent) {
        VideoPreset newestPreset = addNewPreset.copy(addNewPreset.getPresetName());
        videoPresets.add(newestPreset.getPresetPane());
        listPresets.setItems(FXCollections.observableArrayList(videoPresets));



        //Gson gson = new Gson();
        //gson.toJson(SettingsWindow.lookup("#" + "newPreset"));
    }

    public void onPresetDelete(ActionEvent actionEvent) {

    }
}
