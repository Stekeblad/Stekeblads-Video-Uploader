package io.github.stekeblad.videouploader.main;

import io.github.stekeblad.videouploader.jfxExtension.MyStage;
import io.github.stekeblad.videouploader.utils.AlertUtils;
import io.github.stekeblad.videouploader.utils.ConfigManager;
import io.github.stekeblad.videouploader.utils.Constants;
import io.github.stekeblad.videouploader.utils.translation.TranslationBundles;
import io.github.stekeblad.videouploader.utils.translation.Translations;
import io.github.stekeblad.videouploader.utils.translation.TranslationsManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;

/**
 * The program starts here, opens MainWindow and waits for all windows to close
 */
public class Main extends Application {
    private ConfigManager configManager;

    @Override
    public void start(Stage primaryStage) {
        try {
            loadTranslations();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.exceptionDialog("ERROR",
                    "Failed to load translations, unable to launch. Your detected language: " + Locale.getDefault(), e);
            return;
        }
        Translations trans = TranslationsManager.getTranslation(TranslationBundles.BASE);

        // Set the default exception handler, hopefully it can catch some of the exceptions that is not already caught

        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> AlertUtils.unhandledExceptionDialog(exception));

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("mainWindow.fxml"));
            MyStage stage = new MyStage(ConfigManager.WindowPropertyNames.MAIN);
            stage.makeScene(loader.load(), Constants.MAIN_WINDOW_DIMENSIONS_RESTRICTION);

            stage.setTitle(trans.getString("app_name"));

            stage.prepareControllerAndShow(loader.getController());
        } catch (IOException e) {
            AlertUtils.exceptionDialog("Stekeblads Video Uploader",
                    "Unable to load main window, the program will exit", e);
        }
    }

    private void loadTranslations() throws Exception {
        configManager = ConfigManager.INSTANCE;
            configManager.configManager();
            String localeString = configManager.getSelectedLanguage();
            Locale locale;
            if (localeString != null && !localeString.isEmpty()) {
                locale = new Locale(localeString);
            } else {
                locale = Locale.getDefault();
            }
            TranslationsManager.loadAllTranslations(locale);
    }


    public static void main(String[] args) {
        launch(args);
    }
}
