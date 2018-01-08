package io.github.stekeblad.youtubeuploader.settings;

import io.github.stekeblad.youtubeuploader.utils.AlertUtils;
import io.github.stekeblad.youtubeuploader.utils.ConfigManager;
import io.github.stekeblad.youtubeuploader.youtube.PlaylistUtils;
import io.github.stekeblad.youtubeuploader.youtube.VideoPreset;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

import static io.github.stekeblad.youtubeuploader.utils.Constants.BUTTON_DELETE;
import static io.github.stekeblad.youtubeuploader.utils.Constants.BUTTON_EDIT;
import static io.github.stekeblad.youtubeuploader.youtube.VideoInformationBase.NODE_ID_THUMBNAIL;
import static io.github.stekeblad.youtubeuploader.youtube.VideoInformationBase.THUMBNAIL_FILE_FORMAT;


public class PresetsWindowController implements Initializable {

    public AnchorPane SettingsWindow;
    public ListView<GridPane> listPresets;
    public Button btn_tips;
    public Button addNewPreset;

    private ArrayList<VideoPreset> videoPresets;
    private ConfigManager configManager;
    private PlaylistUtils playlistUtils;
    private String newPresetId = "newPreset";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configManager = ConfigManager.INSTANCE;
        playlistUtils = PlaylistUtils.INSTANCE;
        //addNewPreset = new VideoPreset("", "", VisibilityStatus.PUBLIC,
        //        new ArrayList<>(), "", Categories.SPEL, false, null, newPresetId, "");
        //addNewPreset.setEditable(true);
        //GridPane newPreset = addNewPreset.getPresetPane();
        //newPreset.setLayoutX(10);
        //newPreset.setLayoutY(30);
        //newPreset.setPrefSize(680, 150);
        //newPreset.lookup("#" + newPresetId + NODE_ID_THUMBNAIL).setOnMouseClicked(event -> {

        //});
       /* newPreset.lookup("#" + newPresetId + NODE_ID_PLAYLIST).setOnMouseClicked(event -> {
            if (configManager.getNeverAuthed()) {
            Optional<ButtonType> buttonChoice = AlertUtils.yesNo("Authentication Required", "To select a playlist you must grant this application permission to access your Youtube channel. " +
                    "Do you want to allow \"Stekeblads Youtube Uploader\" permission to access Your channel?" +
                    "\n\nPermission overview: \"YOUTUBE_UPLOAD\" for allowing the program to upload videos for you" +
                    "\n\"YOUTUBE\" for basic account access, adding videos to playlists and setting thumbnails" +
                    "\n\nPress yes to open your browser for authentication or no to cancel")
                    .showAndWait();
            if (buttonChoice.isPresent()) {
                if (buttonChoice.get() == ButtonType.YES) {
                    configManager.setNeverAuthed(false);
                    configManager.saveSettings();
                } else { // ButtonType.NO
                    return;
                }
            }
        }
        ArrayList<String> playlistNames = playlistUtils.getUserPlaylistNames();
        addNewPreset.setPlaylists(playlistNames);
    });*/

        //SettingsWindow.getChildren().add(newPreset);

