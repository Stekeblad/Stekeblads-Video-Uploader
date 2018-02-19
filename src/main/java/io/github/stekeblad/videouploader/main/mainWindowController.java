package io.github.stekeblad.videouploader.main;

import io.github.stekeblad.videouploader.settings.PresetsWindowController;
import io.github.stekeblad.videouploader.utils.AlertUtils;
import io.github.stekeblad.videouploader.utils.ConfigManager;
import io.github.stekeblad.videouploader.utils.PickFile;
import io.github.stekeblad.videouploader.youtube.Uploader;
import io.github.stekeblad.videouploader.youtube.VideoPreset;
import io.github.stekeblad.videouploader.youtube.VideoUpload;
import io.github.stekeblad.videouploader.youtube.utils.CategoryUtils;
import io.github.stekeblad.videouploader.youtube.utils.PlaylistUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import static io.github.stekeblad.videouploader.utils.Constants.*;
import static io.github.stekeblad.videouploader.youtube.VideoInformationBase.*;
import static io.github.stekeblad.videouploader.youtube.VideoUpload.NODE_ID_PROGRESS;

public class mainWindowController implements Initializable {
    public ListView<GridPane> listView;
    public Button buttonPickFile;
    public ListView<String> chosen_files;
    public Button btn_presets;
    public ChoiceBox<String> choice_presets;
    public AnchorPane mainWindowPane;
    public TextField text_autoNum;
    public Button btn_applyPreset;
    public Button btn_removeFinished;
    public Button btn_startAll;
    public Button btn_abortAll;

    private ConfigManager configManager;
    private PlaylistUtils playlistUtils;
    private CategoryUtils categoryUtils;
    private int uploadPaneCounter = 0;
    private List<VideoUpload> uploadQueueVideos;
    private List<File> videosToAdd;
    private HashMap<String, VideoUpload> editBackups;
    private Uploader uploader;
    private static final String UPLOAD_PANE_ID_PREFIX = "upload-";
    private boolean bybassAbortWarning = false;

