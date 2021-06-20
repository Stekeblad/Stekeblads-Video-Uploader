package io.github.stekeblad.videouploader.windowControllers;

import io.github.stekeblad.videouploader.ListControllers.PlaylistItemController;
import io.github.stekeblad.videouploader.jfxExtension.IWindowController;
import io.github.stekeblad.videouploader.managers.PlaylistManager;
import io.github.stekeblad.videouploader.managers.SettingsManager;
import io.github.stekeblad.videouploader.utils.AlertUtils;
import io.github.stekeblad.videouploader.utils.background.OpenInBrowser;
import io.github.stekeblad.videouploader.utils.translation.TranslationBundles;
import io.github.stekeblad.videouploader.utils.translation.Translations;
import io.github.stekeblad.videouploader.utils.translation.TranslationsManager;
import io.github.stekeblad.videouploader.youtube.VisibilityStatus;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.stream.Collectors;

public class ManagePlaylistsWindowController implements IWindowController {
    public Button btn_refreshPlaylists;
    public Button btn_addNewPlaylist;
    public TextField txt_newPlaylistName;
    public ListView<PlaylistItemController> list_playlists;
    public ChoiceBox<String> choice_privacyStatus;
    public GridPane window;
    public ToolBar toolbar;
    public Label label_description;

    private final SettingsManager settingsManager = SettingsManager.getSettingsManager();
    private final PlaylistManager playlistManager = PlaylistManager.getPlaylistManager();
    private Translations transPlaylistWindow;
    private Translations transBasic;
    private ObservableList<PlaylistItemController> playlistItems;

    /**
     * Initialize a few things when the window is opened, used instead of initialize as that one does not have access to the scene
     */
    public void myInit() {
        // Set the default exception handler, hopefully it can catch some of the exceptions that is not already caught
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> AlertUtils.unhandledExceptionDialog(exception));

        // Load Translations
        transBasic = TranslationsManager.getTranslation(TranslationBundles.BASE);
        transPlaylistWindow = TranslationsManager.getTranslation(TranslationBundles.WINDOW_PLAYLIST);
        transPlaylistWindow.autoTranslate(window);

        // Insert the stored playlists into the list
        playlistItems = FXCollections.observableArrayList(
                playlistManager.getAllPlaylists().stream()
                        .map(PlaylistItemController::new)
                        .collect(Collectors.toList()))
                .sorted();
        list_playlists.setItems(playlistItems);

        // cant autoTranslate Nodes in Toolbar (bug)
        txt_newPlaylistName.setPromptText(transPlaylistWindow.getString("txt_newPlaylistName_pt"));
        choice_privacyStatus.setTooltip(new Tooltip(transPlaylistWindow.getString("choice_privacyStatus_tt")));
        btn_addNewPlaylist.setText(transPlaylistWindow.getString("btn_addNewPlaylist"));

        // Set choices in playlist privacy choiceBox
        ArrayList<VisibilityStatus> statuses = new ArrayList<>(EnumSet.allOf(VisibilityStatus.class));
        ArrayList<String> visibilityStrings = new ArrayList<>();
        for (VisibilityStatus status : statuses) {
            visibilityStrings.add(status.getStatusName());
        }
        choice_privacyStatus.setItems(FXCollections.observableArrayList(visibilityStrings));
        choice_privacyStatus.getSelectionModel().select(VisibilityStatus.PUBLIC.getStatusName());