        videoPresets = new ArrayList<>();
        ArrayList<String> savedPresetNames = configManager.getPresetNames();
        for (String presetName : savedPresetNames) {
            try {
                VideoPreset videoPreset = new VideoPreset(configManager.getPresetString(presetName), presetName);
                videoPreset.getPane().lookup("#" + presetName + NODE_ID_THUMBNAIL).setOnMouseClicked(event ->
                        doFileChooser(listPresets.getChildrenUnmodifiable().indexOf(listPresets.lookup("#" + presetName))));
                Button editButton = new Button("Edit");
                editButton.setId(presetName + BUTTON_EDIT);
                editButton.setOnMouseClicked(event -> onPresetEdit(editButton.getId()));
                Button deleteButton = new Button("Delete");
                deleteButton.setId(presetName + BUTTON_DELETE);
                deleteButton.setOnMouseClicked(event -> onPresetDelete(deleteButton.getId()));
                HBox buttons = new HBox(5, editButton, deleteButton);
                videoPreset.getPane().add(buttons, 1, 4);
                videoPresets.add(videoPreset);
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Trying to load a preset that does not exist or missing read permission: " + presetName);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Bad format of preset or is another type of preset then the one trying to be created: " + presetName);
            }
        }

        updatePresetList();
    }


    private void onPresetEdit(String calledId) {
        AlertUtils.simpleClose("not yet", "Redoing this, not working right now"); //todo
        /*
        int selected = listPresets.getSelectionModel().getSelectedIndex();
        if (selected == -1) {
            AlertUtils.simpleClose("No preset selected", "No preset selected, cant edit.").show();
            return;
        }
        SettingsWindow.getChildren().set(
                SettingsWindow.getChildren().indexOf(SettingsWindow.lookup("#" + newPresetId)),
                listPresets.getSelectionModel().getSelectedItem());
                */
    }

    public void onPresetSave() {
        //Rewrite
    }

    private void onPresetDelete(String callerId) {
        String parentId = callerId.substring(0, callerId.indexOf('_'));
        int selected = -1;
        for(int i = 0; i < videoPresets.size(); i++) {
            if(videoPresets.get(i).getPaneId().equals(parentId)) {
                selected = i;
                break;
            }
        }
        if(selected == -1) {
            System.err.println("Non-existing button was pressed!!!");
            return;
        }
        Optional<ButtonType> buttonChoice = AlertUtils.yesNo("Confirm delete",
                "Are you sure you want to delete preset " + videoPresets.get(selected).getPresetName() + "?").showAndWait();
        if(buttonChoice.isPresent()) {
            if(buttonChoice.get() == ButtonType.YES) {
                if (!configManager.deletePreset(videoPresets.get(selected).getPresetName())) {
                    AlertUtils.simpleClose("Error", "Could not delete preset").show();
                } else {
                    videoPresets.remove(selected);
                    updatePresetList();
                }
            } //else if ButtonType.NO or closed [X] do nothing
        }
    }

    public void onTipsClicked(ActionEvent actionEvent) {
        String messageContent = "First warnings: \n- Do not start a line in the description box with a underscore (\"_\") as " +
                "that preset will not be loadable again!" +
                "\n- Tags must be separated with a comma followed by a space, forgetting the space will make the " +
                "comma a part of the tag.\nOk, tips!" +
                "\n- In the video title field you can write \"$(ep)\" to tell the program where to insert the episode " +
                "number/name defined  when adding videos for upload." +
                "\n- In the description field you can add \"$(playlist)\" to insert a link to the playlist the " +
                "video will be added to then uploaded.";
        AlertUtils.simpleClose("Tips", messageContent).showAndWait();
        actionEvent.consume();
    }

    public void onPresetAddNew(ActionEvent actionEvent) {

    }

    private void updatePresetList() {
        ArrayList<GridPane> presetPanes = new ArrayList<>();
        for (VideoPreset videoPreset : videoPresets) {
            presetPanes.add(videoPreset.getPresetPane());
        }
        listPresets.setItems(FXCollections.observableArrayList(presetPanes));
    }

    private void doFileChooser(int videoPresetsIndex) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose a thumbnail");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", THUMBNAIL_FILE_FORMAT));
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Image Files", THUMBNAIL_FILE_FORMAT));
        Stage fileChooserStage = new Stage();
        File thumbnail = fileChooser.showOpenDialog(fileChooserStage);
        if (thumbnail != null) {
            if(thumbnail.length() > 2 *1024 * 1024) { // max allowed size is 2MB
                AlertUtils.simpleClose("Warning", "Image to large, YouTube do not allow thumbnails larger then 2 MB." +
                        "\n the chosen file is " + BigDecimal.valueOf((double) thumbnail.length() /(1024 * 1024)).setScale(3, BigDecimal.ROUND_HALF_UP) + "MB").show();
                return;
            }
            try {
                videoPresets.get(videoPresetsIndex).setThumbNailFile(thumbnail);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
