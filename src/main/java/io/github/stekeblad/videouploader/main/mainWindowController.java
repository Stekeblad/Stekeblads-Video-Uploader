package io.github.stekeblad.videouploader.main;

import io.github.stekeblad.videouploader.ListControllers.UploadItemController;
import io.github.stekeblad.videouploader.jfxExtension.IWindowController;
import io.github.stekeblad.videouploader.jfxExtension.MyStage;
import io.github.stekeblad.videouploader.jfxExtension.NoneSelectionModel;
import io.github.stekeblad.videouploader.jfxExtension.stringConverters.VideoPresetStringConverter;
import io.github.stekeblad.videouploader.managers.CategoryManager;
import io.github.stekeblad.videouploader.managers.PlaylistManager;
import io.github.stekeblad.videouploader.managers.PresetManager;
import io.github.stekeblad.videouploader.managers.SettingsManager;
import io.github.stekeblad.videouploader.models.NewVideoPresetModel;
import io.github.stekeblad.videouploader.models.NewVideoUploadModel;
import io.github.stekeblad.videouploader.utils.AlertUtils;
import io.github.stekeblad.videouploader.utils.Constants;
import io.github.stekeblad.videouploader.utils.TimeUtils;
import io.github.stekeblad.videouploader.utils.background.OpenInBrowser;
import io.github.stekeblad.videouploader.utils.background.PresetApplicator;
import io.github.stekeblad.videouploader.utils.background.UpdaterUi;
import io.github.stekeblad.videouploader.utils.translation.TranslationBundles;
import io.github.stekeblad.videouploader.utils.translation.Translations;
import io.github.stekeblad.videouploader.utils.translation.TranslationsManager;
import io.github.stekeblad.videouploader.windowControllers.PresetsWindowController;
import io.github.stekeblad.videouploader.youtube.Uploader;
import io.github.stekeblad.videouploader.youtube.exceptions.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static io.github.stekeblad.videouploader.utils.Constants.*;
import static javafx.scene.control.ProgressIndicator.INDETERMINATE_PROGRESS;

public class mainWindowController implements IWindowController {
    public AnchorPane mainWindowPane;
    public ToolBar toolbar;
    public ListView<UploadItemController> listUploads;
    public HBox box_presetProgress;
    public ListView<File> chosen_files;
    public ChoiceBox<NewVideoPresetModel> choice_presets;
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
    public Label label_presetProgress;
    public ProgressBar progress_preset;

    private SettingsManager settingsManager;
    private PlaylistManager playlistManager;
    private CategoryManager categoryManager;
    private PresetManager presetManager;

    private ObservableList<UploadItemController> uploadItems;
    private int presetsInProgress = 0;
    private boolean bypassAbortWarning = false;

    private Uploader uploader;
    private PresetApplicator presetApplicator;

    private Translations transMainWin;
    private Translations transBasic;
    private Translations transUpload;

    /**
     * Initialize things when the window is opened, used instead of initialize as that one does not have access to the scene
     */
    public void myInit() {
        // Set the default exception handler to catch the exceptions that is not already caught
        // Is this needed in every window or only in the Main.java file?
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> AlertUtils.unhandledExceptionDialog(exception));

        // Load Translations
        transMainWin = TranslationsManager.getTranslation(TranslationBundles.WINDOW_MAIN);
        transMainWin.autoTranslate(mainWindowPane);
        // Bugged:
        // System.out.println(toolbar.getChildrenUnmodifiable());
        btn_presets.setText(transMainWin.getString("btn_presets"));
        btn_settings.setText(transMainWin.getString("btn_settings"));

        transBasic = TranslationsManager.getTranslation(TranslationBundles.BASE);
        transUpload = TranslationsManager.getTranslation(TranslationBundles.PRESET_UPLOAD);

        // Load custom CSS (for improved readability of disabled ChoiceBoxes)
        URL css_path = mainWindowController.class.getClassLoader().getResource("css/disabled.css");
        if (css_path != null) {
            mainWindowPane.getScene().getStylesheets().add(css_path.toString());
        }

        uploader = new Uploader();
        presetApplicator = new PresetApplicator();

        settingsManager = SettingsManager.getSettingsManager();
        playlistManager = PlaylistManager.getPlaylistManager();
        categoryManager = CategoryManager.getCategoryManager();
        presetManager = PresetManager.getPresetManager();

