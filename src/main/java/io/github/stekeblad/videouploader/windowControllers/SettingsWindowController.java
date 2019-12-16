package io.github.stekeblad.videouploader.windowControllers;

import io.github.stekeblad.videouploader.jfxExtension.IWindowController;
import io.github.stekeblad.videouploader.jfxExtension.MyStage;
import io.github.stekeblad.videouploader.utils.AlertUtils;
import io.github.stekeblad.videouploader.utils.ConfigManager;
import io.github.stekeblad.videouploader.utils.Constants;
import io.github.stekeblad.videouploader.utils.RecursiveDirectoryDeleter;
import io.github.stekeblad.videouploader.utils.background.OpenInBrowser;
import io.github.stekeblad.videouploader.utils.translation.TranslationBundles;
import io.github.stekeblad.videouploader.utils.translation.Translations;
import io.github.stekeblad.videouploader.utils.translation.TranslationsManager;
import io.github.stekeblad.videouploader.utils.translation.TranslationsMeta;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static io.github.stekeblad.videouploader.utils.Constants.DATA_DIR;

public class SettingsWindowController implements IWindowController {
    public GridPane settingsWindow;
    public ChoiceBox<String> choice_languages;
    public Label label_langSelect;
    public Label label_links;
    public Label label_resetSettings;
    public Label label_tools;
    public Button btn_gotoMainPage;
    public Button btn_gotoWiki;
    public Button btn_gotoDownload;
    public Button btn_translationDetails;
    public Button btn_clearStoredData;
    public Button btn_metaDataTool;

    private TranslationsMeta translationsMeta;
    private Translations settingsTrans;
    private Translations basicTrans;
    private ConfigManager configManager;
    private boolean hasDoneChanges = false;

    /**
     * Initialize a few things when the window is opened, used instead of initialize as that one does not have access to the scene
     */
    public void myInit() {
        // Set the default exception handler, hopefully it can catch some of the exceptions that is not already caught
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> AlertUtils.unhandledExceptionDialog(exception));

        configManager = ConfigManager.INSTANCE;
        translationsMeta = new TranslationsMeta();
        basicTrans = TranslationsManager.getTranslation(TranslationBundles.BASE);
        settingsTrans = TranslationsManager.getTranslation(TranslationBundles.WINDOW_SETTINGS);
        settingsTrans.autoTranslate(settingsWindow);

        choice_languages.setItems(FXCollections.observableList(translationsMeta.getAllTranslationLocales()));
        choice_languages.getSelectionModel().select(translationsMeta.localeCodeToLangName(configManager.getSelectedLanguage()));

        choice_languages.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> hasDoneChanges = true);

        // F1 for wiki on this window
        settingsWindow.getScene().setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.F1) {
                OpenInBrowser.openInBrowser("https://github.com/Stekeblad/Stekeblads-Video-Uploader/wiki/Settings-Window");
                event.consume();
            }
        });
    }

    @Override
    public boolean onWindowClose() {
        if (hasDoneChanges) {
            AlertUtils.simpleClose("restart may be required", "For some changes to take effect you may need to restart the program").showAndWait();
        }
        configManager.setSelectedLanguage(translationsMeta.langNameToLocaleCode(choice_languages.getValue()));
        configManager.saveSettings();
        return true;
    }

    public void onGotoMainPageClicked(ActionEvent actionEvent) {
        OpenInBrowser.openInBrowser("https://github.com/Stekeblad/Stekeblads-Video-Uploader");
        actionEvent.consume();
    }

    public void onGotoWikiClicked(ActionEvent actionEvent) {
        OpenInBrowser.openInBrowser("https://github.com/Stekeblad/Stekeblads-Video-Uploader/wiki");
        actionEvent.consume();
    }

    public void onGotoDownloadClicked(ActionEvent actionEvent) {
        OpenInBrowser.openInBrowser("https://github.com/Stekeblad/Stekeblads-Video-Uploader/releases");
        actionEvent.consume();
    }

    public void onTranslationDetailsClicked(ActionEvent actionEvent) {
        String selectedLanguage = choice_languages.getValue();
        String translationDetails = "Language locale name: " +
                translationsMeta.getMetaForLanguage(selectedLanguage, "locale") +
                "\nLanguage name: " +
                translationsMeta.getMetaForLanguage(selectedLanguage, "translationName") +
                "\nTranslation made by: " +
                translationsMeta.getMetaForLanguage(selectedLanguage, "authors") +
                "\nLast updated: " +
                translationsMeta.getMetaForLanguage(selectedLanguage, "lastUpdate") +
                "\nUpdated for version: " +
                translationsMeta.getMetaForLanguage(selectedLanguage, "lastAppVersionReview");

        AlertUtils.simpleClose_longContent("Translation Details", translationDetails);
        actionEvent.consume();
    }

    public void onMetaDataToolClicked(ActionEvent actionEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(SettingsWindowController.class.getClassLoader().getResource("fxml/MetaDataToolWindow.fxml"));
            MyStage stage = new MyStage(ConfigManager.WindowPropertyNames.META_TOOL);
            stage.makeScene(fxmlLoader.load(), Constants.META_TOOL_WINDOW_DIMENSIONS_RESTRICTION);
            stage.setTitle(basicTrans.getString("app_metaToolWindowTitle"));
            stage.initModality(Modality.NONE);
            stage.prepareControllerAndShow(fxmlLoader.getController());
        } catch (IOException e) {
            e.printStackTrace();
        }
        actionEvent.consume();
    }

    public void onClearStoredDataClicked(ActionEvent actionEvent) {
        ButtonType buttonChoice = AlertUtils.yesNo(settingsTrans.getString("diag_clearStoredData_short"),
                settingsTrans.getString("diag_clearStoredData_full"), ButtonType.NO);
        if (buttonChoice == ButtonType.NO)
            return;

        AlertUtils.simpleClose(settingsTrans.getString("diag_delAfterExit_short"),
                settingsTrans.getString("diag_delAfterExit_full")).show();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Files.walkFileTree(new File(DATA_DIR).toPath(), new RecursiveDirectoryDeleter());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
        actionEvent.consume();
    }
}
