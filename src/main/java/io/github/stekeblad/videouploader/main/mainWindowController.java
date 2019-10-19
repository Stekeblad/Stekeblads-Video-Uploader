package io.github.stekeblad.videouploader.main;

import io.github.stekeblad.videouploader.jfxExtension.MyStage;
import io.github.stekeblad.videouploader.tagProcessing.ITagProcessor;
import io.github.stekeblad.videouploader.utils.AlertUtils;
import io.github.stekeblad.videouploader.utils.ConfigManager;
import io.github.stekeblad.videouploader.utils.Constants;
import io.github.stekeblad.videouploader.utils.FileUtils;
import io.github.stekeblad.videouploader.utils.background.OpenInBrowser;
import io.github.stekeblad.videouploader.utils.state.ButtonProperties;
import io.github.stekeblad.videouploader.utils.state.VideoUploadState;
import io.github.stekeblad.videouploader.utils.translation.TranslationBundles;
import io.github.stekeblad.videouploader.utils.translation.Translations;
import io.github.stekeblad.videouploader.utils.translation.TranslationsManager;
import io.github.stekeblad.videouploader.windowControllers.PresetsWindowController;
import io.github.stekeblad.videouploader.youtube.Uploader;
import io.github.stekeblad.videouploader.youtube.VideoPreset;
import io.github.stekeblad.videouploader.youtube.VideoUpload;
import io.github.stekeblad.videouploader.youtube.utils.CategoryUtils;
import io.github.stekeblad.videouploader.youtube.utils.PlaylistUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jcodec.containers.mp4.boxes.MetaValue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import static io.github.stekeblad.videouploader.utils.Constants.*;
import static io.github.stekeblad.videouploader.youtube.VideoInformationBase.MAX_THUMB_SIZE;
import static io.github.stekeblad.videouploader.youtube.VideoInformationBase.THUMBNAIL_FILE_FORMAT;
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
    private VideoUploadState buttonStates;

    private Translations transMainWin;
    private Translations transBasic;
    private Translations transUpload;

    /**
     * Initialize things when the window is opened, used instead of initialize as that one does not have access to the scene
     */
    public void myInit() {
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

        uploadPaneCounter = 0;
        uploadQueueVideos = new ArrayList<>();
        editBackups = new HashMap<>();
        configManager = ConfigManager.INSTANCE;
        // configManager.configManager(); Done in Main.java
        playlistUtils = PlaylistUtils.INSTANCE;
        playlistUtils.loadCache();
        categoryUtils = CategoryUtils.INSTANCE;
        categoryUtils.loadCategories();

        // Populate presets dropdown/choice box
        ArrayList<String> presetNames = configManager.getPresetNames();
        if (presetNames == null) {
            presetNames = new ArrayList<>();
            presetNames.add(transBasic.getString("noSelected"));
        } else {
            presetNames.add(0, transBasic.getString("noSelected"));
        }
        choice_presets.setItems(FXCollections.observableArrayList(presetNames));
        choice_presets.getSelectionModel().select(0);

        // Only allow numbers in autoNum textField
        txt_autoNum.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txt_autoNum.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        uploader.setUploadFinishedCallback(s -> Platform.runLater(() -> onUploadFinished(s)));
        uploader.setUploadErredCallback((videoUpload, throwable) -> Platform.runLater(() -> onUploadErred(videoUpload, throwable)));

        // Set up button sets for the different states a upload can be in: editing, locked, uploading, failed/erred
        defineUploadStates();

        // If any uploads was saved when the program was closed last time
        if(configManager.hasWaitingUploads()) {
            ArrayList<String> waitingUploads = configManager.getWaitingUploads();
            if (waitingUploads != null) {
                for(String waitingUpload : waitingUploads) {
                    try {
                        VideoUpload loadedUpload = new VideoUpload(waitingUpload, String.valueOf(uploadPaneCounter++));
                        loadedUpload.setThumbnailCursorEventHandler(this::updateCursor);
                        buttonStates.setLocked(loadedUpload);

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
        videosToAdd = FileUtils.pickVideos(Long.MAX_VALUE);
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
        if (choice_presets.getSelectionModel().getSelectedIndex() < 1) {
            // No preset, add videos to upload list with file name as title and blank/default values on the rest
            for(File videoFile : videosToAdd) {
                VideoUpload newUpload = new VideoUpload(videoFile.getName(), null, null,
                        null, null, null, false,
                        null, UPLOAD_PANE_ID_PREFIX + uploadPaneCounter, videoFile);

                newUpload.setThumbnailCursorEventHandler(this::updateCursor);
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
                uploadQueueVideos.get(uploadQueueVideos.size() - 1).setButton2(deleteButton);

                uploadPaneCounter++;
            }
        } else { // preset selected

            // Load details of the selected preset
            VideoPreset chosenPreset;
            try {
                chosenPreset = new VideoPreset(configManager.getPresetString(
                        choice_presets.getSelectionModel().getSelectedItem()), "preset");
            } catch (Exception e) {
                AlertUtils.simpleClose("Preset error", "Cant read preset \"" +
                        choice_presets.getSelectionModel().getSelectedItem() + "\", the videos will not be added");
                return;
            }

            // Get the auto numbering and preset
            int autoNum = Integer.valueOf(txt_autoNum.getText());

            // Find TagProcessors
            ArrayList<ITagProcessor> tagProcessors = new ArrayList<>();
            ServiceLoader<ITagProcessor> tagProcessorServiceLoader = ServiceLoader.load(ITagProcessor.class);
//            for (ITagProcessor tagProcessor : tagProcessorServiceLoader) {
//                tagProcessor.init(chosenPreset, autoNum);
//                tagProcessors.add(tagProcessor);
//            }

            tagProcessorServiceLoader.forEach(tagProcessor -> {
                tagProcessor.init(chosenPreset, autoNum);
                tagProcessors.add(tagProcessor);
            });

            // Iterate over all selected video files
            for (File videoFile : videosToAdd) {
                // Insert raw file name in title, exclude file extension
                // (may be a TagProcessor later, do not want to pass the File to all TagProcessors)
                String name = chosenPreset.getVideoName();
                if (name.contains("$(rawname)")) {
                    String rawFileName = videoFile.getName().substring(0, videoFile.getName().lastIndexOf("."));
                    name = name.replace("$(rawname)", rawFileName);
                }


                // Debug: print all metadata to the console
                try {
                    org.jcodec.movtool.MetadataEditor f = org.jcodec.movtool.MetadataEditor.createFrom(videoFile);
                    Map<String, org.jcodec.containers.mp4.boxes.MetaValue> m = f.getKeyedMeta();
                    m.forEach((s, metaValue) -> System.out.println(s + " contains " + metaValue.toString()));
                    Map<Integer, MetaValue> n = f.getItunesMeta();
                    n.forEach((integer, metaValue) -> System.out.println(fourccToString(integer) + " contains " + metaValue.toString()));

                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }



                // Execute the TagProcessors
                String description = chosenPreset.getVideoDescription();
                List<String> videoTags = chosenPreset.getVideoTags();
                for (ITagProcessor processor : tagProcessors) {
                    name = processor.processTitle(name);
                    description = processor.processDescription(description);
                    videoTags = processor.processTags(videoTags);
                }

                VideoUpload.Builder newUploadBuilder = new VideoUpload.Builder()
                        .setVideoName(name)
                        .setVideoDescription(description)
                        .setVisibility(chosenPreset.getVisibility())
                        .setVideoTags(videoTags)
                        .setSelectedPlaylist(chosenPreset.getSelectedPlaylist())
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
                newUpload.setThumbnailCursorEventHandler(this::updateCursor);

                transUpload.autoTranslate(newUpload.getPane(), newUpload.getPaneId());
                buttonStates.setLocked(newUpload);
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
            Stage stage = new Stage();
            stage.setTitle(transBasic.getString("app_presetWindowTitle"));
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL); // Make it always above mainWindow
            PresetsWindowController controller = fxmlLoader.getController();
            stage.setOnCloseRequest(controller::onWindowClose);
            controller.myInit(!editBackups.isEmpty() || uploader.getIsActive());
            stage.showAndWait();
        } catch (IOException e) {
            AlertUtils.exceptionDialog(transBasic.getString("error"), transBasic.getString("errOpenWindow"), e);
        }
        actionEvent.consume();
        // Update presets choice box in case presets was added or remove
        ArrayList<String> presetNames = configManager.getPresetNames();
        if (presetNames == null) {
            presetNames = new ArrayList<>();
            presetNames.add(transBasic.getString("noSelected"));
        } else {
            presetNames.add(0, transBasic.getString("noSelected"));
        }
        choice_presets.setItems(FXCollections.observableArrayList(presetNames));
        choice_presets.getSelectionModel().select(0);
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
            MyStage stage = new MyStage(ConfigManager.WindowPropertyNames.SETTINGS);
            stage.makeScene(fxmlLoader.load(), Constants.SETTINGS_WINDOW_DIMENSIONS_RESTRICTION);
            stage.setTitle(transBasic.getString("app_settingsWindowTitle"));
            stage.initModality(Modality.APPLICATION_MODAL); // Make it always above mainWindow
            stage.prepareControllerAndShow(fxmlLoader.getController());
        } catch (IOException e) {
            AlertUtils.exceptionDialog(transBasic.getString("error"), transBasic.getString("errOpenWindow"), e);
        }
//        try {
//            FXMLLoader fxmlLoader = new FXMLLoader();
//            fxmlLoader.setLocation(mainWindowController.class.getClassLoader().getResource("fxml/SettingsWindow.fxml"));
//            Scene scene = new Scene(fxmlLoader.load(), 600, 450);
//            Stage stage = new Stage();
//            stage.setTitle(transBasic.getString("app_settingsWindowTitle"));
//            stage.setScene(scene);
//            stage.initModality(Modality.APPLICATION_MODAL); // Make it always above mainWindow
//            SettingsWindowController controller = fxmlLoader.getController();
//            stage.setOnCloseRequest(controller::onWindowClose);
//            controller.myInit();
//            stage.show();
//        } catch (IOException e) {
//            AlertUtils.exceptionDialog(transBasic.getString("error"), transBasic.getString("errOpenWindow"), e);
//        }
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
            ButtonType userChoice = AlertUtils.yesNo(transBasic.getString("auth_short"),
                    transBasic.getString("auth_full"), ButtonType.NO);
            if (userChoice == ButtonType.YES) {
                configManager.setNeverAuthed(false);
                configManager.saveSettings();
            } else { // ButtonType.NO or closed [X]
                return;
            }
        }
        // Permission given, start uploads
        for (VideoUpload uploadQueueVideo : uploadQueueVideos) {
            if (uploadQueueVideo.getButton3Id() != null &&
                    uploadQueueVideo.getButton3Id().contains(BUTTON_START_UPLOAD)) {
                onStartUpload(uploadQueueVideo.getButton3Id());
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
            uploadQueueVideos.clear();
            uploadPaneCounter = 0;
            updateUploadList();
        }
        actionEvent.consume();
    }

    /**
     * Defines the different states for buttonStates, this is made to make it much simpler to manage the buttons
     * for the different states a upload can be in. Previously buttons was created all over the place and set
     * to call methods. Now there is sets of buttons defined here and this sets is then used when setting the
     * buttons to show. Each old button setting could be towards 15 lines long, now its 1 line and easier to manage.
     */
    private void defineUploadStates() {
        buttonStates = new VideoUploadState();

        // Define Locked from editing
        buttonStates.defineLocked(new ButtonProperties[]{
                new ButtonProperties(BUTTON_EDIT, transBasic.getString("edit"), this::onEdit),
                new ButtonProperties(BUTTON_DELETE, transBasic.getString("delete"), this::onDelete),
                new ButtonProperties(BUTTON_START_UPLOAD, transBasic.getString("startUpload"), this::onStartUpload)
        });

        // Define Editing
        buttonStates.defineEditing(new ButtonProperties[]{
                new ButtonProperties(BUTTON_SAVE, transBasic.getString("save"), this::onSave),
                new ButtonProperties(BUTTON_CANCEL, transBasic.getString("cancel"), this::onCancel),
                new ButtonProperties("_ghost", "", null)
        });

        // Define Uploading
        buttonStates.defineUploading(new ButtonProperties[]{
                new ButtonProperties("_ghost1", "Just to give it width", null),
                new ButtonProperties(BUTTON_ABORT_UPLOAD, transBasic.getString("abort"), this::onAbort),
                new ButtonProperties("_ghost", "", null)
        });


        // Define Failed/Erred
        buttonStates.defineFailed(new ButtonProperties[]{
                new ButtonProperties("_ghost1", "Just to give it width", null),
                new ButtonProperties(BUTTON_RESET, transBasic.getString("reset"), this::onResetUpload),
                new ButtonProperties("_ghost", "", null)
        });
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
     * Set this method to trigger when the cursor enters or leaves a node to change how it looks.
     *
     * @param entered if true, sets the cursor to a pointing hand (usually on enter event).
     *                if false, sets the cursor to default (usually on exit event).
     */
    private void updateCursor(boolean entered) {
        if (entered) {
            mainWindowPane.getScene().setCursor(Cursor.HAND);
        } else {
            mainWindowPane.getScene().setCursor(Cursor.DEFAULT);
        }
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

        // Set on thumbnail clicked
        uploadQueueVideos.get(selected).setEditable(true);
        uploadQueueVideos.get(selected).setOnThumbnailClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) return; // Conflicting with context menu
            File pickedThumbnail = FileUtils.pickThumbnail(THUMBNAIL_FILE_FORMAT, MAX_THUMB_SIZE);
            if(pickedThumbnail != null) {
                try {
                    uploadQueueVideos.get(selected).setThumbNailFile(pickedThumbnail);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        // Sets the thumbnail right click context menu
        ContextMenu thumbnailRClickMenu = new ContextMenu();
        MenuItem item1 = new MenuItem(transBasic.getString("resetToDefault"));
        item1.setOnAction(actionEvent -> {
            try {
                uploadQueueVideos.get(selected).setThumbNailFile(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            actionEvent.consume();
        });
        thumbnailRClickMenu.getItems().add(item1);
        uploadQueueVideos.get(selected).setThumbnailContextMenu(thumbnailRClickMenu);

        buttonStates.setEditing(uploadQueueVideos.get(selected));
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
        // Make sure a category is selected (is initially null and can be set to null if categories has been re-localized)
        if (uploadQueueVideos.get(selected).getCategory() == null) {
            AlertUtils.simpleClose(transBasic.getString("diag_invalidCategory_short"),
                    transBasic.getString("diag_invalidCategory_full")).show();
            return;
        }
        // Check if categories has been re-localized and list is no longer correct
        if (!categoryUtils.getCategoryNames().contains(uploadQueueVideos.get(selected).getCategory())) {
            AlertUtils.simpleClose(transBasic.getString("diag_categoryRemoved_short"),
                    transBasic.getString("diag_categoryRemoved_full")).show();
            return;
        }

        uploadQueueVideos.get(selected).setEditable(false);
        buttonStates.setLocked(uploadQueueVideos.get(selected));
        // Delete backup if there is one
        editBackups.remove(uploadQueueVideos.get(selected).getPaneId());
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

        buttonStates.setLocked(uploadQueueVideos.get(selected));
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

        ButtonType userChoice = AlertUtils.yesNo(transMainWin.getString("diag_confirmDelete_short"),
                desc, ButtonType.NO);
        if (userChoice == ButtonType.YES) {
            // delete backup (may exist if upload was created with no preset and directly deleted
            editBackups.remove(uploadQueueVideos.get(selected).getPaneId());
            uploadQueueVideos.remove(selected);
            updateUploadList();
        } // else if ButtonType.NO or closed [X] do nothing

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
            return;
        }
        if (categoryUtils.getCategoryId(uploadQueueVideos.get(selected).getCategory()).equals("-1")) {
            AlertUtils.simpleClose(transMainWin.getString("diag_noStartUpload_short"),
                    transMainWin.getString("diag_noStartUpload_full_noCategory")).show();
            return;
        }

        // If the user has not given the program permission to access their youtube channel, ask the user to do so.
        if(configManager.getNeverAuthed()) {
            ButtonType userChoice = AlertUtils.yesNo(transBasic.getString("auth_short"),
                    transBasic.getString("auth_full"), ButtonType.NO);
            if (userChoice == ButtonType.YES) {
                configManager.setNeverAuthed(false);
                configManager.saveSettings();
            } else { // ButtonType.NO or closed [X]
                return;
            }

        }
        // User is authenticated or is warned about the upcoming prompt to do so.

        // Queue upload
        uploader.add(uploadQueueVideos.get(selected), uploadQueueVideos.get(selected).getPaneId());

        // Change buttons, make progressbar visible and set text to show it is waiting to be uploaded.
        buttonStates.setUploading(uploadQueueVideos.get(selected));
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
            uploadQueueVideos.get(selected).setProgressBarProgress(INDETERMINATE_PROGRESS); // reset progBar to be animated
            uploadQueueVideos.get(selected).setStatusLabelText(transBasic.getString("aborted"));
        } else {
            AlertUtils.simpleClose(transBasic.getString("error"), "Failed to terminate upload for unknown reason").show();
        }

        buttonStates.setLocked(uploadQueueVideos.get(selected));
        // Make sure visual change get to the UI
        updateUploadList();
    }

    /**
     * Called when Reset upload button is clicked (appear when an upload failed)
     *
     * @param callerId the id of the upload + button name
     */
    private void onResetUpload(String callerId) {
        String parentId = callerId.substring(0, callerId.indexOf('_'));
        int selected = getUploadIndexByName(parentId);
        if (selected == -1) {
            System.err.println("reset upload button belongs to a invalid or non-existing parent");
            return;
        }
        // Change back progressBar color, hide it and set the locked state buttons
        uploadQueueVideos.get(selected).setProgressBarColor(null);
        uploadQueueVideos.get(selected).setProgressBarVisibility(false);
        uploadQueueVideos.get(selected).setStatusLabelText(transUpload.getString("_status"));
        buttonStates.setLocked(uploadQueueVideos.get(selected));

        // Make sure visual change get to the UI
        updateUploadList();
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
        if (e != null && video != null) {
            AlertUtils.exceptionDialog(header, "Failed to upload the video \"" + video.getVideoName() + "\"", e);
        } else {
            AlertUtils.simpleClose(header, "For an unknown reason is error information not available").show();
        }
        //Switch to a reset upload button instead of abort
        if (video != null) {
            video.setProgressBarColor("red");
            video.setStatusLabelText(transUpload.getString("failed"));
            buttonStates.setFailed(video);
            updateUploadList();
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
            System.err.println("remove finished upload, button belongs to a invalid or non-existing parent");
            return;
        }
        uploadQueueVideos.remove(selected);
        editBackups.remove(uploadQueueVideos.get(selected).getPaneId());
        updateUploadList();
    }

    // Taken from https://github.com/jcodec/jcodec/blob/155e0106850381a087f7359325777e3ae190e9e8/src/main/java/org/jcodec/movtool/MetadataEditorMain.java#L201
    private String fourccToString(int key) {
        byte[] bytes = new byte[4];
        java.nio.ByteBuffer.wrap(bytes).order(java.nio.ByteOrder.BIG_ENDIAN).putInt(key);
        return org.jcodec.platform.Platform.stringFromCharset(bytes, org.jcodec.platform.Platform.ISO8859_1);
    }
}