    /**
     * Initialize things when the window is opened
     * @param location provided by fxml
     * @param resources provided by fxml
     */
    @FXML
    public void initialize(URL location, ResourceBundle resources) {

        uploadPaneCounter = 0;
        uploadQueueVideos = new ArrayList<>();
        editBackups = new HashMap<>();
        configManager = ConfigManager.INSTANCE;
        configManager.configManager();
        if (configManager.getNoSettings()) {
            // If no settings was loaded, open settings window
            onSettingsClicked(new ActionEvent());
            configManager.setNoSettings(false);
            configManager.saveSettings();
        }
        playlistUtils = PlaylistUtils.INSTANCE;
        playlistUtils.loadCache();
        categoryUtils = CategoryUtils.INSTANCE;
        categoryUtils.loadCategories();
        choice_presets.setItems(FXCollections.observableArrayList(configManager.getPresetNames()));
        btn_startAll.setTooltip(new Tooltip("Starts all uploads that have the \"Start Upload\" button visible"));

        // Only allow numbers in autoNum textField
        text_autoNum.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                text_autoNum.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        text_autoNum.setTooltip(new Tooltip("Requires the $(ep) tag in preset video title"));

        uploader = new Uploader();
        uploader.setUploadFinishedCallback(s -> Platform.runLater(() -> onUploadFinished(s)));

        if(configManager.hasWaitingUploads()) {
            ArrayList<String> waitingUploads = configManager.getWaitingUploads();
            if (waitingUploads != null) {
                for(String waitingUpload : waitingUploads) {
                    try {
                        VideoUpload loadedUpload = new VideoUpload(waitingUpload, String.valueOf(uploadPaneCounter++));
                        Button editButton = new Button("Edit");
                        editButton.setId(loadedUpload.getPaneId() + BUTTON_EDIT);
                        editButton.setOnMouseClicked(event -> onEdit(editButton.getId()));
                        Button deleteButton = new Button("Delete");
                        deleteButton.setId(loadedUpload.getPaneId() + BUTTON_DELETE);
                        deleteButton.setOnMouseClicked(event -> onDelete(deleteButton.getId()));
                        Button startUploadButton = new Button("Start Upload");
                        startUploadButton.setId(loadedUpload.getPaneId() + BUTTON_START_UPLOAD);
                        startUploadButton.setOnMouseClicked(event -> onStartUpload(startUploadButton.getId()));

                        loadedUpload.setButton1(editButton);
                        loadedUpload.setButton2(deleteButton);
                        loadedUpload.setButton3(startUploadButton);

                        uploadQueueVideos.add(loadedUpload);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            updateUploadList();
        }
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
        String choice = AlertUtils.threeButtons("Close Program?",
                "Do you want to close, now? There is currently one or more uploads in progress and they will " +
                        "be stopped if you close the program. What do you want to do?",
                "Do not close", "Stop all uploads", "auto-restart started but unfinished uploads next time");
        if (choice == null) {
            return false;
        }
        switch (choice) {
            case "Do not close":
                return false;
            case "Stop all uploads":
                uploader.kill();
                return true;
            case "auto-restart started but unfinished uploads next time":
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
        videosToAdd = PickFile.pickVideos();
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
            AlertUtils.simpleClose("No files selected", "Please select files to upload").show();
            return;
        }
        // Check what preset / if a preset is selected
        if(choice_presets.getSelectionModel().getSelectedIndex() == -1) {
            // No preset, add videos to upload list with file name as title and blank/default values on the rest
            for(File videoFile : videosToAdd) {
                VideoUpload newUpload = new VideoUpload(videoFile.getName(), null, null,
                        null, null, null, false, null,
                        UPLOAD_PANE_ID_PREFIX + uploadPaneCounter, videoFile);
                uploadQueueVideos.add(newUpload);
                // Enables the upload to be edited because the lack of details.
                onEdit(UPLOAD_PANE_ID_PREFIX + uploadPaneCounter + "_fakeButton");
                uploadPaneCounter++;
            }
        } else { // preset selected
            VideoPreset chosenPreset;
            // Get the auto numbering and preset
            int autoNum;
            try {
                autoNum = Integer.valueOf(text_autoNum.getText());
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

                // Create the buttons
                Button editButton = new Button("Edit");
                editButton.setId(newUpload.getPaneId() + BUTTON_EDIT);
                editButton.setOnMouseClicked(event -> onEdit(editButton.getId()));
                Button deleteButton = new Button("Delete");
                deleteButton.setId(newUpload.getPaneId() + BUTTON_DELETE);
                deleteButton.setOnMouseClicked(event -> onDelete(deleteButton.getId()));
                Button startUploadButton = new Button("Start Upload");
                startUploadButton.setId(newUpload.getPaneId() + BUTTON_START_UPLOAD);
                startUploadButton.setOnMouseClicked(event -> onStartUpload(startUploadButton.getId()));

                newUpload.setButton1(editButton);
                newUpload.setButton2(deleteButton);
                newUpload.setButton3(startUploadButton);

                uploadQueueVideos.add(newUpload);
                uploadPaneCounter++;
            }
            // update autoNum textField
            text_autoNum.setText(String.valueOf(autoNum));
        }
        // Removes the newly added uploads from the selected files list and update UI
        videosToAdd = null;
        chosen_files.setItems(FXCollections.observableArrayList(new ArrayList<>()));
        updateUploadList();
        actionEvent.consume();
    }

    /**
     * Called when the settings button is clicked.
     * Opens the settings window.
     * @param actionEvent the click event
     */
    public void onSettingsClicked(ActionEvent actionEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(mainWindowController.class.getClassLoader().getResource("fxml/PresetsWindow.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 725, 700);
            Stage stage = new Stage();
            stage.setMinWidth(725);
            stage.setMinHeight(550);
            stage.setTitle("Settings - Stekeblads Video Uploader");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL); // Make it always above mainWindow
            PresetsWindowController controller = fxmlLoader.getController();
            stage.setOnCloseRequest(controller::onWindowClose);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
        actionEvent.consume();
        // Update presets choice box in case presets was added or remove
        choice_presets.setItems(FXCollections.observableArrayList(configManager.getPresetNames()));
    }

    /**
     * Called when the start all ready uploads button is clicked.
     * If a upload is allowed to be started (the start upload button is visible) then that button is clicked by this method.
     * @param actionEvent the click event
     */
    public void onStartAllUploadsClicked(ActionEvent actionEvent) {
        // Check if the user has given the program permission to access the user's youtube account, if not then ask for it
        if(configManager.getNeverAuthed()) {
            Optional<ButtonType> buttonChoice = AlertUtils.yesNo("Authentication Required",
                    "To upload videos you must grant this application permission to access your Youtube channel. " +
                            "Do you want to allow \"Stekeblads Youtube Uploader\" to access Your channel?" +
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
        Optional<ButtonType> buttonChoice = AlertUtils.yesNo("Abort ALL Uploads?",
                "Are you sure you want to abort the uploading of all started uploads?").showAndWait();
        if (buttonChoice.isPresent()) {
            if (buttonChoice.get() != ButtonType.YES) {
                // ButtonType.NO or Closed with [X] button
                return;
            }
        }
        // Prevent the "Are you sure you want to abort X?" dialog for every upload
        bybassAbortWarning = true;
        // Abort the uploads in the reversed order of that they was most likely started in
        // to avoid that the program attempts to start a new upload that will also be aborted, and then the next one...
        for (int i = uploadQueueVideos.size() - 1; i >= 0; i--) {
            if(uploadQueueVideos.get(i).getButton2Id() != null &&
                    uploadQueueVideos.get(i).getButton2Id().contains(BUTTON_ABORT_UPLOAD)) {
                onAbort(uploadQueueVideos.get(i).getButton2Id());
            }
        }
        // Re-enable the individual confirmation on aborts
        bybassAbortWarning = false;
        actionEvent.consume();
    }

    /**
     * Re-adds all elements to the uploadQueuePanes so the UI is up-to-date
     */
    private void updateUploadList() {
        List<GridPane> uploadQueuePanes = new ArrayList<>();
        for(VideoUpload vid : uploadQueueVideos) {
            uploadQueuePanes.add(vid.getUploadPane());
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
        uploadQueueVideos.get(selected).getPane().lookup("#" + parentId + NODE_ID_THUMBNAIL).setOnMouseClicked(event -> {
            File pickedThumbnail = PickFile.pickThumbnail();
            if(pickedThumbnail != null) {
                try {
                    uploadQueueVideos.get(selected).setThumbNailFile(pickedThumbnail);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        uploadQueueVideos.get(selected).getPane().lookup("#" + parentId + NODE_ID_PLAYLIST).setOnMouseClicked(event -> {
            if(configManager.getNeverAuthed()) {
                AlertUtils.simpleClose("No playlists", "No playlists synced yet, go to the settings window to sync with Youtube").show();
            } else {
                uploadQueueVideos.get(selected).setPlaylists(playlistUtils.getVisiblePlaylistnames());
            }
        });
        uploadQueueVideos.get(selected).getPane().lookup("#" + parentId + NODE_ID_CATEGORY).setOnMouseClicked(event ->
                uploadQueueVideos.get(selected).setCategories(categoryUtils.getCategoryNames()));

        // Set buttons
        Button saveButton = new Button("Save");
        saveButton.setId(parentId + BUTTON_SAVE);
        saveButton.setOnMouseClicked(event -> onSave(saveButton.getId()));
        Button cancelButton = new Button("Cancel");
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
        // Check fields
        if(uploadQueueVideos.get(selected).getVideoName().equals("")) {
            AlertUtils.simpleClose("Title Required", "Video does not have a title").show();
            return;
        }
        // Everything else is not required or given a default value that can not be set to a invalid value

        uploadQueueVideos.get(selected).setEditable(false);
        // Delete backup if there is one
        if(editBackups.containsKey(uploadQueueVideos.get(selected).getPaneId())) {
            editBackups.remove(uploadQueueVideos.get(selected).getPaneId());
        }

        // Change buttons
        Button editButton = new Button("Edit");
        editButton.setId(parentId + BUTTON_EDIT);
        editButton.setOnMouseClicked(event -> onEdit(editButton.getId()));
        Button deleteButton = new Button("Delete");
        deleteButton.setId(parentId + BUTTON_DELETE);
        deleteButton.setOnMouseClicked(event -> onDelete(deleteButton.getId()));
        Button startUploadButton = new Button("Start Upload");
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
            editBackups.remove(uploadQueueVideos.get(selected).getPaneId());
        } else {
            AlertUtils.simpleClose("Can not change back", "Could not revert this upload to the state it was in before edit").show();
        }

        // Change buttons
        Button editButton = new Button("Edit");
        editButton.setId(parentId + BUTTON_EDIT);
        editButton.setOnMouseClicked(event -> onEdit(editButton.getId()));
        Button deleteButton = new Button("Delete");
        deleteButton.setId(parentId + BUTTON_DELETE);
        deleteButton.setOnMouseClicked(event -> onDelete(deleteButton.getId()));
        Button startUploadButton = new Button("Start Upload");
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
        Optional<ButtonType> buttonChoice = AlertUtils.yesNo("Confirm delete",
                "Are you sure you want to remove \"" +  uploadQueueVideos.get(selected).getVideoName() +
                "\" from the upload queue?").showAndWait();
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
            AlertUtils.simpleClose("Can not start", "Cant start the upload, the video does not have a name").show();
        }
        if (categoryUtils.getCategoryId(uploadQueueVideos.get(selected).getCategory()).equals("-1")) {
            AlertUtils.simpleClose("Can not start", "Category is not selected or invalid").show();
        }

        // If the user has not given the program permission to access their youtube channel, ask the user to do so.
        if(configManager.getNeverAuthed()) {
            Optional<ButtonType> buttonChoice = AlertUtils.yesNo("Authentication Required",
                    "To upload videos you must grant this application permission to access your Youtube channel. " +
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
        Button abortButton = new Button("Abort");
        abortButton.setId(parentId + BUTTON_ABORT_UPLOAD);
        abortButton.setOnMouseClicked(event -> onAbort(abortButton.getId()));
        Button ghostButton2 = new Button("");
        ghostButton2.setVisible(false);

        uploadQueueVideos.get(selected).setButton1(ghostButton1);
        uploadQueueVideos.get(selected).setButton2(abortButton);
        uploadQueueVideos.get(selected).setButton3(ghostButton2);

        // make progressbar visible and set text to show it is waiting to be uploaded.
        uploadQueueVideos.get(selected).getPane().lookup("#" + parentId + NODE_ID_PROGRESS).setVisible(true);
        uploadQueueVideos.get(selected).setStatusLabelText("Waiting...");

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
        if(! bybassAbortWarning) {
            Optional<ButtonType> buttonChoice = AlertUtils.yesNo("Abort upload?",
                    "Are you sure you want to abort the uploading of " +
                            "\"" + uploadQueueVideos.get(selected).getVideoName() + "\"?").showAndWait();
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
            uploadQueueVideos.get(selected).getPane().lookup("#" + parentId + NODE_ID_PROGRESS).setVisible(false);
            uploadQueueVideos.get(selected).setStatusLabelText("Aborted");
        } else {
            AlertUtils.simpleClose("Error", "Failed to terminate upload for unknown reason");
        }

        // Change buttons
        Button editButton = new Button("Edit");
        editButton.setId(parentId + BUTTON_EDIT);
        editButton.setOnMouseClicked(event -> onEdit(editButton.getId()));
        Button deleteButton = new Button("Delete");
        deleteButton.setId(parentId + BUTTON_DELETE);
        deleteButton.setOnMouseClicked(event -> onDelete(deleteButton.getId()));
        Button startUploadButton = new Button("Start Upload");
        startUploadButton.setId(parentId + BUTTON_START_UPLOAD);
        startUploadButton.setOnMouseClicked(event -> onStartUpload(startUploadButton.getId()));

        uploadQueueVideos.get(selected).setButton1(editButton);
        uploadQueueVideos.get(selected).setButton2(deleteButton);
        uploadQueueVideos.get(selected).setButton3(startUploadButton);

        // Make sure visual change get to the UI
        updateUploadList();
    }

    /**
     * Called when a upload finishes.
     * Places the hide button
     * @param paneId the id of the upload
     */
    private void onUploadFinished(String paneId) {
        int index = getUploadIndexByName(paneId);
        if (index == -1) {
            System.err.println("Unknown upload just finished: " + paneId);
            return;
        }
        Button finishedUploadButton = new Button("hide");
        finishedUploadButton.setId(paneId + BUTTON_FINISHED_UPLOAD);
        finishedUploadButton.setOnMouseClicked(event -> onRemoveFinishedUpload(finishedUploadButton.getId()));
        uploadQueueVideos.get(index).setButton2(finishedUploadButton);
        updateUploadList();
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
