package io.github.stekeblad.youtubeuploader.settings;

import io.github.lilahamstern.AlertBox;
import io.github.lilahamstern.ConfirmBox;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import static io.github.stekeblad.youtubeuploader.youtube.VideoInformationBase.NODE_ID_THUMBNAIL;


public class SettingsController implements Initializable {

    public AnchorPane SettingsWindow;
    public Button editPreset;
    public Button savePreset;
    public Button deletePreset;
    public ListView<GridPane> listPresets;

    private VideoPreset addNewPreset;
    private ArrayList<VideoPreset> videoPresets;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String newPresetId = "newPreset";
        addNewPreset = new VideoPreset("", "", VisibilityStatus.PUBLIC,
                new ArrayList<String>(), Categories.SPEL, false, null, newPresetId, "");
        addNewPreset.setEditable(true);
        GridPane newPreset = addNewPreset.getPresetPane();
        newPreset.setLayoutX(10);
        newPreset.setLayoutY(30);
        newPreset.setPrefSize(680, 150);
        newPreset.lookup("#" + newPresetId + NODE_ID_THUMBNAIL).setOnMouseClicked(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose a thumbnail");
            Stage fileChooserStage = new Stage();
            File thumbnail = fileChooser.showOpenDialog(fileChooserStage);
            if (thumbnail != null) {
                try {
                    addNewPreset.setThumbNailFile(thumbnail);
                } catch (Exception e) {
                    // If user managed to select a non-existing file (or editing is not allowed, it is allowed here)
                    e.printStackTrace();
                }
            }
        });
        SettingsWindow.getChildren().add(newPreset);
        videoPresets = new ArrayList<>();
        updatePresetList();
    }

    public void onPresetEdit(ActionEvent actionEvent) {

    }

    public void onPresetSave(ActionEvent actionEvent) {

        boolean isPresetNameUnique = true;
        String nameNewPreset = addNewPreset.getPresetName();
        for (int i = 0; i < videoPresets.size(); i++) {
            VideoPreset preset = videoPresets.get(i);
            if(preset.getPresetName().equals(nameNewPreset)) {
                isPresetNameUnique = false;
                break;
            }
        }
        if (isPresetNameUnique) {
            VideoPreset newestPreset = addNewPreset.copy(addNewPreset.getPresetName());
            videoPresets.add(newestPreset);
            updatePresetList();
            System.out.println(newestPreset.toString());
        } else {
            AlertBox.display("Invalid Preset name",
                    "There is already a preset with that name. Select another one or edit/delete the existing preset.");
        }


        actionEvent.consume();
    }

    public void onPresetDelete(ActionEvent actionEvent) {
        int selected = listPresets.getSelectionModel().getSelectedIndex();
        if (selected < 0) { //no preset selected
            AlertBox.display("No preset selected", "No preset selected");
            actionEvent.consume();
            return;
        }
        if (ConfirmBox.display("Confirm delete",
                "Are you sure you want to delete preset " + videoPresets.get(selected).getPresetName() + "?")) {
            videoPresets.remove(selected);
            updatePresetList();
        }

        actionEvent.consume();
    }

    private void updatePresetList() {
        ArrayList<GridPane> presetPanes = new ArrayList<>();
        for (int i = 0; i < videoPresets.size(); i++) {
            presetPanes.add(videoPresets.get(i).getPresetPane());
        }
        listPresets.setItems(FXCollections.observableArrayList(presetPanes));
    }
}
