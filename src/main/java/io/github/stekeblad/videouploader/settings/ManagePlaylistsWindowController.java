package io.github.stekeblad.videouploader.settings;

import io.github.stekeblad.videouploader.utils.AlertUtils;
import io.github.stekeblad.videouploader.utils.ConfigManager;
import io.github.stekeblad.videouploader.youtube.LocalPlaylist;
import io.github.stekeblad.videouploader.youtube.utils.PlaylistUtils;
import io.github.stekeblad.videouploader.youtube.utils.VisibilityStatus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.WindowEvent;

import java.net.URL;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 *
 */
public class ManagePlaylistsWindowController implements Initializable {
    public Button btn_refreshPlaylists;
    public Button btn_addNewPlaylist;
    public TextField txt_newPlaylistName;
    public ListView<CheckBox> list_playlists;
    public ChoiceBox<String> choice_privacyStatus;

    private ConfigManager configManager = ConfigManager.INSTANCE;
    private PlaylistUtils playlistUtils = PlaylistUtils.INSTANCE;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Insert the stored playlists into the list
        updatePlaylistList();

        // Set choices in playlist privacy choiceBox
        ArrayList<VisibilityStatus> statuses = new ArrayList<>(EnumSet.allOf(VisibilityStatus.class));
        ArrayList<String> visibilityStrings = new ArrayList<>();
        for (VisibilityStatus status : statuses) {
            visibilityStrings.add(status.getStatusName());
        }
        choice_privacyStatus.setItems(FXCollections.observableArrayList(visibilityStrings));
        choice_privacyStatus.setTooltip(new Tooltip("Yes, playlists can be public, unlisted or private"));

        btn_refreshPlaylists.setTooltip(new Tooltip("Downloads a list of all your playlists from YouTube"));

    }

    public void onWindowClose(WindowEvent windowEvent) {
        ObservableList<CheckBox> listItems = list_playlists.getItems();
        for (CheckBox listItem : listItems) {
            playlistUtils.setVisible(listItem.getText(), listItem.isSelected());
        }
    }

    public void onRefreshPlaylists(ActionEvent actionEvent) {
        if (configManager.getNeverAuthed()) {
            Optional<ButtonType> buttonChoice = AlertUtils.yesNo("Authentication Required",
                    "To download your playlists you must grant this application permission to access your Youtube channel. " +
                    "Do you want to allow \"Stekeblads Video Uploader\" to access Your channel?" +
                    "\n\nPermission overview: \"YOUTUBE_UPLOAD\" for allowing the program to upload videos for you" +
                    "\n\"YOUTUBE\" for basic account access, adding videos to playlists and setting thumbnails" +
                    "\n\nPress yes to open your browser for authentication or no to cancel")
                    .showAndWait();
            if (buttonChoice.isPresent()) {
                if (buttonChoice.get() == ButtonType.YES) {
                    configManager.setNeverAuthed(false);
                    configManager.saveSettings();
                } else { // ButtonType.NO or closed [X]
                    AlertUtils.simpleClose("Permission not Granted", "Permission to access your YouTube was denied, playlists will not be updated.").show();
                    actionEvent.consume();
                    return;
                }
            }
        }
        // Auth done or user is ready to allow it
        // Do not allow the button to be clicked again until the window is closed and reopened
        btn_refreshPlaylists.setDisable(true);
        playlistUtils.refreshPlaylist();
        updatePlaylistList();
        actionEvent.consume();
    }

    public void onAddNewPlaylist(ActionEvent actionEvent) {
        if(txt_newPlaylistName.getText().isEmpty()) {
            AlertUtils.simpleClose("Missing playlist name", "You need to specify a name to create a new playlist").show();
            return;
        }
        if (configManager.getNeverAuthed()) {
            Optional<ButtonType> buttonChoice = AlertUtils.yesNo("Authentication Required",
                    "To create a new playlist you must grant this application permission to access your Youtube channel. " +
                            "Do you want to allow \"Stekeblads Video Uploader\" to access Your channel?" +
                            "\n\nPermission overview: \"YOUTUBE_UPLOAD\" for allowing the program to upload videos for you" +
                            "\n\"YOUTUBE\" for basic account access, adding videos to playlists and setting thumbnails" +
                            "\n\nPress yes to open your browser for authentication or no to cancel")
                    .showAndWait();
            if (buttonChoice.isPresent()) {
                if (buttonChoice.get() == ButtonType.YES) {
                    configManager.setNeverAuthed(false);
                    configManager.saveSettings();
                } else { // ButtonType.NO or closed [X]
                    AlertUtils.simpleClose("Permission not Granted",
                            "Permission to access your YouTube was denied, new playlist can not be created.").show();
                    actionEvent.consume();
                    return;
                }
            }
        }

        // Auth OK
        LocalPlaylist localPlaylist = playlistUtils.addPlaylist(
                txt_newPlaylistName.getText(), choice_privacyStatus.getSelectionModel().getSelectedItem());
        if(localPlaylist == null) {
            return;
        }
        CheckBox cb = new CheckBox(localPlaylist.getName());
        cb.setSelected(true);
        list_playlists.getItems().add(cb);
        txt_newPlaylistName.setText(""); // visually indicate its done by clearing the new playlist name textField

        actionEvent.consume();
    }

    private void updatePlaylistList() {
        ArrayList<CheckBox> playlistCheckBoxes = new ArrayList<>();
        ArrayList<LocalPlaylist> playlists = playlistUtils.getAllPlaylists();
        for (LocalPlaylist playlist : playlists) {
            CheckBox cb = new CheckBox(playlist.getName());
            cb.setSelected(playlist.isVisible());
            playlistCheckBoxes.add(cb);
        }
        list_playlists.setItems(FXCollections.observableArrayList(playlistCheckBoxes));
    }
}
