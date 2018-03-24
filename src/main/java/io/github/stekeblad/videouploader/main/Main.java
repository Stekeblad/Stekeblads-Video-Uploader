package io.github.stekeblad.videouploader.main;

import io.github.stekeblad.videouploader.utils.Translations;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * The program starts here, opens MainWindow and waits for all windows to close
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("mainWindow.fxml"));
        Parent root = loader.load();
        Translations trans = new Translations("baseStrings");
        primaryStage.setTitle(trans.getString("app_name"));
        primaryStage.setScene(new Scene(root, 900, 825));
        // Register MainWindowController.onWindowClose() to be called when the close button is clicked
        mainWindowController controller = loader.getController();
        primaryStage.setOnCloseRequest(event -> {
            if(! controller.onWindowClose()) {
                // Close or not close based on return value
                event.consume();
            }
        });
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
