package io.github.stekeblad.videouploader.windowControllers;

import io.github.stekeblad.videouploader.utils.AlertUtils;
import io.github.stekeblad.videouploader.utils.ConfigManager;
import io.github.stekeblad.videouploader.utils.TranslationsMeta;
import io.github.stekeblad.videouploader.utils.background.OpenInBrowser;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.stage.WindowEvent;

public class SettingsWindowController {
    public GridPane settingsWindow;
    public Label label_settingsWinDesc;
    public Label label_langSelect;
    public Label label_links;
    public Button btn_gotoMainPage;
    public Button btn_gotoWiki;
    public Button btn_gotoDownload;
    public ChoiceBox<String> choice_languages;
    public Button btn_translationDetails;
    public Label label_resetSettings;
    public Button btn_clearStoredData;

    private TranslationsMeta translationsMeta;
    private ConfigManager configManager;
    private boolean hasDoneChanges = false;

    /**
     * Initialize a few things when the window is opened, used instead of initialize as that one does not have access to the scene
     */
    public void myInit() {
        configManager = ConfigManager.INSTANCE;
        translationsMeta = new TranslationsMeta();

        choice_languages.setItems(FXCollections.observableList(translationsMeta.getAllTranslationLocales()));
        choice_languages.getSelectionModel().select(translationsMeta.localeCodeToLangName(configManager.getSelectedLanguage()));
        System.out.println(translationsMeta.getAllTranslationLocales());


        // translation file for this window
        // -- tooltip for wiki button to tell user to press F1 to get to current window's wiki page

        // F1 for wiki on this window
        settingsWindow.getScene().setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.F1) {
                OpenInBrowser.openInBrowser("https://github.com/Stekeblad/Stekeblads-Video-Uploader/wiki/------------------");
                event.consume();
            }
        });
    }

    /**
     * Executed when the user tries to close the window
     *
     * @param windowEvent the close window event
     */
    public void onWindowClose(WindowEvent windowEvent) {
        if (hasDoneChanges) {
            AlertUtils.simpleClose("restart may be required", "For some changes to take effect you may need to restart the program").showAndWait();
        }
        configManager.setSelectedLanguage(translationsMeta.langNameToLocaleCode(choice_languages.getValue()));
        configManager.saveSettings();
        // do not consume event, it will prevent the window from closing
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
                "\nLast updated for version: " +
                translationsMeta.getMetaForLanguage(selectedLanguage, "lastUpdate");

        AlertUtils.simpleClose_longContent("Translation Details", translationDetails);
        actionEvent.consume();
    }

    public void onClearStoredDataClicked(ActionEvent actionEvent) {
        AlertUtils.simpleClose("Not yet implemented", "Sorry, you can currently only delete stored data manually!" +
                "\n\nYou do this by closing the program and then deleting the \"uploader data\" folder located in the same folder as the program.").show();
        actionEvent.consume();
    }
}
