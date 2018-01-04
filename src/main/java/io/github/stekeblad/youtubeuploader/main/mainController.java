package io.github.stekeblad.youtubeuploader.main;

import io.github.lilahamstern.AlertBox;
import io.github.lilahamstern.ConfirmBox;
import io.github.stekeblad.youtubeuploader.utils.ConfigManager;
import io.github.stekeblad.youtubeuploader.utils.PresetManager;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class mainController implements Initializable {
    public Button buttDoThing;
    public ListView<Pane> listView;
    public Button buttonPickFile;
    public Button buttonAddFile;
    public ListView<String> chosen_files;
    public Button btn_presets;
    public ChoiceBox<String> choice_presets;
    public AnchorPane mainWindowPane;

    private ConfigManager configManager;
    private PresetManager presetManager;
    private int videoPaneCounter;
    private List<Pane> videoPanes;

    @FXML
    public void initialize(URL location, ResourceBundle resources) {

        videoPaneCounter = 0;
        videoPanes = new ArrayList<>();
        configManager = ConfigManager.INSTANCE;
        configManager.configManager();
        if (configManager.getNoSettings()) {
            AlertBox.display("No settings found", "Go to settings and add some");
            onSettingsPressed(new ActionEvent());
            configManager.setNoSettings(false);
            configManager.saveSettings();
        }
        presetManager = PresetManager.INSTANCE;
        choice_presets.setItems(FXCollections.observableArrayList(presetManager.getPresetNames()));
        choice_presets.getSelectionModel().selectedItemProperty()
                .addListener( (ObservableValue<? extends String> observable, String oldValue, String newValue) ->
                        System.out.println(newValue)
                );
    }

    public void onDoThingClicked(ActionEvent event) {

        event.consume();
    }


    public void onPickFile(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose a video file to upload");
        Stage fileChooserStage = new Stage();
        List<File> filesToUpload = fileChooser.showOpenMultipleDialog(fileChooserStage);
        if (filesToUpload != null) {

            List<String> filenames = new ArrayList<>();
            for (int i = 0; i < filesToUpload.size(); i++) {
                String fileNameString = filesToUpload.get(i).getName();
                filenames.add(fileNameString);
            }
            chosen_files.setItems(FXCollections.observableArrayList(filenames));
        }

        actionEvent.consume();
    }

    public void onAddUploads(ActionEvent actionEvent) {
        if (configManager.getNeverAuthed()) {
            if (ConfirmBox.display("Authentication Required", "To upload videos you must grant this application permission to access your Youtube channel. " +
                    "Do you want to allow \"Stekeblads Youtube Uploader\" permission to access Your channel?" +
                    "\n\nPermission overview: YOUTUBE_UPLOAD needed for uploading videos to your channel" +
                    "\nYOUTUBE for basic account access, needed for adding videos to playlists and setting thumbnails" +
                    "\n\nPress yes to open your browser for authentication or no to cancel")) {
                configManager.setNeverAuthed(false);
                configManager.saveSettings();
            } else {
                actionEvent.consume();
                return;
            }
        }
        /*if(chosen_files.getItems().size() == 0) {
          actionEvent.consume();
            return;
        } /*
        GridPane newVideo = PaneFactory.makeUploadPane("vid" + videoPaneCounter);
        ((TextField) newVideo.lookup("#vid" + videoPaneCounter + "_title")).setText(nameOfFile.getText());
        videoPanes.add(newVideo);
        nameOfFile.setText("");
        listView.setItems(FXCollections.observableArrayList(videoPanes));
        videoPaneCounter++;
*/        actionEvent.consume();

    }

    public void onSettingsPressed(ActionEvent actionEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(mainController.class.getClassLoader().getResource("fxml/SettingsWindow.fxml"));
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