        // Populate presets dropdown/choice box
        choice_presets.setItems(presetManager.getAllPresetsIncludingDefault());
        choice_presets.setConverter(new VideoPresetStringConverter());
        choice_presets.getSelectionModel().select(0);

        // Only allow numbers in autoNum textField
        txt_autoNum.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txt_autoNum.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        // Connect events and callbacks
        uploader.setUploadFinishedCallback(s -> Platform.runLater(() -> onUploadFinished(s)));
        uploader.setUploadErredCallback((videoUpload, throwable) -> Platform.runLater(() -> onUploadErred(videoUpload, throwable)));
        presetApplicator.setSuccessCallback(upload -> Platform.runLater(() -> onPresetApplicationSuccess(upload)));
        presetApplicator.setErrorCallback((video, throwable) -> Platform.runLater(() -> onPresetApplicationError(video, throwable)));

        // Set selectionModel for the uploads listView
        listUploads.setSelectionModel(new NoneSelectionModel<>());

        uploadItems = FXCollections.emptyObservableList();
        listUploads.setItems(uploadItems);

        // If any uploads was saved when the program was closed last time
        if (settingsManager.hasWaitingUploads()) {
            // TODO: Implement
        }
        // Set so pressing F1 opens the wiki page for this window
        Scene scene = mainWindowPane.getScene();
        scene.setOnKeyPressed((event) -> {
            if (event.getCode() == KeyCode.F1) {
                OpenInBrowser.openInBrowser("https://github.com/Stekeblad/Stekeblads-Video-Uploader/wiki/Main-Window");
                event.consume();
            }
        });

