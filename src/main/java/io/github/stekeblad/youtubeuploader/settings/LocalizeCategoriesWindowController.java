package io.github.stekeblad.youtubeuploader.settings;

import io.github.stekeblad.youtubeuploader.utils.AlertUtils;
import io.github.stekeblad.youtubeuploader.utils.ConfigManager;
import io.github.stekeblad.youtubeuploader.youtube.utils.CategoryUtils;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.zip.DataFormatException;

public class LocalizeCategoriesWindowController implements Initializable{
    public TextField txt_country;
    public TextField txt_lang;
    public TextArea txt_description;
    public Button btn_getCategories;
    public Button btn_cancel;
    public Button btn_codeList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        txt_country.textProperty().addListener((observable, oldValue, newValue) -> {
            if(! newValue.matches("[A-Za-z]") || newValue.length() > 2) {
                txt_country.setText(oldValue);
            }
        });

        txt_lang.textProperty().addListener((observable, oldValue, newValue) -> {
            if(! newValue.matches("[A-Za-z]") || newValue.length() > 2) {
                txt_lang.setText(oldValue);
            }
        });

        txt_description.setText("Did you know that Youtube has different categories in different countries and that the " +
                "internal categoryId for one category can be different in different countries? For your video to appear " +
                "in the correct category we need to know what country you live in. (We will keep it a secret between you and Youtube.) " +
                "The language field allows you to get the category names in any language Youtube support. " +
                "(language can be different from country if you wish so.) " +
                "The fields should contain the country's official two-character code. The code list button opens a list of " +
                "countries and their codes in your default browser");

        btn_codeList.setTooltip(new Tooltip("https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2#Officially_assigned_code_elements"));
    }

    public void onGetCategoriesClicked(ActionEvent actionEvent) {
        ConfigManager configManager = ConfigManager.INSTANCE;
        CategoryUtils categoryUtils = CategoryUtils.INSTANCE;

        try {
            configManager.setCategoryCountry(txt_country.getText());
            configManager.setCategoryLanguage(txt_lang.getText());
        } catch (DataFormatException e) {
            AlertUtils.simpleClose("Invalid Content", "A valid code for both country and language is two characters long").show();
            actionEvent.consume();
            return;
        }
        btn_cancel.setDisable(true);
        btn_getCategories.setDisable(true);
        btn_getCategories.setText("Downloading...");
        categoryUtils.downloadCategories();

        onCancelClicked(actionEvent);
    }

    public void onCancelClicked(ActionEvent actionEvent) {
        ((Stage) btn_cancel.getScene().getWindow()).close();
        actionEvent.consume();
    }

    public void onCodeListClicked(ActionEvent actionEvent) {
        if(Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI("https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2#Officially_assigned_code_elements"));
                actionEvent.consume();
                return;
            } catch (IOException | URISyntaxException e) {
                System.err.println("Failed open country code list (Wikipedia)");
            }
        } else {
            System.err.println("Desktop not supported, cant open Wikipedia country code list");
        }
        AlertUtils.simpleClose("Sorry!", "For some reason we cant open the webpage in your default browser, but this is the url:\n " +
                "https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2#Officially_assigned_code_elements").showAndWait();
        actionEvent.consume();
    }
}
