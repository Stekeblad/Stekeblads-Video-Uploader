package io.github.stekeblad.youtubeuploader.settings;

import io.github.stekeblad.youtubeuploader.utils.AlertUtils;
import io.github.stekeblad.youtubeuploader.utils.ConfigManager;
import io.github.stekeblad.youtubeuploader.utils.PickFile;
import io.github.stekeblad.youtubeuploader.youtube.PlaylistUtils;
import io.github.stekeblad.youtubeuploader.youtube.VideoPreset;
import io.github.stekeblad.youtubeuploader.youtube.constants.Categories;
import io.github.stekeblad.youtubeuploader.youtube.constants.VisibilityStatus;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.ResourceBundle;

import static io.github.stekeblad.youtubeuploader.utils.Constants.*;
import static io.github.stekeblad.youtubeuploader.youtube.VideoInformationBase.NODE_ID_PLAYLIST;
import static io.github.stekeblad.youtubeuploader.youtube.VideoInformationBase.NODE_ID_THUMBNAIL;


public class PresetsWindowController implements Initializable {

    public AnchorPane SettingsWindow;
    public ListView<GridPane> listPresets;
    public Button btn_tips;
    public Button addNewPreset;
    public Button btn_refreshPlaylists;
    public TextField txt_nameNewPreset;

    private ArrayList<VideoPreset> videoPresets;
    private ConfigManager configManager;
    private PlaylistUtils playlistUtils;
    private int presetCounter = 0;
    private HashMap<String, VideoPreset> presetBackups;
    
    private static final String PRESET_PANE_ID_PREFIX = "preset-";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configManager = ConfigManager.INSTANCE;
        playlistUtils = PlaylistUtils.INSTANCE;
        presetBackups = new HashMap<>();

        videoPresets = new ArrayList<>();
        ArrayList<String> savedPresetNames = configManager.getPresetNames();
        if (savedPresetNames != null) {
            for (String presetName : savedPresetNames) {
                try {
                    VideoPreset videoPreset = new VideoPreset(configManager.getPresetString(presetName), PRESET_PANE_ID_PREFIX + presetCounter);
                    Button editButton = new Button("Edit");
                    editButton.setId(PRESET_PANE_ID_PREFIX + presetCounter + BUTTON_EDIT);
                    editButton.setOnMouseClicked(event -> onPresetEdit(editButton.getId()));
                    Button deleteButton = new Button("Delete");
                    deleteButton.setId(PRESET_PANE_ID_PREFIX + presetCounter + BUTTON_DELETE);
                    deleteButton.setOnMouseClicked(event -> onPresetDelete(deleteButton.getId()));
                    videoPreset.setButton1(editButton);
                    videoPreset.setButton2(deleteButton);
                    videoPresets.add(videoPreset);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("Trying to load a preset that does not exist or missing read permission: " + presetName);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Bad format of preset or is another type of preset then the one trying to be created: " + presetName);
                }
                presetCounter++;
            }
        }