        // Start a background check for updates
        UpdaterUi updater = new UpdaterUi();
        updater.runUpdater(true);
    }

    /**
     * This method is called when this window's close button is clicked.
     * If one or more uploads is queued a confirmation dialog will be showed to ask the user if they want to
     * abort them or not.
     * @return true if the window should to be closed, false if not
     */
    public boolean onWindowClose() {
        // stop PresetApplicator
        presetApplicator.kill();

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
            //TODO: For each task, save that upload so it can be recreated net time program runs
            return true;
        }
        return false;
    }

    /**
     * Called when the apply preset button is clicked.
     * Takes the files in the selected files list and applies the selected preset or uses a blank preset if none is selected.
     * Populates the uploads list.
     * @param actionEvent the click event
     */
    public void onApplyPresetClicked(ActionEvent actionEvent) {
        List<File> videosToAdd = chosen_files.getItems();
        if(videosToAdd == null || videosToAdd.size() == 0 ) {
            AlertUtils.simpleClose(transMainWin.getString("diag_noFiles_short"),
                    transMainWin.getString("diag_noFiles_full")).show();
            return;
        }
        // Check which preset / if a preset is selected
        if (choice_presets.getSelectionModel().getSelectedIndex() < 1) {
            // No preset, add videos to upload list with file name as title and blank/default values on the rest
            for(File videoFile : videosToAdd) {
                NewVideoUploadModel newUpload = new NewVideoUploadModel();
                newUpload.setVideoName(videoFile.getName());
                newUpload.setMadeForKids(false);
                newUpload.setTellSubs(false);
                newUpload.setVideoFile(videoFile);

                uploadItems.add(new UploadItemController(newUpload, listUploads.prefWidthProperty()));

                // Enables the upload to be edited because the lack of details.
                // Change the cancel button to a delete button, the backed up state created by onEdit is not valid
                // TODO: Control this?
            }
        } else { // preset selected
            // Load details of the selected preset
            NewVideoPresetModel chosenPreset = choice_presets.getSelectionModel().getSelectedItem();

            // Get the auto numbering
            int autoNum;
            try {
                autoNum = Integer.parseInt(txt_autoNum.getText());
            } catch (NumberFormatException ex) {
                autoNum = 1;
            }

            // update progress message
            updatePresetProgressIndicator(videosToAdd.size());

            try {
                // Queue the videos and preset to be combined in a background thread
                presetApplicator.applyPreset(videosToAdd, chosenPreset, autoNum);
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtils.exceptionDialog(transBasic.getString("app_name") + "Failed applying preset",
                        "An unexpected error occurred while preparing to apply the selected preset to your videos", e);
            }

            // update autoNum textField
            txt_autoNum.setText(String.valueOf(autoNum + videosToAdd.size()));
        }
        // Removes the newly added uploads from the selected files list and update UI
        videosToAdd = null;
        chosen_files.setItems(FXCollections.observableArrayList(new ArrayList<>()));
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
            Stage stage = new Stage();
            stage.setTitle(transBasic.getString("app_presetWindowTitle"));
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL); // Make it always above mainWindow
            PresetsWindowController controller = fxmlLoader.getController();
            stage.setOnCloseRequest(controller::onWindowClose);
            controller.myInit(false);// TODO: verify that this is not needed
            stage.showAndWait();
        } catch (IOException e) {
            AlertUtils.exceptionDialog(transBasic.getString("error"), transBasic.getString("errOpenWindow"), e);
        }
        actionEvent.consume();
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
            MyStage stage = new MyStage(WindowPropertyNames.SETTINGS);
            stage.makeScene(fxmlLoader.load(), Constants.SETTINGS_WINDOW_DIMENSIONS_RESTRICTION);
            stage.setTitle(transBasic.getString("app_settingsWindowTitle"));
            stage.initModality(Modality.APPLICATION_MODAL); // Make it always above mainWindow
            stage.prepareControllerAndShow(fxmlLoader.getController());
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
        if (settingsManager.getNeverAuthed()) {
            ButtonType userChoice = AlertUtils.yesNo(transBasic.getString("auth_short"),
                    transBasic.getString("auth_full"), ButtonType.NO);

            if (userChoice == ButtonType.NO)
                return;
        }
        // Permission given, start uploads
        for (UploadItemController uploadQueueVideo : uploadItems) {
            uploadQueueVideo.startUpload(new ActionEvent());
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
        actionEvent.consume();
    }

    /**
     * Aborts all uploads that have been started
     * @param actionEvent the button click event
     */
    public void onAbortAllUploadsClicked(ActionEvent actionEvent) {
        // Show confirmation dialog
        if (!bypassAbortWarning) { // can be set from onAbortAndClearClicked
            ButtonType userChoice = AlertUtils.yesNo(transMainWin.getString("diag_abortAll_short"),
                    transMainWin.getString("diag_abortAll_full"), ButtonType.NO);
            if (userChoice == ButtonType.NO) {
                return;
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
        ButtonType userChoice = AlertUtils.yesNo(transMainWin.getString("diag_abortAllClear_short"),
                transMainWin.getString("diag_abortAllClear_full"), ButtonType.NO);
        if (userChoice == ButtonType.YES) {
            bypassAbortWarning = true; // is set back to false by onAbortAllUploadsClicked
            onAbortAllUploadsClicked(new ActionEvent());
            uploadItems.clear();
        }
        actionEvent.consume();
    }

    /**
     * Adds and removes the progress indicator for presets currently being applied to videos.
     * Sets the progress indicator to invisible if presetInProgress counter becomes zero, otherwise sets it to visible
     *
     * @param change how much to change the counter with, a positive number when the queue is growing,
     *               negative when tasks finish
     */
    private void updatePresetProgressIndicator(int change) {
        presetsInProgress += change;
        box_presetProgress.setVisible(presetsInProgress != 0);
        label_presetProgress.setText(String.format(transMainWin.getString("label_presetProgress"), presetsInProgress));
    }

    /**
     * Called when the delete button for a upload is clicked.
     * Shows a confirmation dialog and removes the upload from the list if the user select yes
     * @param callerId the id of the upload + button name
     */
    private void onDelete(String callerId) {
        String desc = String.format(transMainWin.getString("diag_confirmDelete_full"),
                uploadQueueVideos.get(selected).getVideoName());

        ButtonType userChoice = AlertUtils.yesNo(transMainWin.getString("diag_confirmDelete_short"),
                desc, ButtonType.NO);
        if (userChoice == ButtonType.YES) {
            // delete backup (may exist if upload was created with no preset and directly deleted
            editBackups.remove(uploadQueueVideos.get(selected).getPaneId());
            uploadQueueVideos.remove(selected);
        } // else if ButtonType.NO or closed [X] do nothing

        // Make sure visual change get to the UI
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
            return;
        }
        if (categoryManager.findById(uploadQueueVideos.get(selected).getCategory()).equals("-1")) {
            AlertUtils.simpleClose(transMainWin.getString("diag_noStartUpload_short"),
                    transMainWin.getString("diag_noStartUpload_full_noCategory")).show();
            return;
        }

        // If the user has not given the program permission to access their youtube channel, ask the user to do so.
        if (settingsManager.getNeverAuthed()) {
            ButtonType userChoice = AlertUtils.yesNo(transBasic.getString("auth_short"),
                    transBasic.getString("auth_full"), ButtonType.NO);

            if (userChoice == ButtonType.NO)
                return;


        }
        // User is authenticated or is warned about the upcoming prompt to do so.

        // Queue upload
        uploader.add(uploadQueueVideos.get(selected), uploadQueueVideos.get(selected).getPaneId());

        // Change buttons, make progressbar visible and set text to show it is waiting to be uploaded.
        uploadQueueVideos.get(selected).setProgressBarVisibility(true);
        uploadQueueVideos.get(selected).setStatusLabelText(transBasic.getString("waiting"));
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

            ButtonType userChoice = AlertUtils.yesNo(transMainWin.getString("diag_abortSingle_short"),
                    desc, ButtonType.NO);
            if (userChoice == ButtonType.NO) {
                // ButtonType.NO or Closed with [X] button
                return;
            }
        }
        // Abort upload
        boolean abortSuccess = uploader.abortUpload(uploadQueueVideos.get(selected).getPaneId());

        if (abortSuccess) {
            // Set label text and reset progress bar
            uploadQueueVideos.get(selected).setProgressBarVisibility(false);
            uploadQueueVideos.get(selected).setProgressBarProgress(INDETERMINATE_PROGRESS); // reset progressBar to be animated
            uploadQueueVideos.get(selected).setStatusLabelText(transBasic.getString("aborted"));
        } else {
            AlertUtils.simpleClose(transBasic.getString("error"), "Failed to terminate upload for unknown reason").show();
        }
    }

    /**
     * Called when Reset upload button is clicked (appear when an upload failed)
     *
     * @param callerId the id of the upload + button name
     */
    private void onResetUpload(String callerId) {
        // Change back progressBar color, hide it and set the locked state buttons
        uploadQueueVideos.get(selected).setProgressBarColor(null);
        uploadQueueVideos.get(selected).setProgressBarVisibility(false);
        uploadQueueVideos.get(selected).setStatusLabelText(transUpload.getString("_status"));
    }

    /**
     * Called when the hide button that appears then a upload finishes is clicked.
     * Removes that upload from the list
     *
     * @param callerId the id of the upload + button name
     */
    private void onRemoveFinishedUpload(String callerId) {
        String parentId = callerId.substring(0, callerId.indexOf('_'));
        int selected = getUploadIndexByName(parentId);
        if (selected == -1) {
            System.err.println("remove finished upload, button belongs to a invalid or non-existing parent");
            return;
        }
        editBackups.remove(uploadQueueVideos.get(selected).getPaneId());
        uploadQueueVideos.remove(selected);
    }

    /**
     * Called when PresetApplicator successfully applied a preset to a video
     *
     * @param newUpload the newly created VideoUpload created from a File and a VideoPreset
     */
    private void onPresetApplicationSuccess(NewVideoUploadModel newUpload) {
        // make the upload change its width together with the uploads list and the window
        newUpload.getPane().prefWidthProperty().bind(listUploads.widthProperty().subtract(35));

        transUpload.autoTranslate(newUpload.getPane(), newUpload.getPaneId());
        uploadQueueVideos.add(newUpload);
        updatePresetProgressIndicator(-1);
    }

    /**
     * Called if PresetApplicator fail with applying a preset to a video
     *
     * @param video the video that was being processed when the exception was thrown
     * @param e     the exception that occurred
     */
    private void onPresetApplicationError(File video, Throwable e) {
        String header = transBasic.getString("app_name") + " - Failed to apply preset";
        AlertUtils.exceptionDialog(header, "Failed to apply preset to the file \"" + video.getName() + "\"", e);
        updatePresetProgressIndicator(-1);
    }

    /**
     * Called when an upload finishes.
     * Places the hide button
     * @param paneId the id of the upload
     */
    private void onUploadFinished(String paneId) {
        Button finishedUploadButton = new Button(transBasic.getString("hide"));
        finishedUploadButton.setId(paneId + BUTTON_FINISHED_UPLOAD);
        finishedUploadButton.setOnMouseClicked(event -> onRemoveFinishedUpload(finishedUploadButton.getId()));
        uploadQueueVideos.get(index).setButton2(finishedUploadButton);
    }

    /**
     * Called if an upload errors/fails
     *
     * @param video the video that failed uploading
     * @param e     the exception that occurred
     */
    private void onUploadErred(NewVideoUploadModel video, Throwable e) {
        String header = transBasic.getString("app_name") + " - Failed to upload video";
        if (e == null && video == null) {
            AlertUtils.simpleClose(header, "For an unknown reason is error information not available").show();
        }

        if (e == null && video != null) {
            AlertUtils.simpleClose(header, "An unknown error occurred while uploading the video " +
                    video.getVideoName()).show();
        }

        if (e != null) {
            String onVideoWithName = "";
            if (video != null)
                onVideoWithName = "\n\nThis happened on the video named " + video.getVideoName();
            if (e instanceof QuotaLimitExceededException) {
                // abort all scheduled uploads, they will all fail with this error
                bypassAbortWarning = true;
                for (String key : uploader.getUploadQueue()) {
                    uploader.abortUpload((key));
                    onAbort(key + "_fakeButton");
                }
                bypassAbortWarning = false;

                // Find when midnight in the pacific timezone is in the user's timezone
                String userClockAtPacificMidnight = TimeUtils.fromMidnightPacificToUserTimeZone();
                AlertUtils.simpleClose(header, transBasic.getString("app_name") + " has reached its daily upload limit" +
                        " in the YouTube API and can not continue the uploading. All scheduled uploads has been aborted.\n\n" +
                        "The limit will be reset at midnight Pacific Time. (" + userClockAtPacificMidnight + " in your timezone.)").show();
            } else if (e instanceof UploadLimitExceededException) {
                // abort all scheduled uploads, they will all fail with this error
                bypassAbortWarning = true;
                for (String key : uploader.getUploadQueue()) {
                    uploader.abortUpload((key));
                    onAbort(key + "_fakeButton");
                }
                bypassAbortWarning = false;
                AlertUtils.simpleClose(header, "You have reached your personal upload limit on YouTube" +
                        " and you can not upload more videos right now. All scheduled uploads has been aborted.\n\n" +
                        "Wait a few hours or retry again tomorrow").show();
            } else if (e instanceof InvalidVideoDetailsException) {
                AlertUtils.simpleClose(header, "YouTube reported a video property contains invalid content: " +
                        e.getMessage() + onVideoWithName).show();
            } else if (e instanceof InvalidMissingImageException) {
                AlertUtils.simpleClose(transBasic.getString("app_name") + " - Failed to set thumbnail",
                        "The video has been uploaded, but the selected image file could not be used as " +
                                "thumbnail.\n\n" + e.getMessage() + onVideoWithName).show();
            } else if (e instanceof PlaylistNotFoundException) {
                AlertUtils.simpleClose(transBasic.getString("app_name") + " - Failed to add to playlist",
                        "The video has been uploaded, but it could not be added to the selected playlist, " +
                                "YouTube says the playlist could not be found." + onVideoWithName).show();
            } else if (e instanceof PlaylistFullException) {
                AlertUtils.simpleClose(transBasic.getString("app_name") + " - Failed to add to playlist",
                        "The video has been uploaded but it could not be added to the selected playlist, " +
                                "the playlist is full and no more videos can be added to it." + onVideoWithName).show();
            } else if (e instanceof OtherYouTubeException) {
                AlertUtils.exceptionDialog(transBasic.getString("app_name"),
                        "An error was returned from YouTube: ",
                        e);
            } else {
                AlertUtils.unhandledExceptionDialog(e);
            }
        }

        //Switch to a reset upload button instead of abort for the upload that threw the error
        if (video != null) {
            video.setProgressBarColor("red");
            video.setStatusLabelText(transUpload.getString("failed"));
        }
    }
}
