package io.github.stekeblad.youtubeuploader.main;

import io.github.stekeblad.youtubeuploader.fxml.PaneFactory;
import io.github.stekeblad.youtubeuploader.utils.ConfigManager;
import io.github.stekeblad.youtubeuploader.youtube.Uploader;
import io.github.stekeblad.youtubeuploader.youtube.constants.Categories;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import static io.github.stekeblad.youtubeuploader.utils.Constants.DATA_DIR;

public class mainController implements Initializable {
    public Button buttDoThing;
    public ListView<Pane> listView;
    public GridPane videoGridPane;
    public Button buttonPickFile;
    public Button buttonAddFile;
    public ListView<Pane> chosen_files;
    public TextField txt_common_title;
    public TextArea txt_common_description;
    public TextField txtStartEpisode;
    public TextArea txtTags;
    public TextField txt_playlistURL;
    public Button btn_presets;
    public ChoiceBox choice_presets;

    private ConfigManager configManager;
    private int videoPaneCounter;
    private List<Pane> videoPanes;

    public mainController() {
        configManager = new ConfigManager();
    }

    @FXML
    public void initialize(URL location, ResourceBundle resources) {

        videoPaneCounter = 0;
        videoPanes = new ArrayList<>();
        configManager = new ConfigManager();
        if (configManager.getNoSettings()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No settings found");
            alert.setHeaderText(null);
            alert.setContentText("Go to settings and add some");
            alert.showAndWait();
            onSettingsPressed(new ActionEvent());
            configManager.setNoSettings(false);
            configManager.saveSettings();
        }

        txtStartEpisode.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtStartEpisode.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    public void onDoThingClicked(ActionEvent event) {


        //videoPanes.add(videoGridPane);
        videoPanes.add(PaneFactory.makeVideoDetailsPane("pane1"));
        videoPanes.add(PaneFactory.makeUploadPane("pane2"));
        videoPanes.add(PaneFactory.makePresetPane("pane3"));
        listView.setItems(FXCollections.observableArrayList(videoPanes));




        try {
            List<String> tags = Collections.singletonList("example tag");
            InputStream videoStream = new FileInputStream(DATA_DIR + "/myNewVideo.mkv");
            Uploader newUpload = new Uploader.Builder()
                    .setVideoName("My new Video")
                    .setVideoDescription("This video was uploaded using Stekeblad Youtube Uploader!")
                    .setVisibility("private")
                    .setVideoTags(tags)
                    .setVideoFile(videoStream)
                    .setCategory(Categories.KOMEDI)
                    .setTellSubs(false)
                    .build();

            //newUpload.uploadToTheTube();



        } catch (IOException e) {
            e.printStackTrace();
        }

       event.consume();
    }


    public void onPickFile(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose a video file to upload");
        Stage fileChooserStage = new Stage();
        List<File> filesToUpload = fileChooser.showOpenMultipleDialog(fileChooserStage);
        if (filesToUpload != null) {

            List<Pane> filenames = new ArrayList<>();
            for (int i = 0; i < filesToUpload.size(); i++) {
                Pane fileNamePane = PaneFactory.makeSingleLabelPane("file" + i);
                ((Label) fileNamePane.getChildren().get(0)).setText(filesToUpload.get(i).getName());
                filenames.add(fileNamePane);
            }
            chosen_files.setItems(FXCollections.observableArrayList(filenames));
        }

        actionEvent.consume();
    }

    public void onAddUploads(ActionEvent actionEvent) {
        /*if(chosen_files.getItems().size() == 0) {
            return;
        }
        GridPane newVideo = PaneFactory.makeUploadPane("vid" + videoPaneCounter);
        ((TextField) newVideo.lookup("#vid" + videoPaneCounter + "_title")).setText(nameOfFile.getText());
        videoPanes.add(newVideo);
        nameOfFile.setText("");
        listView.setItems(FXCollections.observableArrayList(videoPanes));
        videoPaneCounter++;
        actionEvent.consume();
*/
    }

    public void onSettingsPressed(ActionEvent actionEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(mainController.class.getClassLoader().getResource("fxml/SettingsWindow.fxml"));
       // System.out.println(ClassLoader.getSystemResource("fxml/SettingsWindow.fxml"));
            /*
             * if "fx:controller" is not set in fxml
             * fxmlLoader.setController(NewWindowController);
             */
            Scene scene = new Scene(fxmlLoader.load(), 550, 600);
            Stage stage = new Stage();
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
