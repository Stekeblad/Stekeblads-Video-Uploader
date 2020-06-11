package io.github.stekeblad.videouploader.main;

import io.github.stekeblad.videouploader.jfxExtension.MyStage;
import io.github.stekeblad.videouploader.utils.AlertUtils;
import io.github.stekeblad.videouploader.utils.ConfigManager;
import io.github.stekeblad.videouploader.utils.Constants;
import io.github.stekeblad.videouploader.utils.translation.TranslationBundles;
import io.github.stekeblad.videouploader.utils.translation.Translations;
import io.github.stekeblad.videouploader.utils.translation.TranslationsManager;
import io.github.stekeblad.videouploader.youtube.Auth;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Locale;

/**
 * The program starts here, opens MainWindow and waits for all windows to close
 */
public class Main extends Application {
    private ConfigManager configManager;

    @Override
    public void start(Stage primaryStage) {
        try {
            configManager = ConfigManager.INSTANCE;
            configManager.configManager();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.exceptionDialog("ERROR",
                    "Failed to load settings or other configurations file, unable to launch.", e);
        }
        try {
            loadTranslations();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.exceptionDialog("ERROR",
                    "Failed to load translations, unable to launch. Your detected language: " + Locale.getDefault(), e);
            return;
        }

        // Set the default exception handler, hopefully it can catch some of the exceptions that is not already caught
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> AlertUtils.unhandledExceptionDialog(exception));

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("mainWindow.fxml"));
            MyStage stage = new MyStage(ConfigManager.WindowPropertyNames.MAIN);
            stage.makeScene(loader.load(), Constants.MAIN_WINDOW_DIMENSIONS_RESTRICTION);

            // Show channel name in window title, if authenticated.
            stage.setTitle(getWindowTitle());

            customizeTooltip();

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
        if (configManager.getNeverAuthed()) {
            return trans.getString("app_name");
        }
        // then check the settings
        else if (configManager.getChannelName() == null || configManager.getChannelName().equals("")) {
            // Not there, get from YouTube
            String channelName = Auth.getChannelName();
            if (channelName != null) {
                // success, save and return program name + channel name
                configManager.setChannelName(channelName);
                return trans.getString("app_name") + " - (" + channelName + ")";
            } else {
                // failure, only return program name
                return trans.getString("app_name");
            }
        } else {
            // return program name and saved channel name
            return trans.getString("app_name") + " - (" + configManager.getChannelName() + ")";
        }
    }

    private void loadTranslations() throws Exception {

        String localeString = configManager.getSelectedLanguage();
        Locale locale;
        if (localeString != null && !localeString.isEmpty()) {
            locale = new Locale(localeString);
        } else {
            locale = Locale.getDefault();
        }
        TranslationsManager.loadAllTranslations(locale);
    }

    /**
     * Configures all tooltips to show for a longer time than that is default (now 10 seconds)
     * <p>
     * Maybe a little hacky. Code taken from the following StackOverflow answer:
     * https://stackoverflow.com/a/27739605
     */
    private void customizeTooltip() {
        try {
            Tooltip tooltip = new Tooltip();
            Field fieldBehavior = tooltip.getClass().getDeclaredField("BEHAVIOR");
            fieldBehavior.setAccessible(true);
            Object objBehavior = fieldBehavior.get(tooltip);

            Field fieldTimer = objBehavior.getClass().getDeclaredField("hideTimer");
            fieldTimer.setAccessible(true);
            Timeline objTimer = (Timeline) fieldTimer.get(objBehavior);

            objTimer.getKeyFrames().clear();
            objTimer.getKeyFrames().add(new KeyFrame(new Duration(10000)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