        // Set so pressing enter in txt_newPlaylistName triggers onAddNewPlaylistClicked
        txt_newPlaylistName.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                onAddNewPlaylistClicked(new ActionEvent());
                event.consume();
            } // On Java 8, function key events is not passed on by TextFields, lets add it because handler already exists (Bug fixed in Java 9)
            if (event.getCode() == KeyCode.F1) {
                OpenInBrowser.openInBrowser("https://github.com/Stekeblad/Stekeblads-Video-Uploader/wiki/Manage-Playlists");
                event.consume();
            }
        });

        // Set so pressing F1 opens the wiki page for this window
        window.getScene().setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.F1) {
                OpenInBrowser.openInBrowser("https://github.com/Stekeblad/Stekeblads-Video-Uploader/wiki/Manage-Playlists");
                event.consume();
            }
        });
    }

    /**
     * Executed when the window's close button is triggered
     */
    public boolean onWindowClose() {
        // TODO: Test this, is visibility updated automatically by the observing feature?
//        ObservableList<CheckBox> listItems = list_playlists.getItems();
//        if (listItems.size() > 0) {
//            for (CheckBox listItem : listItems) {
//                playlistManager.setVisible(listItem.getText(), listItem.isSelected());
//            }
//            playlistManager.saveCache();
//        }
        return true;
    }

    /**
     * Downloads a list of all playlists on the user's channel and updates the list on screen
     * @param actionEvent the button click event
     */
    public void onRefreshPlaylistsClicked(ActionEvent actionEvent) {
        if (settingsManager.getNeverAuthed()) {
            ButtonType userChoice = AlertUtils.yesNo(transBasic.getString("auth_short"),
                    transBasic.getString("auth_full"), ButtonType.NO);

            if (userChoice == ButtonType.NO) { // or closed [X]
                actionEvent.consume();
                return;
            }
        }
        // Auth done or user is ready to allow it
        // Do not allow the button to be clicked again until the window is closed and reopened
        btn_refreshPlaylists.setDisable(true);
        btn_refreshPlaylists.setText(transBasic.getString("downloading"));

        // Send the request in the background
        Task<Void> backgroundTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                playlistManager.updateFromYouTube();
                Platform.runLater(() -> btn_refreshPlaylists.setText(transPlaylistWindow.getString("btn_refreshPlaylists")));
                return null;
            }
        };

        Thread backgroundThread = new Thread(backgroundTask);
        // Define a handler for exceptions
        backgroundThread.setUncaughtExceptionHandler((t, e) -> Platform.runLater(() -> {
            AlertUtils.simpleClose(transBasic.getString("error"),
                    transPlaylistWindow.getString("diag_downloadFailed")).showAndWait();
            e.printStackTrace();
            btn_refreshPlaylists.setDisable(false);
        }));

        // Start downloading playlists in the background and return
        backgroundThread.start();
        actionEvent.consume();
    }

    /**
     * Creates a new playlist on the user's channel using the content of txt_newPlaylistName_pt and choice_privacyStatus
     * @param actionEvent the button click event
     */
    public void onAddNewPlaylistClicked(ActionEvent actionEvent) {
        if (txt_newPlaylistName.getText().isEmpty()) {
            AlertUtils.simpleClose(transPlaylistWindow.getString("diag_noPlaylistName_short"),
                    transPlaylistWindow.getString("diag_noPlaylistName_full")).show();
            return;
        }
        if (settingsManager.getNeverAuthed()) {
            ButtonType userChoice = AlertUtils.yesNo(transBasic.getString("auth_short"),
                    transBasic.getString("auth_full"), ButtonType.NO);
            if (userChoice == ButtonType.NO) { // or closed [X]
                actionEvent.consume();
                return;
            }
        }

        // Auth OK, add the playlist
        btn_addNewPlaylist.setDisable(true);
        btn_addNewPlaylist.setText(transPlaylistWindow.getString("creating"));
        final String listName = txt_newPlaylistName.getText();
        final String privacyLevel = choice_privacyStatus.getSelectionModel().getSelectedItem();

        // Perform the request in a background thread
        Task<Void> backgroundTask = new Task<>() {
            @Override
            protected Void call() {
                playlistManager.createPlaylist(listName, privacyLevel);
                // TODO: The million tests, updating and creating playlists
                //CheckBox cb = new CheckBox(localPlaylist.getName());
                //cb.setSelected(true);
                Platform.runLater(() -> {
                    //list_playlists.getItems().add(cb);
                    txt_newPlaylistName.setText(""); // visually indicate its done by clearing the new playlist name textField
                    btn_addNewPlaylist.setDisable(false);
                    btn_addNewPlaylist.setText(transPlaylistWindow.getString("btn_addNewPlaylist"));
                });
                return null;
            }
        };
        Thread backgroundThread = new Thread(backgroundTask);
        // Exception handler
        backgroundThread.setUncaughtExceptionHandler((t, e) -> Platform.runLater(() -> {
            AlertUtils.simpleClose(transBasic.getString("error"),
                    transPlaylistWindow.getString("diag_creatingFailed")).showAndWait();
            e.printStackTrace();
        }));

        // Start the background thread and return
        backgroundThread.start();
        actionEvent.consume();
    }
}
