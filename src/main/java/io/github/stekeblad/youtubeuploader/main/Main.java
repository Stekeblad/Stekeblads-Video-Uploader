package io.github.stekeblad.youtubeuploader.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("mainWindow.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Stekeblads Youtube Uploader");
        primaryStage.setScene(new Scene(root, 900, 825));
        mainWindowController controller = loader.getController();
        primaryStage.setOnCloseRequest(event -> {
            if(! controller.onWindowClose()) {
                event.consume();
            }
        });
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
