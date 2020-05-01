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

            // Show channel name in window title.
            if (configManager.getNeverAuthed()) {
                stage.setTitle(trans.getString("app_name"));
            } else {
                String channelName = Auth.getChannelName();
                if (channelName != null)
                    stage.setTitle(trans.getString("app_name") + " - (" + channelName + ")");
                else
                    stage.setTitle(trans.getString("app_name"));
            }

            customizeTooltip();

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
