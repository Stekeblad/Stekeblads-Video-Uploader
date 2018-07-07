package io.github.stekeblad.videouploader.main;

import io.github.stekeblad.videouploader.utils.*;
import io.github.stekeblad.videouploader.utils.background.OpenInBrowser;
import io.github.stekeblad.videouploader.windowControllers.PresetsWindowController;
import io.github.stekeblad.videouploader.windowControllers.SettingsWindowController;
import io.github.stekeblad.videouploader.youtube.Uploader;
import io.github.stekeblad.videouploader.youtube.VideoPreset;
import io.github.stekeblad.videouploader.youtube.VideoUpload;
import io.github.stekeblad.videouploader.youtube.utils.CategoryUtils;
import io.github.stekeblad.videouploader.youtube.utils.PlaylistUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import static io.github.stekeblad.videouploader.utils.Constants.*;
import static javafx.scene.control.ProgressIndicator.INDETERMINATE_PROGRESS;

public class mainWindowController {
    public AnchorPane mainWindowPane;
    public ToolBar toolbar;
    public ListView<GridPane> listView;
    public ListView<String> chosen_files;
    public ChoiceBox<String> choice_presets;
    public TextField txt_autoNum;
    public Button btn_presets;
    public Button btn_settings;
    public Button btn_pickFile;
    public Button btn_applyPreset;
    public Button btn_removeFinished;
    public Button btn_startAll;
    public Button btn_abortAll;
    public Button btn_abortAndClear;
    public Label label_selectPreset;
    public Label label_numbering;

    private ConfigManager configManager;
    private PlaylistUtils playlistUtils;
    private CategoryUtils categoryUtils;
    private int uploadPaneCounter = 0;
    private List<VideoUpload> uploadQueueVideos;
    private List<File> videosToAdd;
    private HashMap<String, VideoUpload> editBackups;
    private Uploader uploader;
    private static final String UPLOAD_PANE_ID_PREFIX = "upload-";
    private boolean bypassAbortWarning = false;

    private Translations transMainWin;
    private Translations transBasic;
    private Translations transUpload;

