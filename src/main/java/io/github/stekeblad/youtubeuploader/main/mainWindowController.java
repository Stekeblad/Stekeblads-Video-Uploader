package io.github.stekeblad.youtubeuploader.main;

import io.github.stekeblad.youtubeuploader.utils.AlertUtils;
import io.github.stekeblad.youtubeuploader.utils.ConfigManager;
import io.github.stekeblad.youtubeuploader.youtube.VideoInformationBase;
import io.github.stekeblad.youtubeuploader.youtube.VideoUpload;
import io.github.stekeblad.youtubeuploader.youtube.constants.Categories;
import io.github.stekeblad.youtubeuploader.youtube.constants.VisibilityStatus;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static io.github.stekeblad.youtubeuploader.youtube.VideoUpload.VIDEO_FILE_FORMAT;

public class mainWindowController implements Initializable {
    public Button buttDoThing;
    public ListView<GridPane> listView;
    public Button buttonPickFile;
    public Button buttonAddFile;
    public ListView<String> chosen_files;
    public Button btn_presets;
    public ChoiceBox<String> choice_presets;
    public AnchorPane mainWindowPane;
    public TextField text_autoNum;
    public Button btn_applyPreset;

    private ConfigManager configManager;
    private int uploadPaneCounter;
    private List<GridPane> uploadQueuePanes;
    private List<VideoUpload> uploadQueueVideos;
    private List<VideoUpload> inEditingVideos;
    private VideoInformationBase videoEdit;

    @FXML
    public void initialize(URL location, ResourceBundle resources) {

        uploadPaneCounter = 0;
        uploadQueuePanes = new ArrayList<>();
        uploadQueueVideos = new ArrayList<>();
        inEditingVideos = new ArrayList<>();
        configManager = ConfigManager.INSTANCE;
        configManager.configManager();
        if (configManager.getNoSettings()) {
            AlertUtils.simpleClose("No settings found", "Go to settings and add some").show();
            onSettingsPressed(new ActionEvent());
            configManager.setNoSettings(false);
            configManager.saveSettings();
        }
        choice_presets.setItems(FXCollections.observableArrayList(configManager.getPresetNames()));
        choice_presets.getSelectionModel().selectedItemProperty()
                .addListener( (ObservableValue<? extends String> observable, String oldValue, String newValue) ->
                        onPresetChanged(newValue)
                );

        text_autoNum.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                text_autoNum.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        String editorId = "editor";
        videoEdit = new VideoInformationBase("", "", VisibilityStatus.PUBLIC,
                new ArrayList<>(), "", Categories.SPEL, false, null, editorId);
        videoEdit.setEditable(true);
        GridPane videoEditPane = videoEdit.getPane();
        videoEditPane.setLayoutX(180);
        videoEditPane.setLayoutY(80);
        videoEditPane.setPrefSize(680, 130);
        mainWindowPane.getChildren().add(videoEditPane);

    }

    public void onDoThingClicked(ActionEvent event) {

        event.consume();
    }


    public void onPickFile(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose video files to upload");
        Stage fileChooserStage = new Stage();
        List<File> filesToUpload = fileChooser.showOpenMultipleDialog(fileChooserStage);
        if (filesToUpload != null) {
            List<String> filenames = new ArrayList<>();
            boolean fileWasSkipped = false;
            for (File aFilesToUpload : filesToUpload) {
                try { // Check file MIME to see if it is a video file
                    if(Files.probeContentType(Paths.get(aFilesToUpload.toURI())).startsWith(VIDEO_FILE_FORMAT)) {
                        String fileNameString = aFilesToUpload.getName();
                        filenames.add(fileNameString);
                    } else {
                        fileWasSkipped = true; // at leased one selected file is not a video file
                    }
                } catch (Exception e) {
                    fileWasSkipped = true;
                }
            }
            if(fileWasSkipped) {
                AlertUtils.simpleClose("Invalid files",
                        "One or more of the selected files was not added because they are not video files").show();
            }
            chosen_files.setItems(FXCollections.observableArrayList(filenames));
            if(chosen_files.getItems().size() > 0) {
                choice_presets.setDisable(false);
            }
        }

        actionEvent.consume();
    }

    private void onPresetChanged(String newValue) {
        text_autoNum.setDisable(false);
        btn_applyPreset.setDisable(false);

    }

    public void onApplyPresetClicked(ActionEvent actionEvent) {

    }

    public void onAddUploads(ActionEvent actionEvent) {
        if (configManager.getNeverAuthed()) {
            Optional<ButtonType> buttonChoice = AlertUtils.yesNo("Authentication Required", "To upload videos you must grant this application permission to access your Youtube channel. " +
                    "Do you want to allow \"Stekeblads Youtube Uploader\" permission to access Your channel?" +
                    "\n\nPermission overview: YOUTUBE_UPLOAD needed for uploading videos to your channel" +
                    "\nYOUTUBE for basic account access, needed for adding videos to playlists and setting thumbnails" +
                    "\n\nPress yes to open your browser for authentication or no to cancel")
                    .showAndWait();
            if (buttonChoice.isPresent()) {
                if (buttonChoice.get() == ButtonType.YES) {
                    configManager.setNeverAuthed(false);
                    configManager.saveSettings();
                } else { // ButtonType.NO or Closed [X]
                    return;
                }
            }


        }
        /*if(chosen_files.getItems().size() == 0) {
          actionEvent.consume();
            return;
        } /*
        GridPane newVideo = PaneFactory.makeUploadPane("vid" + uploadPaneCounter);
        ((TextField) newVideo.lookup("#vid" + uploadPaneCounter + "_title")).setText(nameOfFile.getText());
        uploadQueuePanes.add(newVideo);
        nameOfFile.setText("");
        listView.setItems(FXCollections.observableArrayList(uploadQueuePanes));
        uploadPaneCounter++;
*/        actionEvent.consume();

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
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        actionEvent.consume();
    }
}
