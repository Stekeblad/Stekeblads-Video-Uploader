package io.github.stekeblad.videouploader.windowControllers;

import io.github.stekeblad.videouploader.utils.FileUtils;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;

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
    public Button btn_fullDelete;
    public Button btn_userDelete;

    public void myInit() {
        final int PROPERTIES_LEN = ".properties".length();
        final int META_LEN = "meta_".length();
        ArrayList<String> langList = new ArrayList<>();
        List<String> filesInMeta = FileUtils.getContentOfResourceDir("strings/meta/");
        if (filesInMeta != null) {
            for (String fileName : filesInMeta) {
                if (fileName.equals("meta.properties")) {
                    langList.add("default english");
                } else {
                    // Only keep the local part of the file name "meta_sv_SE.properties" -> "sv_SE"
                    fileName = fileName.substring(META_LEN, fileName.length() - PROPERTIES_LEN);
                    langList.add(fileName);
                }
            }
            choice_languages.setItems(FXCollections.observableArrayList(langList));
        }
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