    /**
     * Initialize things when the window is opened, used instead of initialize as that one does not have access to the scene
     */
    public void myInit() {
        // Load Translations
        transMainWin = TranslationsManager.getTranslation("mainWindow");
        transMainWin.autoTranslate(mainWindowPane);
        // Bugged:
        // System.out.println(toolbar.getChildrenUnmodifiable());
        btn_presets.setText(transMainWin.getString("btn_presets"));
        btn_settings.setText(transMainWin.getString("btn_settings"));

        transBasic = TranslationsManager.getTranslation("baseStrings");
        transUpload = TranslationsManager.getTranslation("presetsUploads");

        // Load custom CSS (for improved readability of disabled ChoiceBoxes)
        URL css_path = mainWindowController.class.getClassLoader().getResource("css/disabled.css");
        if (css_path != null) {
            mainWindowPane.getScene().getStylesheets().add(css_path.toString());
        }

        uploader = new Uploader();

        uploadPaneCounter = 0;
        uploadQueueVideos = new ArrayList<>();
        editBackups = new HashMap<>();
        configManager = ConfigManager.INSTANCE;
        // configManager.configManager(); Done in Main.java
        playlistUtils = PlaylistUtils.INSTANCE;
        playlistUtils.loadCache();
        categoryUtils = CategoryUtils.INSTANCE;
        categoryUtils.loadCategories();
        choice_presets.setItems(FXCollections.observableArrayList(configManager.getPresetNames()));

        // Only allow numbers in autoNum textField
        txt_autoNum.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txt_autoNum.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        uploader.setUploadFinishedCallback(s -> Platform.runLater(() -> onUploadFinished(s)));
        uploader.setUploadErredCallback((videoUpload, throwable) -> Platform.runLater(() -> onUploadErred(videoUpload, throwable)));

        // If any uploads was saved when the program was closed last time
        if(configManager.hasWaitingUploads()) {
            ArrayList<String> waitingUploads = configManager.getWaitingUploads();
            if (waitingUploads != null) {
                for(String waitingUpload : waitingUploads) {
                    try {
                        VideoUpload loadedUpload = new VideoUpload(waitingUpload, String.valueOf(uploadPaneCounter++));
                        Button editButton = new Button(transBasic.getString("edit"));
                        editButton.setId(loadedUpload.getPaneId() + BUTTON_EDIT);
                        editButton.setOnMouseClicked(event -> onEdit(editButton.getId()));
                        Button deleteButton = new Button(transBasic.getString("delete"));
                        deleteButton.setId(loadedUpload.getPaneId() + BUTTON_DELETE);
                        deleteButton.setOnMouseClicked(event -> onDelete(deleteButton.getId()));
                        Button startUploadButton = new Button(transBasic.getString("startUpload"));
                        startUploadButton.setId(loadedUpload.getPaneId() + BUTTON_START_UPLOAD);
                        startUploadButton.setOnMouseClicked(event -> onStartUpload(startUploadButton.getId()));

                        loadedUpload.setButton1(editButton);
                        loadedUpload.setButton2(deleteButton);
                        loadedUpload.setButton3(startUploadButton);

                        // Auto resize width and translation
                        loadedUpload.getPane().prefWidthProperty().bind(listView.widthProperty());
                        transUpload.autoTranslate(loadedUpload.getPane(), loadedUpload.getPaneId());

                        uploadQueueVideos.add(loadedUpload);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            updateUploadList();
        }
        // Set so pressing F1 opens the wiki page for this window
        Scene scene = mainWindowPane.getScene();
        scene.setOnKeyPressed((event) -> {
            if (event.getCode() == KeyCode.F1) {
                OpenInBrowser.openInBrowser("https://github.com/Stekeblad/Stekeblads-Video-Uploader/wiki/Main-Window");
                event.consume();
            }
        });
    }


    /**
     * This method is called when this window's close button is clicked.
     * If one or more uploads is queued a confirmation dialog will be showed to ask the user if they want to
     * abort them or not.
     * @return true if the window should to be closed, false if not
     */
    public boolean onWindowClose() {
        // Check if uploads is in progress, if not then directly return true
        if (! uploader.getIsActive()) {
            uploader.kill(); // just because it does not do anything it started and must be stopped
            return true;
        }
        String op1 = transMainWin.getString("diag_closeWarn_op1");
        String op2 = transMainWin.getString("diag_closeWarn_op2");
        String op3 = transMainWin.getString("diag_closeWarn_op3");
        String choice = AlertUtils.threeButtons(transMainWin.getString("diag_closeWarn_short"),
                transMainWin.getString("diag_closeWarn_full"), op1, op2, op3);
        if (choice == null) {
            return false;
        }
        if (choice.equals(op1)) {
            return false;
        } else if (choice.equals(op2)) {
            uploader.kill();
            return true;
        } else if (choice.equals(op3)) {

            Set<String> tasks = uploader.kill();
            tasks.forEach(s -> {
                int index = getUploadIndexByName(s);
                if (index != -1) { // If a task does not have a index it has been removed and is not interesting, or bugged with a bad id, skip them
                    configManager.saveWaitingUpload(uploadQueueVideos.get(index).toString(), String.valueOf(index));
                }
            });
            return true;
        }
        return false;
    }

    /**
     * Called when the pick files button is pressed.
     * Opens a file chooser and sets the list of selected files to the left of the button
     * @param actionEvent the click event
     */
    public void onPickFileClicked(ActionEvent actionEvent) {
        videosToAdd = FileUtils.pickVideos();
        ArrayList<String> filenames = new ArrayList<>();
        if(videosToAdd != null) {
            for (File file : videosToAdd) {
                filenames.add(file.getName());
            }
        }
        chosen_files.setItems(FXCollections.observableArrayList(filenames));
        actionEvent.consume();
    }

    /**
     * Called when the apply preset button is clicked.
     * Takes the files in the selected files list and applies the selected preset or uses a blank preset if none is selected.
     * Populates the uploads list.
     * @param actionEvent the click event
     */
    public void onApplyPresetClicked(ActionEvent actionEvent) {
        if(videosToAdd == null || videosToAdd.size() == 0 ) {
            AlertUtils.simpleClose(transMainWin.getString("diag_noFiles_short"),
                    transMainWin.getString("diag_noFiles_full")).show();
            return;
        }
        // Check what preset / if a preset is selected
        if(choice_presets.getSelectionModel().getSelectedIndex() == -1) {
            // No preset, add videos to upload list with file name as title and blank/default values on the rest
            for(File videoFile : videosToAdd) {
                VideoUpload newUpload = new VideoUpload(videoFile.getName(), null, null,
                        null, null, null, false, null,
                        UPLOAD_PANE_ID_PREFIX + uploadPaneCounter, videoFile);

                // make the upload change its width together with the uploads list and the window
                newUpload.getPane().prefWidthProperty().bind(listView.widthProperty());
                // Translate the upload
                transUpload.autoTranslate(newUpload.getPane(), newUpload.getPaneId());
                uploadQueueVideos.add(newUpload);

                // Enables the upload to be edited because the lack of details.
                onEdit(UPLOAD_PANE_ID_PREFIX + uploadPaneCounter + "_fakeButton");
                // Change the cancel button to a delete button, the backed up state created by onEdit is not valid
                Button deleteButton = new Button(transBasic.getString("delete"));
                deleteButton.setId(UPLOAD_PANE_ID_PREFIX + uploadPaneCounter + BUTTON_DELETE);
                deleteButton.setOnMouseClicked(event -> onDelete(deleteButton.getId()));
                uploadQueueVideos.get(uploadQueueVideos.size() - 1).setButton1(deleteButton);

                uploadPaneCounter++;
            }
        } else { // preset selected
            VideoPreset chosenPreset;
            // Get the auto numbering and preset
            int autoNum;
            try {
                autoNum = Integer.valueOf(txt_autoNum.getText());
            } catch (NumberFormatException e) {
                autoNum = 1;
            }
            // Load details of the selected preset
            try {
                chosenPreset = new VideoPreset(configManager.getPresetString(
                        choice_presets.getSelectionModel().getSelectedItem()), "preset");
            } catch (Exception e) {
                AlertUtils.simpleClose("Preset error", "Cant read preset, the videos will not be added");
                return;
            }
            // Get playlist url if available
            String playlistUrl = playlistUtils.getPlaylistUrl(chosenPreset.getPlaylist());
            if (playlistUrl == null) {
                playlistUrl = "";
            }
            // Iterate over all selected video files
            for (File videoFile : videosToAdd) {
                // Apply automatic numbering on video name
                String name = chosenPreset.getVideoName().replace("$(ep)", String.valueOf(autoNum++));
                // Insert raw file name in title, exclude file extension
                if (name.contains("$(rawname)")) {
                    String rawFileName = videoFile.getName().substring(0, videoFile.getName().lastIndexOf("."));
                    name = name.replace("$(rawname)", rawFileName);
                }
                // Insert playlist URL in description
                String description = chosenPreset.getVideoDescription()
                        .replace("$(playlist)", playlistUrl);

                VideoUpload.Builder newUploadBuilder = new VideoUpload.Builder()
                        .setVideoName(name)
                        .setVideoDescription(description)
                        .setVisibility(chosenPreset.getVisibility())
                        .setVideoTags(chosenPreset.getVideoTags())
                        .setPlaylist(chosenPreset.getPlaylist())
                        .setCategory(chosenPreset.getCategory())
                        .setTellSubs(chosenPreset.isTellSubs())
                        .setPaneName(UPLOAD_PANE_ID_PREFIX + uploadPaneCounter)
                        .setVideoFile(videoFile);
                if (chosenPreset.getThumbNail() != null) {
                    newUploadBuilder.setThumbNailPath(chosenPreset.getThumbNail().getAbsolutePath());
                }
                 VideoUpload newUpload = newUploadBuilder.build();
                // make the upload change its width together with the uploads list and the window
                newUpload.getPane().prefWidthProperty().bind(listView.widthProperty());

                // Translate the upload
                transUpload.autoTranslate(newUpload.getPane(), newUpload.getPaneId());

                // Create the buttons
                Button editButton = new Button(transBasic.getString("edit"));
                editButton.setId(newUpload.getPaneId() + BUTTON_EDIT);
                editButton.setOnMouseClicked(event -> onEdit(editButton.getId()));
                Button deleteButton = new Button(transBasic.getString("delete"));
                deleteButton.setId(newUpload.getPaneId() + BUTTON_DELETE);
                deleteButton.setOnMouseClicked(event -> onDelete(deleteButton.getId()));
                Button startUploadButton = new Button(transBasic.getString("startUpload"));
                startUploadButton.setId(newUpload.getPaneId() + BUTTON_START_UPLOAD);
                startUploadButton.setOnMouseClicked(event -> onStartUpload(startUploadButton.getId()));

                newUpload.setButton1(editButton);
                newUpload.setButton2(deleteButton);
                newUpload.setButton3(startUploadButton);

                uploadQueueVideos.add(newUpload);
                uploadPaneCounter++;
            }
            // update autoNum textField
            txt_autoNum.setText(String.valueOf(autoNum));
        }
        // Removes the newly added uploads from the selected files list and update UI
        videosToAdd = null;
        chosen_files.setItems(FXCollections.observableArrayList(new ArrayList<>()));
        updateUploadList();
        actionEvent.consume();
    }

    /**
     * Called when the preset button is clicked.
     * Opens the preset window.
     * @param actionEvent the click event
     */
    public void onPresetsClicked(ActionEvent actionEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(mainWindowController.class.getClassLoader().getResource("fxml/PresetsWindow.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 725, 700);
            URL css_path = mainWindowController.class.getClassLoader().getResource("css/disabled.css");
            if (css_path != null) {
                scene.getStylesheets().add(css_path.toString());
            }
            Stage stage = new Stage();
            stage.setTitle(transBasic.getString("app_presetWindowTitle"));
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL); // Make it always above mainWindow
            PresetsWindowController controller = fxmlLoader.getController();
            stage.setOnCloseRequest(controller::onWindowClose);
            controller.myInit();
            stage.showAndWait();
        } catch (IOException e) {
            AlertUtils.exceptionDialog(transBasic.getString("error"), transBasic.getString("errOpenWindow"), e);
        }
        actionEvent.consume();
        // Update presets choice box in case presets was added or remove
        choice_presets.setItems(FXCollections.observableArrayList(configManager.getPresetNames()));
    }

    /**
     * Called when the settings button is clicked.
     * Opens the settings window.
     *
     * @param actionEvent the click event
     */
    public void onSettingsClicked(ActionEvent actionEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(mainWindowController.class.getClassLoader().getResource("fxml/SettingsWindow.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 600, 450);
            Stage stage = new Stage();
            stage.setTitle(transBasic.getString("app_settingsWindowTitle"));
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL); // Make it always above mainWindow
            SettingsWindowController controller = fxmlLoader.getController();
            stage.setOnCloseRequest(controller::onWindowClose);
            controller.myInit();
            stage.show();
        } catch (IOException e) {
            AlertUtils.exceptionDialog(transBasic.getString("error"), transBasic.getString("errOpenWindow"), e);
        }
        actionEvent.consume();
    }

    /**
     * Called when the start all ready uploads button is clicked.
     * If a upload is allowed to be started (the start upload button is visible) then that button is clicked by this method.
     * @param actionEvent the click event
     */
    public void onStartAllUploadsClicked(ActionEvent actionEvent) {
        // Check if the user has given the program permission to access the user's youtube account, if not then ask for it
        if(configManager.getNeverAuthed()) {
            Optional<ButtonType> buttonChoice = AlertUtils.yesNo(transBasic.getString("auth_short"),
                    transBasic.getString("auth_full")).showAndWait();
            if (buttonChoice.isPresent()) {
                if (buttonChoice.get() == ButtonType.YES) {
                    configManager.setNeverAuthed(false);
                    configManager.saveSettings();
                } else { // ButtonType.NO or closed [X]
                    return;
                }
            }
        }
        // Permission given, start uploads
        for (int i = 0; i < uploadQueueVideos.size(); i++) {
            if (uploadQueueVideos.get(i).getButton3Id() != null &&
                    uploadQueueVideos.get(i).getButton3Id().contains(BUTTON_START_UPLOAD)) {
                onStartUpload(uploadQueueVideos.get(i).getButton3Id());
            }
        }
        actionEvent.consume();
    }

    /**
     * Called when the remove all finished uploads button is clicked.
     * Clicks the hide button on all uploads that has that button visible
     * @param actionEvent the click event
     */
    public void onRemoveFinishedUploadsClicked(ActionEvent actionEvent) {
        for (int i = 0; i < uploadQueueVideos.size(); i++) {
            if (uploadQueueVideos.get(i).getButton2Id() != null &&
                    uploadQueueVideos.get(i).getButton2Id().contains(BUTTON_FINISHED_UPLOAD)) {
                uploadQueueVideos.remove(i);
                i--;
            }
        }
        updateUploadList();
        actionEvent.consume();
    }

    /**
     * Aborts all uploads that have been started
     * @param actionEvent the button click event
     */
    public void onAbortAllUploadsClicked(ActionEvent actionEvent) {
        // Show confirmation dialog
        if (!bypassAbortWarning) { // can be set from onAbortAndClearClicked
            Optional<ButtonType> buttonChoice = AlertUtils.yesNo(transMainWin.getString("diag_abortAll_short"),
                    transMainWin.getString("diag_abortAll_full")).showAndWait();
            if (buttonChoice.isPresent()) {
                if (buttonChoice.get() != ButtonType.YES) {
                    // ButtonType.NO or Closed with [X] button
                    return;
                }
            }
        }
        // Prevent the "Are you sure you want to abort X?" dialog for every upload
        bypassAbortWarning = true;
        // Abort the uploads in the reversed order of that they was most likely started in
        // to avoid that the program attempts to start a new upload that will also be aborted, and then the next one...
        for (int i = uploadQueueVideos.size() - 1; i >= 0; i--) {
            if(uploadQueueVideos.get(i).getButton2Id() != null &&
                    uploadQueueVideos.get(i).getButton2Id().contains(BUTTON_ABORT_UPLOAD)) {
                onAbort(uploadQueueVideos.get(i).getButton2Id());
            }
        }
        // Re-enable the individual confirmation on aborts
        bypassAbortWarning = false;
        actionEvent.consume();
    }

    /**
     * Aborts all uploads and removes all uploads from the list (aborted, finished and not started)
     *
     * @param actionEvent the button click event
     */
    public void onAbortAndClearClicked(ActionEvent actionEvent) {
        Optional<ButtonType> choice = AlertUtils.yesNo(transMainWin.getString("diag_abortAllClear_short"),
                transMainWin.getString("diag_abortAllClear_full")).showAndWait();
        if (choice.isPresent()) {
            if (choice.get() == ButtonType.YES) {
                bypassAbortWarning = true; // is set back to false by onAbortAllUploadsClicked
                onAbortAllUploadsClicked(new ActionEvent());
                uploadQueueVideos.clear();
                uploadPaneCounter = 0;
                updateUploadList();
            }
        }
        actionEvent.consume();
    }

    /**
     * Re-adds all elements to the uploadQueuePanes so the UI is up-to-date
     */
    private void updateUploadList() {
        List<GridPane> uploadQueuePanes = new ArrayList<>();
        for(VideoUpload vid : uploadQueueVideos) {
            uploadQueuePanes.add(vid.getPane());
        }
        listView.setItems(FXCollections.observableArrayList(uploadQueuePanes));
    }

    /**
     * Takes a node Id and checks if there is a upload with that id and if so returns its index inside uploadQueueVideos.
     * @param nameToTest a Node id
     * @return the index of a upload with that id inside uploadQueueVideos or -1 if it was not found in uploadQueueVideos.
     */
    private int getUploadIndexByName(String nameToTest) {
        int videoIndex = -1;
        for(int i = 0; i < uploadQueueVideos.size(); i++) {
            if(uploadQueueVideos.get(i).getPaneId().equals(nameToTest)) {
                videoIndex = i;
                break;
            }
        }
        return videoIndex;
    }

    /**
     * Called when the edit button for a upload is clicked
     * Enables editing for that upload, takes a backup of its state to be able to revert and changes the available buttons
     * @param callerId the id of the upload + button name
     */
    private void onEdit(String callerId) {
        String parentId = callerId.substring(0, callerId.indexOf('_'));
        int selected = getUploadIndexByName(parentId);
        if (selected == -1) {
            System.err.println("edit button belongs to a invalid or non-existing parent");
            return;
        }
        // Create a backup to be able to revert
        editBackups.put(uploadQueueVideos.get(selected).getPaneId(), uploadQueueVideos.get(selected).copy(null)); //null -> same id

        uploadQueueVideos.get(selected).setEditable(true);
        uploadQueueVideos.get(selected).setOnThumbnailClicked(event -> {
            File pickedThumbnail = FileUtils.pickThumbnail();
            if(pickedThumbnail != null) {
                try {
                    uploadQueueVideos.get(selected).setThumbNailFile(pickedThumbnail);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        uploadQueueVideos.get(selected).setOnPlaylistsClicked(event -> {
            if (playlistUtils.getPlaylistNames().size() == 0) {
                AlertUtils.simpleClose(transMainWin.getString("diag_noPlaylists_short"),
                        transMainWin.getString("diag_noPlaylists_full")).show();
            } else {
                uploadQueueVideos.get(selected).setPlaylists(playlistUtils.getVisiblePlaylistnames());
            }
        });
        uploadQueueVideos.get(selected).setOnCategoriesClicked(event ->
                uploadQueueVideos.get(selected).setCategories(categoryUtils.getCategoryNames())
        );


        // Set buttons
        Button saveButton = new Button(transBasic.getString("save"));
        saveButton.setId(parentId + BUTTON_SAVE);
        saveButton.setOnMouseClicked(event -> onSave(saveButton.getId()));
        Button cancelButton = new Button(transBasic.getString("cancel"));
        cancelButton.setId(parentId + BUTTON_CANCEL);
        cancelButton.setOnMouseClicked(event -> onCancel(cancelButton.getId()));
        //Button three is not used and I do not want the previous one to be visible
        Button ghostButton = new Button("");
        ghostButton.setVisible(false);

        uploadQueueVideos.get(selected).setButton1(cancelButton);
        uploadQueueVideos.get(selected).setButton2(saveButton);
        uploadQueueVideos.get(selected).setButton3(ghostButton);

        // Make sure visual change get to the UI
        updateUploadList();
    }

    /**
     * Called when the save button for a upload is clicked.
     * Saves all changes made to the upload, disables editing and changes the visible buttons.
     * @param callerId the id of the upload + button name
     */
    private void onSave(String callerId) {
        String parentId = callerId.substring(0, callerId.indexOf('_'));
        int selected = getUploadIndexByName(parentId);
        if (selected == -1) {
            System.err.println("save button belongs to a invalid or non-existing parent");
            return;
        }
        // Check fields, video name
        if(uploadQueueVideos.get(selected).getVideoName().equals("")) {
            AlertUtils.simpleClose(transMainWin.getString("diag_noVidTitle_short"),
                    transMainWin.getString("diag_noVidTitle_full")).show();
            return;
        }
        // Make sure a category is selected and the category name still match a stored category
        // (will not match stored if categories have been re-localized)
        if (uploadQueueVideos.get(selected).getCategory() == null &&
                !categoryUtils.getCategoryId(uploadQueueVideos.get(selected).getCategory()).equals("-1")) {
            AlertUtils.simpleClose(transBasic.getString("diag_invalidCategory_short"),
                    transBasic.getString("diag_invalidCategory_full")).show();
            return;
        }

        uploadQueueVideos.get(selected).setEditable(false);
        // Delete backup if there is one
        if(editBackups.containsKey(uploadQueueVideos.get(selected).getPaneId())) {
            editBackups.remove(uploadQueueVideos.get(selected).getPaneId());
        }

        // Change buttons
        Button editButton = new Button(transBasic.getString("edit"));
        editButton.setId(parentId + BUTTON_EDIT);
        editButton.setOnMouseClicked(event -> onEdit(editButton.getId()));
        Button deleteButton = new Button(transBasic.getString("delete"));
        deleteButton.setId(parentId + BUTTON_DELETE);
        deleteButton.setOnMouseClicked(event -> onDelete(deleteButton.getId()));
        Button startUploadButton = new Button(transBasic.getString("startUpload"));
        startUploadButton.setId(parentId + BUTTON_START_UPLOAD);
        startUploadButton.setOnMouseClicked(event -> onStartUpload(startUploadButton.getId()));

        uploadQueueVideos.get(selected).setButton1(editButton);
        uploadQueueVideos.get(selected).setButton2(deleteButton);
        uploadQueueVideos.get(selected).setButton3(startUploadButton);

        // Make sure visual change get to the UI
        updateUploadList();
    }

    /**
     * Called when the cancel button for a upload is clicked.
     * disables editing, reverts all changes made to the upload and changes the visible buttons.
     * @param callerId the id of the upload + button name
     */
    private void onCancel(String callerId) {
        String parentId = callerId.substring(0, callerId.indexOf('_'));
        int selected = getUploadIndexByName(parentId);
        if (selected == -1) {
            System.err.println("cancel button belongs to a invalid or non-existing parent");
            return;
        }
        uploadQueueVideos.get(selected).setEditable(false);
        // Restore from backup and delete it if there is one
        if(editBackups.containsKey(uploadQueueVideos.get(selected).getPaneId())) {
            uploadQueueVideos.set(selected, editBackups.get(uploadQueueVideos.get(selected).getPaneId()));
            uploadQueueVideos.get(selected).getPane().prefWidthProperty().bind(listView.widthProperty());
            editBackups.remove(uploadQueueVideos.get(selected).getPaneId());
        } else {
            AlertUtils.simpleClose(transMainWin.getString("diag_backupNoRestore_short"),
                    transMainWin.getString("diag_backupNoRestore_full")).show();
        }

        // Change buttons
        Button editButton = new Button(transBasic.getString("edit"));
        editButton.setId(parentId + BUTTON_EDIT);
        editButton.setOnMouseClicked(event -> onEdit(editButton.getId()));
        Button deleteButton = new Button(transBasic.getString("delete"));
        deleteButton.setId(parentId + BUTTON_DELETE);
        deleteButton.setOnMouseClicked(event -> onDelete(deleteButton.getId()));
        Button startUploadButton = new Button(transBasic.getString("startUpload"));
        startUploadButton.setId(parentId + BUTTON_START_UPLOAD);
        startUploadButton.setOnMouseClicked(event -> onStartUpload(startUploadButton.getId()));

        uploadQueueVideos.get(selected).setButton1(editButton);
        uploadQueueVideos.get(selected).setButton2(deleteButton);
        uploadQueueVideos.get(selected).setButton3(startUploadButton);

        // Make sure visual change get to the UI
        updateUploadList();
    }

    /**
     * Called when the delete button for a upload is clicked.
     * Shows a confirmation dialog and removes the upload from the list if the user select yes
     * @param callerId the id of the upload + button name
     */
    private void onDelete(String callerId) {
        String parentId = callerId.substring(0, callerId.indexOf('_'));
        int selected = getUploadIndexByName(parentId);
        if (selected == -1) {
            System.err.println("delete button belongs to a invalid or non-existing parent");
            return;
        }
        String desc = String.format(transMainWin.getString("diag_confirmDelete_full"),
                uploadQueueVideos.get(selected).getVideoName());
        Optional<ButtonType> buttonChoice = AlertUtils.yesNo(
                transMainWin.getString("diag_confirmDelete_short"), desc).showAndWait();
        if (buttonChoice.isPresent()) {
            if(buttonChoice.get() == ButtonType.YES) {
                uploadQueueVideos.remove(selected);
                updateUploadList();
            } // else if ButtonType.NO or closed [X] do nothing
        }
        // Make sure visual change get to the UI
        updateUploadList();
    }

    /**
     * Called when the start upload button for a upload is clicked.
     * Schedules the upload to be uploaded after performing a few checks.
     * @param callerId the id of the upload + button name
     */
    private void onStartUpload(String callerId) {
        String parentId = callerId.substring(0, callerId.indexOf('_'));
        int selected = getUploadIndexByName(parentId);
        if (selected == -1) {
            System.err.println("start upload button belongs to a invalid or non-existing parent");
            return;
        }
        // a few small checks first
        if (uploadQueueVideos.get(selected).getVideoName().length() < 1) {
            AlertUtils.simpleClose(transMainWin.getString("diag_noStartUpload_short"),
                    transMainWin.getString("diag_noStartUpload_full_noTitle")).show();
        }
        if (categoryUtils.getCategoryId(uploadQueueVideos.get(selected).getCategory()).equals("-1")) {
            AlertUtils.simpleClose(transMainWin.getString("diag_noStartUpload_short"),
                    transMainWin.getString("diag_noStartUpload_full_noCategory")).show();
        }

        // If the user has not given the program permission to access their youtube channel, ask the user to do so.
        if(configManager.getNeverAuthed()) {
            Optional<ButtonType> buttonChoice = AlertUtils.yesNo(transBasic.getString("auth_short"),
                    transBasic.getString("auth_full")).showAndWait();
            if (buttonChoice.isPresent()) {
                if (buttonChoice.get() == ButtonType.YES) {
                    configManager.setNeverAuthed(false);
                    configManager.saveSettings();
                } else { // ButtonType.NO or closed [X]
                    return;
                }
            }
        }
        // User is authenticated or warned about the upcoming prompt to do so.

        // Queue upload
        uploader.add(uploadQueueVideos.get(selected), uploadQueueVideos.get(selected).getPaneId());
        // Change Buttons and text
        Button ghostButton1 = new Button("just to give it width");
        ghostButton1.setVisible(false);
        Button abortButton = new Button(transBasic.getString("abort"));
        abortButton.setId(parentId + BUTTON_ABORT_UPLOAD);
        abortButton.setOnMouseClicked(event -> onAbort(abortButton.getId()));
        Button ghostButton2 = new Button("");
        ghostButton2.setVisible(false);

        uploadQueueVideos.get(selected).setButton1(ghostButton1);
        uploadQueueVideos.get(selected).setButton2(abortButton);
        uploadQueueVideos.get(selected).setButton3(ghostButton2);

        // make progressbar visible and set text to show it is waiting to be uploaded.
        uploadQueueVideos.get(selected).setProgressBarVisibility(true);
        uploadQueueVideos.get(selected).setStatusLabelText(transBasic.getString("waiting"));

        // Make sure visual change get to the UI
        updateUploadList();
    }

    /**
     * Called when the abort button is clicked on a upload that is scheduled or in progress
     * @param callerId the id of the upload + button name
     */
    private void onAbort(String callerId) {
        String parentId = callerId.substring(0, callerId.indexOf('_'));
        int selected = getUploadIndexByName(parentId);
        if (selected == -1) {
            System.err.println("abort upload button belongs to a invalid or non-existing parent");
            return;
        }
        // Show confirmation dialog, but not if abort all button was clicked
        if (!bypassAbortWarning) {
            String desc = String.format(transMainWin.getString("diag_abortSingle_full"),
                    uploadQueueVideos.get(selected).getVideoName());
            Optional<ButtonType> buttonChoice = AlertUtils.yesNo(
                    transMainWin.getString("diag_abortSingle_short"), desc).showAndWait();
            if (buttonChoice.isPresent()) {
                if (buttonChoice.get() != ButtonType.YES) {
                    // ButtonType.NO or Closed with [X] button
                    return;
                }
            }
        }
        // Abort upload
        boolean abortSuccess = uploader.abortUpload(uploadQueueVideos.get(selected).getPaneId());

        if (abortSuccess) {
            // Set label text and reset progress bar
            uploadQueueVideos.get(selected).setProgressBarVisibility(false);
            uploadQueueVideos.get(selected).setProgressBarProgress(INDETERMINATE_PROGRESS); // reset progBar to be animated
            uploadQueueVideos.get(selected).setStatusLabelText(transBasic.getString("aborted"));
        } else {
            AlertUtils.simpleClose(transBasic.getString("error"), "Failed to terminate upload for unknown reason").show();
        }

        // Change buttons
        Button editButton = new Button(transBasic.getString("edit"));
        editButton.setId(parentId + BUTTON_EDIT);
        editButton.setOnMouseClicked(event -> onEdit(editButton.getId()));
        Button deleteButton = new Button(transBasic.getString("delete"));
        deleteButton.setId(parentId + BUTTON_DELETE);
        deleteButton.setOnMouseClicked(event -> onDelete(deleteButton.getId()));
        Button startUploadButton = new Button(transBasic.getString("startUpload"));
        startUploadButton.setId(parentId + BUTTON_START_UPLOAD);
        startUploadButton.setOnMouseClicked(event -> onStartUpload(startUploadButton.getId()));

        uploadQueueVideos.get(selected).setButton1(editButton);
        uploadQueueVideos.get(selected).setButton2(deleteButton);
        uploadQueueVideos.get(selected).setButton3(startUploadButton);

        // Make sure visual change get to the UI
        updateUploadList();
    }

    private void onResetUpload(String callerId) {
        String parentId = callerId.substring(0, callerId.indexOf('_'));
        int selected = getUploadIndexByName(parentId);
        if (selected == -1) {
            System.err.println("reset upload button belongs to a invalid or non-existing parent");
            return;
        }
        // Change back progressBar color, hide it and set the edit/delete/startUpload buttons
        uploadQueueVideos.get(selected).setProgressBarColor(null);
        uploadQueueVideos.get(selected).setProgressBarVisibility(false);
        uploadQueueVideos.get(selected).setStatusLabelText(transUpload.getString("notStarted"));

        Button editButton = new Button(transBasic.getString("edit"));
        editButton.setId(parentId + BUTTON_EDIT);
        editButton.setOnMouseClicked(event -> onEdit(editButton.getId()));
        Button deleteButton = new Button(transBasic.getString("delete"));
        deleteButton.setId(parentId + BUTTON_DELETE);
        deleteButton.setOnMouseClicked(event -> onDelete(deleteButton.getId()));
        Button startUploadButton = new Button(transBasic.getString("startUpload"));
        startUploadButton.setId(parentId + BUTTON_START_UPLOAD);
        startUploadButton.setOnMouseClicked(event -> onStartUpload(startUploadButton.getId()));

        uploadQueueVideos.get(selected).setButton1(editButton);
        uploadQueueVideos.get(selected).setButton2(deleteButton);
        uploadQueueVideos.get(selected).setButton3(startUploadButton);
    }

    /**
     * Called when an upload finishes.
     * Places the hide button
     * @param paneId the id of the upload
     */
    private void onUploadFinished(String paneId) {
        int index = getUploadIndexByName(paneId);
        if (index == -1) {
            System.err.println("Unknown upload just finished: " + paneId);
            return;
        }
        Button finishedUploadButton = new Button(transBasic.getString("hide"));
        finishedUploadButton.setId(paneId + BUTTON_FINISHED_UPLOAD);
        finishedUploadButton.setOnMouseClicked(event -> onRemoveFinishedUpload(finishedUploadButton.getId()));
        uploadQueueVideos.get(index).setButton2(finishedUploadButton);
        updateUploadList();
    }

    /**
     * Called if an upload errors/fails
     *
     * @param video the video that failed uploading
     * @param e     the exception that occurred
     */
    private void onUploadErred(VideoUpload video, Throwable e) {
        String header = transBasic.getString("app_name") + " - Failed to upload video";
        if (e == null) {
            if (video == null) {
                AlertUtils.simpleClose(header, "Failed uploading of a undefined video with an unknown error").show();
            } else { // e == null && video != null
                AlertUtils.simpleClose(header, "An unknown error occurred while trying to upload \"" + video.getVideoName() + "\"").show();
            }
        } else if (video == null) { // e != null
            AlertUtils.exceptionDialog(header, "Failed uploading of a undefined video", e);
        } else { // e != null && video != null
            AlertUtils.exceptionDialog(header, "Failed to upload the video \"" + video.getVideoName() + "\"", e);
        }
        //Switch to a reset upload button instead of abort
        if (video != null) {
            video.setProgressBarColor("red");
            video.setStatusLabelText(transUpload.getString("failed"));
            Button resetButton = new Button(transBasic.getString("reset"));
            resetButton.setId(video.getPaneId() + BUTTON_RESET);
            resetButton.setOnMouseClicked(event -> onResetUpload(resetButton.getId()));
            video.setButton2(resetButton);
        }
    }

    /**
     * Called when the hide button that appears then a upload finishes is clicked.
     * Removes that upload from the list
     * @param callerId the id of the upload + button name
     */
    private void onRemoveFinishedUpload(String callerId) {
        String parentId = callerId.substring(0, callerId.indexOf('_'));
        int selected = getUploadIndexByName(parentId);
        if (selected == -1) {
            System.err.println("remove finished upload button belongs to a invalid or non-existing parent");
            return;
        }
        uploadQueueVideos.remove(selected);
        updateUploadList();
    }
}
