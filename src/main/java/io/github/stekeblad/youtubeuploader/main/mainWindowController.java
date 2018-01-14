package io.github.stekeblad.youtubeuploader.main;

import io.github.stekeblad.youtubeuploader.utils.AlertUtils;
import io.github.stekeblad.youtubeuploader.utils.ConfigManager;
import io.github.stekeblad.youtubeuploader.utils.PickFile;
import io.github.stekeblad.youtubeuploader.youtube.PlaylistUtils;
import io.github.stekeblad.youtubeuploader.youtube.VideoUpload;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static io.github.stekeblad.youtubeuploader.utils.Constants.*;
import static io.github.stekeblad.youtubeuploader.youtube.VideoInformationBase.NODE_ID_PLAYLIST;
import static io.github.stekeblad.youtubeuploader.youtube.VideoInformationBase.NODE_ID_THUMBNAIL;

public class mainWindowController implements Initializable {
    public ListView<GridPane> listView;
    public Button buttonPickFile;
    public ListView<String> chosen_files;
    public Button btn_presets;
    public ChoiceBox<String> choice_presets;
    public AnchorPane mainWindowPane;
    public TextField text_autoNum;
    public Button btn_applyPreset;
    public Button buttonStartAll;

    private ConfigManager configManager;
    private PlaylistUtils playlistUtils;
    private int uploadPaneCounter = 0;
    private List<VideoUpload> uploadQueueVideos;
    private List<File> videosToAdd;

    private static final String UPLOAD_PANE_ID_PREFIX = "upload-";

    @FXML
    public void initialize(URL location, ResourceBundle resources) {

        uploadPaneCounter = 0;
        //uploadQueuePanes = new ArrayList<>();
        uploadQueueVideos = new ArrayList<>();
        configManager = ConfigManager.INSTANCE;
        configManager.configManager();
        if (configManager.getNoSettings()) {
            AlertUtils.simpleClose("No settings found", "Go to settings and add some").show();
            onSettingsPressed(new ActionEvent());
            configManager.setNoSettings(false);
            configManager.saveSettings();
        }
        playlistUtils = PlaylistUtils.INSTANCE;
        choice_presets.setItems(FXCollections.observableArrayList(configManager.getPresetNames()));

        text_autoNum.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                text_autoNum.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }


    public void onPickFile(ActionEvent actionEvent) {
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

    public void onApplyPresetClicked(ActionEvent actionEvent) {
        if(videosToAdd == null || videosToAdd.size() == 0 ) {
            AlertUtils.simpleClose("No files selected", "Please select files to upload").show();
            return;
        }
        if(choice_presets.getSelectionModel().getSelectedIndex() == -1) {
            // add videos to upload list with file name as title and blank/default values on the rest
            for(File videoFile : videosToAdd) {
                VideoUpload newUpload = new VideoUpload(videoFile.getName(), null, null,
                        null, null, null, false, null,
                        UPLOAD_PANE_ID_PREFIX + uploadPaneCounter, videoFile);
                onEdit(UPLOAD_PANE_ID_PREFIX + uploadPaneCounter + "_fakeButton");
                uploadQueueVideos.add(newUpload);
                uploadPaneCounter++;
            }
        }
        videosToAdd = null;
        chosen_files.setItems(FXCollections.observableArrayList(new ArrayList<>()));
        updateUploadList();
        actionEvent.consume();
    }

    public void onStartAll(ActionEvent actionEvent) {
        actionEvent.consume();
    }

    public void onSettingsPressed(ActionEvent actionEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(mainWindowController.class.getClassLoader().getResource("fxml/PresetsWindow.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 725, 700);
            Stage stage = new Stage();
            stage.setMinWidth(725);
            stage.setMinHeight(550);
            stage.setTitle("Settings - Stekeblads Youtube Uploader");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
        actionEvent.consume();
        // Update presets choice box in case presets was added or remove
        choice_presets.setItems(FXCollections.observableArrayList(configManager.getPresetNames()));
    }

    private void updateUploadList() {
        List<GridPane> uploadQueuePanes = new ArrayList<>();
        for(VideoUpload vid : uploadQueueVideos) {
            uploadQueuePanes.add(vid.getUploadPane());
        }
        listView.setItems(FXCollections.observableArrayList(uploadQueuePanes));
    }

    private int getUploadIndexByname(String nameToTest, int skipIndex) {
        int videoIndex = -1;
        for(int i = 0; i < uploadQueueVideos.size(); i++) {
            if (i == skipIndex) {
                continue;
            }
            if(uploadQueueVideos.get(i).getPaneId().equals(nameToTest)) {
                videoIndex = i;
                break;
            }
        }
        return videoIndex;
    }

    private void onEdit(String calledId) {
        String parentId = calledId.substring(0, calledId.indexOf('_'));
        int selected = getUploadIndexByname(parentId, -1);
        if (selected == -1) {
            System.err.println("Non-existing button was pressed!");
            return;
        }
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
        uploadQueueVideos.get(selected).getPane().lookup("#" + parentId + NODE_ID_PLAYLIST).setOnMouseClicked(event ->
                ((ChoiceBox<String>) uploadQueueVideos.get(selected).getPane().lookup("#" + parentId + NODE_ID_PLAYLIST)).setItems(
                        FXCollections.observableArrayList(playlistUtils.getUserPlaylistNames())));
        // Set buttons
        Button saveButton = new Button("Save");
        saveButton.setId(parentId + BUTTON_SAVE);
        saveButton.setOnMouseClicked(event -> onSave(saveButton.getId()));
        Button cancelButton = new Button("Cancel");
        cancelButton.setId(parentId + BUTTON_CANCEL);
        cancelButton.setOnMouseClicked(event -> onCancel(cancelButton.getId()));
        //Button three is not used but I do not want it to be invisible
        Button ghostButton = new Button("");
        ghostButton.setVisible(false);

        uploadQueueVideos.get(selected).setButton1(cancelButton);
        uploadQueueVideos.get(selected).setButton2(saveButton);
        uploadQueueVideos.get(selected).setButton3(ghostButton);

        // Make sure visual change get to the UI
        updateUploadList();
    }

    private void onSave(String calledId) {
        String parentId = calledId.substring(0, calledId.indexOf('_'));
        int selected = getUploadIndexByname(parentId, -1);
        if (selected == -1) {
            System.err.println("Non-existing button was pressed!");
            return;
        }
        // Check fields
        if(uploadQueueVideos.get(selected).getVideoName().equals("")) {
            AlertUtils.simpleClose("Title Required", "Video does not have a title").show();
            return;
        }
        // Everything else is not required or given a default value that can not be set to a invalid value

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

    private void onCancel(String calledId) {

    }

    private void onDelete(String calledId) {

    }

    private void onStartUpload(String calledId) {

    }

    private void onAbort(String calledId) {

    }
}
