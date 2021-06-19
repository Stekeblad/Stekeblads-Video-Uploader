package io.github.stekeblad.videouploader.main;

import io.github.stekeblad.videouploader.jfxExtension.MyStage;
import io.github.stekeblad.videouploader.managers.SettingsManager;
import io.github.stekeblad.videouploader.utils.AlertUtils;
import io.github.stekeblad.videouploader.utils.Constants;
import io.github.stekeblad.videouploader.utils.translation.TranslationBundles;
import io.github.stekeblad.videouploader.utils.translation.Translations;
import io.github.stekeblad.videouploader.utils.translation.TranslationsManager;
import io.github.stekeblad.videouploader.youtube.YouTubeApiLayer;
import io.github.stekeblad.videouploader.youtube.exceptions.YouTubeException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;

/**
 * The program starts here, opens MainWindow and waits for all windows to close
 */
public class Main extends Application {
    private SettingsManager settingsManager;

    @Override
    public void start(Stage primaryStage) {
        try {
            settingsManager = SettingsManager.getSettingsManager();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.exceptionDialog("ERROR - Stekeblads Video Uploader",
                    "Failed to load settings or other configurations file, unable to launch.", e);
        }
        try {
            loadTranslations();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.exceptionDialog("ERROR - Stekeblads Video Uploader",
                    "Failed to load translations, unable to launch. Your detected language: " + Locale.getDefault(), e);
            return;
        }

        // Set the default exception handler, hopefully it can catch some of the exceptions that is not already caught
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> AlertUtils.unhandledExceptionDialog(exception));

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("mainWindow.fxml"));
            MyStage stage = new MyStage(Constants.WindowPropertyNames.MAIN);
            stage.makeScene(loader.load(), Constants.MAIN_WINDOW_DIMENSIONS_RESTRICTION);

            // Show channel name in window title, if authenticated.
            stage.setTitle(getWindowTitle());

            stage.prepareControllerAndShow(loader.getController());
        } catch (IOException e) {
            AlertUtils.exceptionDialog("Stekeblads Video Uploader",
                    "Unable to load main window, the program will exit", e);
        }
    }

    /**
     * If the user has authenticated with YouTube, then an attempt is made
     * to show the name of the user's channel in the window title. In order to save time and quota
     * may the channel name already be saved in the settings file and loaded from there instead of
     * requested from YouTube.
     *
     * @return A string to show in the window title
     */
    private String getWindowTitle() {
        Translations trans = TranslationsManager.getTranslation(TranslationBundles.BASE);
        // If the user has never authed, only use program name
        if (settingsManager.getNeverAuthed()) {
            return trans.getString("app_name");
        }
        // then check the settings
        else if (settingsManager.getChannelName() == null || settingsManager.getChannelName().equals("")) {
            // Not there, get from YouTube
            String channelName;
            try {
                channelName = YouTubeApiLayer.requestChannelName();
            } catch (YouTubeException e) {
                // Should probably not show errors from this, the program is starting at this point
                return trans.getString("app_name");
            }
            if (channelName != null) {
                // success, save and return program name + channel name
                settingsManager.setChannelName(channelName);
                return trans.getString("app_name") + " - (" + channelName + ")";
            } else {
                // failure, only return program name
                return trans.getString("app_name");
            }
        } else {
            // return program name and saved channel name
            return trans.getString("app_name") + " - (" + settingsManager.getChannelName() + ")";
        }
    }

    private void loadTranslations() throws Exception {

        String localeString = settingsManager.getSelectedLanguage();
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
