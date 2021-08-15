package io.github.stekeblad.videouploader.main;

import io.github.stekeblad.videouploader.ListControllers.UploadItemController;
import io.github.stekeblad.videouploader.extensions.jfx.IWindowController;
import io.github.stekeblad.videouploader.extensions.jfx.MyStage;
import io.github.stekeblad.videouploader.extensions.jfx.NoneSelectionModel;
import io.github.stekeblad.videouploader.extensions.jfx.stringConverters.VideoPresetStringConverter;
import io.github.stekeblad.videouploader.managers.CategoryManager;
import io.github.stekeblad.videouploader.managers.PlaylistManager;
import io.github.stekeblad.videouploader.managers.PresetManager;
import io.github.stekeblad.videouploader.managers.SettingsManager;
import io.github.stekeblad.videouploader.models.NewVideoPresetModel;
import io.github.stekeblad.videouploader.models.NewVideoUploadModel;
import io.github.stekeblad.videouploader.models.NewVideoUploadState;
import io.github.stekeblad.videouploader.utils.AlertUtils;
import io.github.stekeblad.videouploader.utils.Constants;
import io.github.stekeblad.videouploader.utils.FileUtils;
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
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.github.stekeblad.videouploader.utils.Constants.WindowPropertyNames;

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

    private Uploader uploader;
    private PresetApplicator presetApplicator;

    private Translations transMainWin;
    private Translations transBasic;

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

            String[] tasks = uploader.kill();
            //TODO: For each task, save that upload so it can be recreated next time program runs
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

                UploadItemController uploadController = new UploadItemController(newUpload, listUploads.prefWidthProperty());
                uploadController.registerStartUploadButtonActionHandler(this::onStartUpload);
                uploadController.registerDeleteButtonActionHandler(this::removeUploadFromList);
                uploadController.registerHideUploadButtonActionHandler(this::removeUploadFromList);
                uploadItems.add(uploadController);
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
     * Called when the pick files button is pressed.
     * Opens a file chooser and sets the list of selected files to the left of the button
     *
     * @param actionEvent the click event
     */
    public void onPickFileClicked(ActionEvent actionEvent) {
        chosen_files.setItems(FXCollections.observableArrayList(FileUtils.pickVideos(Long.MAX_VALUE)));
        actionEvent.consume();
    }

    /**
     * Called when the start all ready uploads button is clicked.
     * If a upload is allowed to be started (the start upload button is visible) then that button is clicked by this method.
     *
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
            if (uploadQueueVideo.getState() == NewVideoUploadState.SAVED) {
                uploadQueueVideo.startUpload(new ActionEvent());
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
        for (int i = 0; i < uploadItems.size(); i++) {
            var item = uploadItems.get(i);
            if (item.getState() == NewVideoUploadState.COMPLETED) {
                item.delete(new ActionEvent());
                uploadItems.remove(i);
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
        ButtonType userChoice = AlertUtils.yesNo(transMainWin.getString("diag_abortAll_short"),
                transMainWin.getString("diag_abortAll_full"), ButtonType.NO);
        if (userChoice == ButtonType.NO) {
            return;
        }
        abortUploads();
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
            abortUploads();
            while (!uploadItems.isEmpty()) {
                uploadItems.get(0).delete(new ActionEvent(), false);
            }
        }
        actionEvent.consume();
    }

    private void abortUploads() {
        // Abort the uploads in the reversed order of that they was most likely started in
        // to avoid that the program attempts to start a new upload that will also be aborted, and then the next one...
        for (int i = uploadItems.size() - 1; i >= 0; i--) {
            var item = uploadItems.get(i);
            if (item.getState() == NewVideoUploadState.UPLOADING)
                item.abort(new ActionEvent(), false);
        }
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
     * Registered as a callback to uploadModelControllers and called when a upload should be removed from the list
     *
     * @param upload The upload that need to be removed
     */
    private void removeUploadFromList(NewVideoUploadModel upload) {
        int index = findUploadIndexFromUniqueId(upload.getUniqueId());
        uploadItems.remove(index);
    }

    private int findUploadIndexFromUniqueId(String uuid) {
        return findUploadIndexFromUniqueId(UUID.fromString(uuid));
    }

    private int findUploadIndexFromUniqueId(UUID uuid) {
        for (int i = 0, uploadItemsSize = uploadItems.size(); i < uploadItemsSize; i++) {
            UploadItemController uploadItem = uploadItems.get(i);
            if (uploadItem.getUniqueModelId().equals(uuid)) {
                return i;
            }
        }
        return -1;
    }

    //TODO: I am new
    private void onStartUpload(NewVideoUploadModel upload) {
        // validation?

        // Queue upload
        // TODO: Ensure video is not null before adding!
        uploader.add(upload, upload.getUniqueId().toString());

        // Change buttons, make progressbar visible and set text to show it is waiting to be uploaded.
        upload.setStatusText(transBasic.getString("waiting"));
    }

    /**
     * Called when PresetApplicator successfully applied a preset to a video
     *
     * @param newUpload the newly created VideoUpload created from a File and a VideoPreset
     */
    private void onPresetApplicationSuccess(NewVideoUploadModel newUpload) {
        UploadItemController uploadController = new UploadItemController(newUpload, listUploads.widthProperty());
        uploadController.registerDeleteButtonActionHandler(this::removeUploadFromList);
        uploadController.registerStartUploadButtonActionHandler(this::onStartUpload);
        uploadController.registerHideUploadButtonActionHandler(this::removeUploadFromList);
        uploadItems.add(uploadController);
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
     *
     * @param id the unique model id of the upload
     */
    private void onUploadFinished(String id) {
        uploadItems.get(findUploadIndexFromUniqueId(id))
                .complete();
    }

    /**
     * Called if an upload errors/fails
     *
     * @param video the video that failed uploading
     * @param e     the exception that occurred
     */
    private void onUploadErred(@NotNull NewVideoUploadModel video, Throwable e) {
        String appName = transBasic.getString("app_name");
        String header = appName + " - Failed to upload video";
        if (e == null) {
            AlertUtils.simpleClose(header, "Failed to upload the video " + video.getVideoName()
                    + "\n\nMore detailed error information is not available").show();
            uploadItems.get(findUploadIndexFromUniqueId(video.getUniqueId())).fail();
            return;
        }

        String onVideoWithName = "\n\nThis happened on the video named " + video.getVideoName();
        if (e instanceof QuotaLimitExceededException) {
            // abort all scheduled uploads, they will all fail with this error
            for (String key : uploader.getUploadQueue()) {
                uploader.abortUpload((key));
                UploadItemController item = uploadItems.get(findUploadIndexFromUniqueId(key));
                item.abort(new ActionEvent(), false);
            }

            // Find when midnight in the pacific timezone is in the user's timezone
            String userClockAtPacificMidnight = TimeUtils.fromMidnightPacificToUserTimeZone();
            AlertUtils.simpleClose(header, appName + " has reached its daily upload limit" +
                    " in the YouTube API and can not continue the uploading. All scheduled uploads has been aborted.\n\n" +
                    "The limit will be reset at midnight Pacific Time. (" + userClockAtPacificMidnight + " in your timezone.)").show();
        } else if (e instanceof UploadLimitExceededException) {
            // abort all scheduled uploads, they will all fail with this error
            for (String key : uploader.getUploadQueue()) {
                UploadItemController item = uploadItems.get(findUploadIndexFromUniqueId(key));
                item.abort(new ActionEvent(), false);
            }
            AlertUtils.simpleClose(header, "You have reached your personal upload limit on YouTube" +
                    " and you can not upload more videos right now. All scheduled uploads has been aborted.\n\n" +
                    "Wait a few hours or retry again tomorrow").show();
        } else if (e instanceof InvalidVideoDetailsException) {
            AlertUtils.simpleClose(header, "YouTube reported a video property contains invalid content: " +
                    e.getMessage() + onVideoWithName).show();
            uploadItems.get(findUploadIndexFromUniqueId(video.getUniqueId())).fail();
        } else if (e instanceof InvalidMissingImageException) {
            AlertUtils.simpleClose(appName + " - Failed to set thumbnail",
                    "The video has been uploaded, but the selected image file could not be used as " +
                            "thumbnail. If you have selected a playlist to add this video to then this error " +
                            "happened before that could be done.\n\n" + e.getMessage() + onVideoWithName).show();
            uploadItems.get(findUploadIndexFromUniqueId(video.getUniqueId())).complete();
        } else if (e instanceof PlaylistNotFoundException) {
            AlertUtils.simpleClose(appName + " - Failed to add to playlist",
                    "The video has been uploaded, but it could not be added to the selected playlist. " +
                            "YouTube says the playlist could not be found." + onVideoWithName).show();
            uploadItems.get(findUploadIndexFromUniqueId(video.getUniqueId())).complete();
        } else if (e instanceof PlaylistFullException) {
            AlertUtils.simpleClose(appName + " - Failed to add to playlist",
                    "The video has been uploaded but it could not be added to the selected playlist, " +
                            "the playlist is full and no more videos can be added to it." + onVideoWithName).show();
            uploadItems.get(findUploadIndexFromUniqueId(video.getUniqueId())).complete();
        } else if (e instanceof OtherYouTubeException) {
            AlertUtils.exceptionDialog(appName,
                    "An error was returned from YouTube" + onVideoWithName,
                    e);
            uploadItems.get(findUploadIndexFromUniqueId(video.getUniqueId())).fail();
        } else {
            AlertUtils.unhandledExceptionDialog(e);
            uploadItems.get(findUploadIndexFromUniqueId(video.getUniqueId())).fail();
        }
    }
}