        updatePresetList();
    }

    public void onPresetAddNew(ActionEvent actionEvent) {
        if (txt_nameNewPreset.getText().equals("")) {
            AlertUtils.simpleClose("name missing", "Enter a name for the new preset!").show();
            return;
        }
        //if (getPresetIndexByName(txt_nameNewPreset.getText(), -1) > -1) {
        //    AlertUtils.simpleClose("Preset already exists", "Preset names must be unique, there is already a preset with that name!").show();
        //    return;
        //}
        VideoPreset newPreset = new VideoPreset("", "", VisibilityStatus.PUBLIC, null,
                null, Categories.SPEL, false, null, PRESET_PANE_ID_PREFIX + presetCounter, txt_nameNewPreset.getText());
        videoPresets.add(newPreset);
        onPresetEdit(PRESET_PANE_ID_PREFIX + presetCounter + "_fakeButton");
        updatePresetList();
        listPresets.scrollTo(listPresets.getItems().size() -1);
        txt_nameNewPreset.setText("");
        presetCounter++;
        updatePresetList();
        actionEvent.consume();
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

    public void onRefreshPlaylists(ActionEvent actionEvent) {
        if (configManager.getNeverAuthed()) {
            Optional<ButtonType> buttonChoice = AlertUtils.yesNo("Authentication Required", "To download your playlists you must grant this application permission to access your Youtube channel. " +
                    "Do you want to allow \"Stekeblads Youtube Uploader\" to access Your channel?" +
                    "\n\nPermission overview: \"YOUTUBE_UPLOAD\" for allowing the program to upload videos for you" +
                    "\n\"YOUTUBE\" for basic account access, adding videos to playlists and setting thumbnails" +
                    "\n\nPress yes to open your browser for authentication or no to cancel")
                    .showAndWait();
            if (buttonChoice.isPresent()) {
                if (buttonChoice.get() == ButtonType.YES) {
                    configManager.setNeverAuthed(false);
                    configManager.saveSettings();
                    btn_refreshPlaylists.setDisable(true);
                    playlistUtils.refreshPlaylist();
                    AlertUtils.simpleClose("Playlists updated", "For the updated playlist list to show up " +
                            "you may need to save any preset you are currently editing and press 'edit' again").showAndWait();
                } else { // ButtonType.NO oc closed [X]
                    AlertUtils.simpleClose("Permission not Granted", "Permission to access your YouTube was denied, playlists will not be updated.").show();
                }
            }
        } else { // has authenticated
            btn_refreshPlaylists.setDisable(true);
            playlistUtils.refreshPlaylist();
            AlertUtils.simpleClose("Playlists updated", "For the updated playlist list to show up you " +
                    "may need to save any preset you are currently editing and press 'edit' again").showAndWait();
        }
        actionEvent.consume();
    }

    private void updatePresetList() {
        ArrayList<GridPane> presetPanes = new ArrayList<>();
        for (VideoPreset videoPreset : videoPresets) {
            presetPanes.add(videoPreset.getPresetPane());
        }
        listPresets.setItems(FXCollections.observableArrayList(presetPanes));
    }

    /**
     * Returns the index in videoPresets that has a preset named nameToTest or -1 if no preset has that name
     * @param nameToTest preset name to test for
     * @return the index of where the preset named nameToTest inside videoPresets or -1 if it does not exist a preset with that name
     */
    private int getPresetIndexByName(String nameToTest) {
        int presetIndex = -1;
        for (int i = 0; i < videoPresets.size(); i++) {
            if (videoPresets.get(i).getPresetPane().getId().equals(nameToTest)) {
                presetIndex = i;
                break;
            }
        }
        return presetIndex;
    }

    private void onPresetEdit(String callerId) {
        String parentId = callerId.substring(0, callerId.indexOf('_'));
        int selected = getPresetIndexByName(parentId);
        if(selected == -1) {
            System.err.println("Non-existing edit button was pressed!!!");
            return;
        }
        presetBackups.put(videoPresets.get(selected).getPaneId(), videoPresets.get(selected).copy(null));
        videoPresets.get(selected).setEditable(true);
        videoPresets.get(selected).getPane().lookup("#" + parentId + NODE_ID_THUMBNAIL).setOnMouseClicked(event -> {
            File pickedThumbnail = PickFile.pickThumbnail();
            if(pickedThumbnail != null) {
                try {
                    videoPresets.get(selected).setThumbNailFile(pickedThumbnail);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        videoPresets.get(selected).getPane().lookup("#" + parentId + NODE_ID_PLAYLIST).setOnMouseClicked(event->
                ((ChoiceBox<String>) videoPresets.get(selected).getPane().lookup("#" + parentId + NODE_ID_PLAYLIST)).setItems(
                        FXCollections.observableArrayList(playlistUtils.getUserPlaylistNames())));

        // Change buttons from "edit" and "delete" to "save" and "cancel"
        Button saveButton = new Button("Save");
        saveButton.setId(parentId + BUTTON_SAVE);
        saveButton.setOnMouseClicked(event-> onPresetSave(saveButton.getId()));
        Button cancelButton = new Button("Cancel");
        cancelButton.setId(parentId + BUTTON_CANCEL);
        cancelButton.setOnMouseClicked(event-> onPresetCancelEdit(cancelButton.getId()));

        videoPresets.get(selected).setButton1(cancelButton);
        videoPresets.get(selected).setButton2(saveButton);
    }

    private void onPresetSave(String callerId) {
        String parentId = callerId.substring(0, callerId.indexOf('_'));
        // locate this preset
        int selected = getPresetIndexByName(parentId);
        if(selected == -1) {
            System.err.println("Can't find witch preset to save");
            return;
        }
        // make sure preset name is not empty
        if(videoPresets.get(selected).getPresetName().equals("")) {
            AlertUtils.simpleClose("Invalid preset name", "Preset names can not be empty").show();
            return;
        }
        // if there is a backup it needs to be deleted
        if (presetBackups.containsKey(videoPresets.get(selected).getPaneId())) {
            // if preset name changed then the preset save file needs to be changed
            String oldPresetName = presetBackups.get(videoPresets.get(selected).getPaneId()).getPresetName();
            if (!videoPresets.get(selected).getPresetName().equals(oldPresetName)) {
                configManager.deletePreset(oldPresetName);
            }
            presetBackups.remove(videoPresets.get(selected).getPaneId());
        }
        configManager.savePreset(videoPresets.get(selected).getPresetName(), videoPresets.get(selected).toString());

        //change back buttons
        Button editButton = new Button("Edit");
        editButton.setId(parentId + BUTTON_EDIT);
        editButton.setOnMouseClicked(event -> onPresetEdit(editButton.getId()));
        Button deleteButton = new Button("Delete");
        deleteButton.setId(parentId + BUTTON_DELETE);
        deleteButton.setOnMouseClicked(event -> onPresetDelete(deleteButton.getId()));

        videoPresets.get(selected).setButton1(editButton);
        videoPresets.get(selected).setButton2(deleteButton);
    }

    private void onPresetCancelEdit(String callerId) {
        String parentId = callerId.substring(0, callerId.indexOf('_'));
        //reload preset from disc
        int selected = getPresetIndexByName(parentId);
        if (selected == -1) {
            System.err.println("Non-existing cancelEdit button was pressed!!!");
            return;
        }

        if (! presetBackups.containsKey(videoPresets.get(selected).getPaneId())) {
            // assume preset is a newly added not saved preset, delete it directly
            videoPresets.remove(selected);
            updatePresetList();
            return;
        } else {
            // restore backup
            videoPresets.set(selected, presetBackups.get(videoPresets.get(selected).getPaneId()));
            presetBackups.remove(videoPresets.get(selected).getPaneId());
        }
        updatePresetList();


        //change buttons
        Button editButton = new Button("Edit");
        editButton.setId(parentId + BUTTON_EDIT);
        editButton.setOnMouseClicked(event -> onPresetEdit(editButton.getId()));
        Button deleteButton = new Button("Delete");
        deleteButton.setId(parentId + BUTTON_DELETE);
        deleteButton.setOnMouseClicked(event -> onPresetDelete(deleteButton.getId()));

        videoPresets.get(selected).setButton1(editButton);
        videoPresets.get(selected).setButton2(deleteButton);
    }

    private void onPresetDelete(String callerId) {
        String parentId = callerId.substring(0, callerId.indexOf('_'));
        int selected = getPresetIndexByName(parentId);
        if(selected == -1) {
            System.err.println("Non-existing delete button was pressed!!!");
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
}
