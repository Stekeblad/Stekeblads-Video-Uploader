package io.github.stekeblad.videouploader.utils.background;

import io.github.stekeblad.videouploader.updater.UpdateInfo;
import io.github.stekeblad.videouploader.updater.UpdaterCore;
import io.github.stekeblad.videouploader.updater.VersionFormat;
import io.github.stekeblad.videouploader.utils.AlertUtils;
import io.github.stekeblad.videouploader.utils.ConfigManager;
import io.github.stekeblad.videouploader.utils.translation.TranslationBundles;
import io.github.stekeblad.videouploader.utils.translation.Translations;
import io.github.stekeblad.videouploader.utils.translation.TranslationsManager;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * UpdaterUi is a user interface version over {@link UpdaterCore} that checks for updates and downloads them.
 * UpdaterUi respects the user settings to disable update checks and applying them silently. If updates are not set to
 * be downloaded silently the user will be shown a popup about new updates, what's new and a yes/no choice to download it
 * or not. To use this updater, just instantiate the class and call {@link UpdaterUi#runUpdater}
 */
public class UpdaterUi {
    private UpdaterCore updater;
    private Stage dialogStage;
    private TextArea updatingStatus;
    private Button cancelButton;
    private Button closeButton;

    /**
     * Starts the process of checking and installing updates, the details are described on the class documentation: {@link UpdaterUi}.
     *
     * @param inBackground if true, then dialogs will only appear if an update is found and silent updates is disabled
     *                     in the settings window.
     */
    public void runUpdater(boolean inBackground) {
        ConfigManager configManager = ConfigManager.INSTANCE;

        // Do not check for updates if running in background and disabled from settings
        // Allow manually started updates
        if (inBackground && !configManager.getCheckForUpdates())
            return;

        createUpdateDialog();

        if (!inBackground)
            Platform.runLater(dialogStage::show);

        updater = new UpdaterCore(statusString -> Platform.runLater(() ->
                updatingStatus.setText(updatingStatus.getText() + statusString)));

        updater.checkForUpdate(updateInfo -> {
            // If checking for updates failed or no update was found
            if (updateInfo == null) {
                if (!inBackground) {
                    // Change button from Cancel to Close
                    Platform.runLater(() -> {
                        cancelButton.setVisible(false);
                        closeButton.setVisible(true);
                    });
                }
                return;
            }

            // Do not allow automatic updates to a new major release
            // In this case we sak if the user wants to update manually and sends them to the releases page on GitHub
            VersionFormat currentVersion = updater.determineCurrentVersion();
            boolean isMajorUpdate = currentVersion.getMajor() < updateInfo.getVersion().getMajor();
            if (isMajorUpdate) {
                if (getInstallUpdateButtonChoice(updateInfo) == ButtonType.NO) {
                    Platform.runLater(() ->
                            dialogStage.fireEvent(new WindowEvent(dialogStage, WindowEvent.WINDOW_CLOSE_REQUEST))
                    );
                } else {
                    OpenInBrowser.openInBrowser("https://github.com/Stekeblad/Stekeblads-Video-Uploader/releases/");
                }
                return;
            }


            // if in background and silent updates is enabled then the user will not be not
            // be notified and the update is downloaded directly.
            if (inBackground && configManager.getSilentUpdates()) {
                updater.installUpdate(null);
            } else {
                // Ask user if they want to update
                if (getInstallUpdateButtonChoice(updateInfo) == ButtonType.NO) {
                    Platform.runLater(() ->
                            dialogStage.fireEvent(new WindowEvent(dialogStage, WindowEvent.WINDOW_CLOSE_REQUEST))
                    );
                } else {
                    if (inBackground)
                        Platform.runLater(dialogStage::show);

                    updater.installUpdate(ignored -> Platform.runLater(() -> {
                        // Change button from Cancel to Close
                        cancelButton.setVisible(false);
                        closeButton.setVisible(true);
                    }));
                }
            }
        });
    }

    /**
     * Sets up the text field, buttons, layout etc.
     * Does not display the dialog.
     */
    private void createUpdateDialog() {
        Translations basicTrans = TranslationsManager.getTranslation(TranslationBundles.BASE);

        updatingStatus = new TextArea();
        updatingStatus.setEditable(false);
        updatingStatus.setWrapText(false);
        updatingStatus.setId("statusTextArea");

        closeButton = new Button(basicTrans.getString("close"));
        closeButton.setId("closeButton");
        closeButton.setVisible(false);
        closeButton.setOnMouseClicked(event -> {
            event.consume();
            dialogStage.fireEvent(new WindowEvent(dialogStage, WindowEvent.WINDOW_CLOSE_REQUEST));
        });

        cancelButton = new Button(basicTrans.getString("cancel"));
        cancelButton.setCancelButton(true);
        cancelButton.setId("cancelButton");
        cancelButton.setOnMouseClicked(event -> {
            event.consume();
            cancelButton.setDisable(false);
            updater.abort();
            cancelButton.setVisible(false);
            closeButton.setVisible(true);
        });

        GridPane pane = new GridPane();
        GridPane.setMargin(updatingStatus, new Insets(3, 0, 0, 0));
        GridPane.setMargin(closeButton, new Insets(3, 0, 0, 0));
        GridPane.setMargin(cancelButton, new Insets(3, 0, 0, 0));
        pane.add(updatingStatus, 0, 0);
        pane.add(closeButton, 0, 1);
        pane.add(cancelButton, 0, 2);
        GridPane.setHalignment(closeButton, HPos.RIGHT);
        GridPane.setHalignment(cancelButton, HPos.RIGHT);

        Scene updateDialog = new Scene(pane);
        dialogStage = new Stage();
        dialogStage.initModality(Modality.NONE);
        dialogStage.setScene(updateDialog);
        dialogStage.setTitle("Updater - Stekeblads Video Uploader");
        dialogStage.setAlwaysOnTop(false);
        dialogStage.setResizable(true);

        // abort if [X] is clicked
        dialogStage.setOnCloseRequest(event -> updater.abort());

        // F1 for wiki on this window
        dialogStage.getScene().setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.F1) {
                OpenInBrowser.openInBrowser("https://github.com/Stekeblad/Stekeblads-Video-Uploader/wiki/Update-dialog");
                event.consume();
            }
        });
    }

    /**
     * Opens a AlertUtils.yesNo-dialog on the UI thread asking if the update should be downloaded.
     *
     * @return The ButtonType of the selected button or ButtonType.NO if the user canceled or something went wrong.
     */
    private ButtonType getInstallUpdateButtonChoice(UpdateInfo info) {
        try {
            Translations basicTrans = TranslationsManager.getTranslation(TranslationBundles.BASE);

            String alertText = basicTrans.getString("update_available_full") + info.getVersion() +
                    "\n\n" + info.getHeading() +
                    "\n\n" + info.getBody();

            return AlertUtils.yesNo(basicTrans.getString("update_available_short") + " - Stekeblads Video Uploader",
                    alertText, ButtonType.NO);
        } catch (Exception ignored) {
            return ButtonType.NO;
        }
    }

    /**
     * Opens a AlertUtils.yesNo-dialog on the UI thread asking if the user wants to download the major update manually.
     *
     * @return The ButtonType of the selected button or ButtonType.NO if the user canceled or something went wrong.
     */
    private ButtonType getUpdateManuallyButtonChoice(UpdateInfo info) {
        try {
            Translations basicTrans = TranslationsManager.getTranslation(TranslationBundles.BASE);

            String alertText = basicTrans.getString("update_available_noAuto_full") + info.getVersion() +
                    "\n\n" + info.getHeading() +
                    "\n\n" + info.getBody();

            return AlertUtils.yesNo(basicTrans.getString("update_available_short") + " - Stekeblads Video Uploader",
                    alertText, ButtonType.NO);
        } catch (Exception ignored) {
            return ButtonType.NO;
        }
    }
}
