package io.github.stekeblad.videouploader.windowControllers;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class SettingsWindowController {
    public GridPane settingsWindow;
    public Label label_settingsWinDesc;
    public Label label_langSelect;
    public Label label_links;
    public Button btn_gotoMainPage;
    public Button btn_gotoWiki;
    public Button btn_gotoDownload;
    public ChoiceBox choice_languages;
    public Button btn_translationDetails;
    public Label label_resetSettings;
    public Button btn_fullDelete;
    public Button btn_userDelete;

    public void myInit() {

    }

    public boolean onWindowClose() {
        return false;
    }

    public void onGotoMainPageClicked(ActionEvent actionEvent) {
    }

    public void onGotoWikiClicked(ActionEvent actionEvent) {
    }

    public void onGotoDownloadClicked(ActionEvent actionEvent) {
    }

    public void onTranslationDetailsClicked(ActionEvent actionEvent) {
    }

    public void onFullDeleteClicked(ActionEvent actionEvent) {
    }

    public void onUserDeleteClicked(ActionEvent actionEvent) {

    }
}
