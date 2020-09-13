package io.github.stekeblad.videouploader.utils.background;

import io.github.stekeblad.videouploader.utils.AlertUtils;
import io.github.stekeblad.videouploader.utils.ConfigManager;
import io.github.stekeblad.videouploader.utils.HttpOperations;
import io.github.stekeblad.videouploader.utils.RecursiveDirectoryDeleter;
import io.github.stekeblad.videouploader.utils.translation.TranslationBundles;
import io.github.stekeblad.videouploader.utils.translation.Translations;
import io.github.stekeblad.videouploader.utils.translation.TranslationsManager;
import io.github.stekeblad.videouploader.youtube.YouTubeApiLayer;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.ButtonType;
import okhttp3.FormBody;
import okhttp3.RequestBody;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static io.github.stekeblad.videouploader.utils.Constants.DATA_DIR;

/**
 * Class for off-loading the Settings window from the logic of revoking YouTube access and deleting program settings.
 */
public class YouTubeRevoke {

    private final Translations settingsTrans;
    private boolean logOut;
    private boolean delete;

    /**
     * Shows a warning about what the user is trying to do and if they confirm that they are sure
     * calls {@link YouTubeRevoke#revokeAndDelete()}
     */
    public static void show() {
        YouTubeRevoke revokeWindow = new YouTubeRevoke();
        revokeWindow.showWarning();
        revokeWindow.revokeAndDelete();
    }

    private YouTubeRevoke() {
        settingsTrans = TranslationsManager.getTranslation(TranslationBundles.WINDOW_SETTINGS);
        logOut = false;
        delete = false;
    }

    /**
     * Shows delete/log out warning and sets internal variables based on the user's choice
     */
    private void showWarning() {

        String button1 = settingsTrans.getString("diag_clearStoredData_optDel");
        String button2 = settingsTrans.getString("diag_clearStoredData_optLogOutDel");
        String button3 = ButtonType.CANCEL.getText();

        String buttonChoice = AlertUtils.threeButtons(settingsTrans.getString("diag_clearStoredData_short"),
                settingsTrans.getString("diag_clearStoredData_full"),
                button1, button2, button3);

        // buttonChoice needs to be inside of equals and not the reverse! buttonChoice can be null based on user action.
        // getString() is more likely to throw exceptions than returning null and then its an error in the code or the translation.
        if (button1.equals(buttonChoice)) {
            delete = true;
        } else if (button2.equals(buttonChoice) && !ConfigManager.INSTANCE.getNeverAuthed()) {
            delete = true;
            logOut = true;
        }
    }

    /**
     * Tries to revoke the YouTube channel access token and on success or semi-success[1] also adds a shutdown hook for
     * deleting all the program's files.
     * <p>
     * [1] If the token could not be automatically revoked then an alert dialog is shown asking the user if they want to
     * do it manually in their browser. If they answer yes its treated as a successful revocation.
     */
    private void revokeAndDelete() {
        // Check if we need to do something
        if (!logOut && !delete)
            return;

        Task<Void> newTask = new Task<>() {
            @Override
            // Define what it does
            protected Void call() {
                try {
                    // Send request to YouTube on a background thread to invalidate the token
                    boolean revokeSuccess = true;
                    if (logOut) {
                        try {
                            String youtubeToken = YouTubeApiLayer.getToken();
                            if (youtubeToken == null) {
                                revokeSuccess = false;
                            } else {
                                RequestBody form = new FormBody.Builder()
                                        .add("token", youtubeToken)
                                        .build();
                                revokeSuccess = HttpOperations.postForm("https://oauth2.googleapis.com/revoke", form);
                            }
                        } catch (Exception ignored) {
                            revokeSuccess = false;
                        }
                    }

                    // When we jump between threads variables needs to be final or effectively, this extra variable is
                    // needed for that requirement to be met.
                    boolean finalRevokeSuccess = revokeSuccess;

                    // Jump back to the UI thread to show the dialog if we need to.
                    Platform.runLater(() -> {
                        boolean shouldDeleteFiles = finalRevokeSuccess;
                        if (logOut) {
                            if (!shouldDeleteFiles) {
                                ButtonType choiceRevokeInBrowser = AlertUtils.yesNo("Failed to revoke channel access",
                                        "Do you want to open the permissions page for your Google account in your web browser and do it from there?",
                                        ButtonType.NO);
                                if (choiceRevokeInBrowser == ButtonType.YES) {
                                    OpenInBrowser.openInBrowser("https://myaccount.google.com/permissions");
                                    shouldDeleteFiles = true;
                                }
                            }
                            if (shouldDeleteFiles)
                                YouTubeApiLayer.tryDeleteToken();
                        }

                        // Registering the hook is fast enough to be done on the UI thread
                        // and getting back on the background thread is too troublesome
                        if (delete) {
                            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                                try {
                                    Files.walkFileTree(new File(DATA_DIR).toPath(), new RecursiveDirectoryDeleter());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }));
                        }
                    });
                } catch (Exception ignored) {

                }
                return null;
            }
        };

        Thread newThread = new Thread(newTask);
        newThread.start();
    }
}
