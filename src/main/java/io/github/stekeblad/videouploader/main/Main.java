package io.github.stekeblad.videouploader.main;

import io.github.stekeblad.videouploader.utils.AlertUtils;
import io.github.stekeblad.videouploader.utils.ConfigManager;
import io.github.stekeblad.videouploader.utils.Translations;
import io.github.stekeblad.videouploader.utils.TranslationsManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Locale;

/**
 * The program starts here, opens MainWindow and waits for all windows to close
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("mainWindow.fxml"));
        Parent root = loader.load();
        loadTranslations();
        Translations trans = TranslationsManager.getTranslation("baseStrings");
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
        controller.myInit();
        primaryStage.show();
    }

    private void loadTranslations() {
        try {
            ConfigManager configManager = ConfigManager.INSTANCE;
            configManager.configManager();
            String localeString = configManager.getSelectedLanguage();
            Locale locale;
            if (localeString != null && !localeString.isEmpty()) {
                locale = new Locale(localeString);
            } else {
                locale = Locale.getDefault();
            }
            TranslationsManager.loadAllTranslations(locale);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.exceptionDialog("ERROR",
                    "Failed to load a translation, unable to launch. Your detected language: " + Locale.getDefault(), e);
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
