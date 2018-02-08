package io.github.stekeblad.videouploader.settings;

import io.github.stekeblad.videouploader.utils.AlertUtils;
import io.github.stekeblad.videouploader.utils.ConfigManager;
import io.github.stekeblad.videouploader.youtube.utils.CategoryUtils;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.*;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.zip.DataFormatException;

public class LocalizeCategoriesWindowController implements Initializable{
    public TextField txt_country;
    public TextField txt_lang;
    public TextArea txt_description;
    public Button btn_getCategories;
    public Button btn_cancel;
    public Button btn_codeListCountry;
    public Button btn_codeListLang;

    /**
     * Initialize things when the window is opened
     * @param location provided by fxml
     * @param resources provided by fxml
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Filter what can be entered into the textFields
        txt_country.textProperty().addListener((observable, oldValue, newValue) -> {
            if(! newValue.matches("[A-Za-z]*") || newValue.length() > 2) {
                txt_country.setText(oldValue);
            }
        });

        txt_lang.textProperty().addListener((observable, oldValue, newValue) -> {
            if(! newValue.matches("[A-Za-z]*") || newValue.length() > 2) {
                txt_lang.setText(oldValue);
            }
        });

        // Add description text
        txt_description.setText("Did you know that Youtube has different categories in different countries and that the " +
                "internal categoryId for one category can be different in different countries? For your video to appear " +
                "in the correct category we need to know what country you live in. (We will keep it a secret between you and Youtube.) " +
                "The language field allows you to get the category names in any language Youtube support. " +
                "The fields should contain the official two-character code for the country/language. The code list buttons opens a list of " +
                "countries/languages and their codes in your default browser. " +
                "Also, it may sound weird but we need access to your Youtube channel to do this because of the way the Youtube API works. " +
                "If you have not already given permission you will be asked to then you press the \"Get categories\" button");

        // set ToolTips
        btn_codeListCountry.setTooltip(new Tooltip("https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2#Officially_assigned_code_elements"));
        btn_codeListLang.setTooltip(new Tooltip("https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes"));
    }

    /**
     * Called when the get categories button is clicked.
     * Performs a quick check of the input before sending it to YouTube
     * @param actionEvent the click event
     */
    public void onGetCategoriesClicked(ActionEvent actionEvent) {
        ConfigManager configManager = ConfigManager.INSTANCE;
        CategoryUtils categoryUtils = CategoryUtils.INSTANCE;

        // test if the codes is of the correct length
        try {
            configManager.setCategoryCountry(txt_country.getText());
            configManager.setCategoryLanguage(txt_lang.getText());
        } catch (DataFormatException e) {
            AlertUtils.simpleClose("Invalid Content", "A valid code for both country and language is two characters long").show();
            actionEvent.consume();
            return;
        }

        // Authentication with youtube is required, check if the user has given permission, if not then ask for it
        if(configManager.getNeverAuthed()) {
            Optional<ButtonType> buttonChoice = AlertUtils.yesNo("Authentication Required",
                    "To use the Youtube API you must grant this application permission to access your Youtube channel. " +
                            "Do you want to allow \"Stekeblads Video Uploader\" to access Your channel?" +
                            "\n\nPermission overview: \"YOUTUBE_UPLOAD\" for allowing the program to upload videos for you" +
                            "\n\"YOUTUBE\" for basic account access, adding videos to playlists and setting thumbnails" +
                            "\n\nPress yes to open your browser for authentication or no to cancel")
                    .showAndWait();
            if (buttonChoice.isPresent()) {
                if (buttonChoice.get() == ButtonType.YES) {
                    configManager.setNeverAuthed(false);
                    configManager.saveSettings();
                } else { // ButtonType.NO or closed [X]
                    AlertUtils.simpleClose("Permission not Granted", "Permission to access your YouTube was denied, categories can not be retrieved.").show();
                    actionEvent.consume();
                    return;
                }
            }
        }

        // Send the request
        btn_getCategories.setText("Downloading...");
        btn_cancel.setDisable(true);
        btn_getCategories.setDisable(true);
        categoryUtils.downloadCategories();

        // Handle negative result
        if (categoryUtils.getCategoryNames().size() < 2) { // < 2 because of default "no categories" category
            AlertUtils.simpleClose("Error", "The selected language or country code is not valid or not " +
                    "supported by Youtube, did not receive any categories").showAndWait();
            actionEvent.consume();
            return;
        }

        onCancelClicked(actionEvent);
    }

    /**
     * Called when the cancel button is clicked.
     * Closes the window.
     * @param actionEvent the click event
     */
    public void onCancelClicked(ActionEvent actionEvent) {
        ((Stage) btn_cancel.getScene().getWindow()).close();
        actionEvent.consume();
    }

    /**
     * called when the code list button for countries is clicked.
     * Opens a web page with country codes.
     * @param actionEvent the click event
     */
    public void onCodeListCountryClicked(ActionEvent actionEvent) {
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
        AlertUtils.simpleClose("Sorry!", "For some reason we cant open the web page in your default browser, but this is the url:\n " +
                "https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2#Officially_assigned_code_elements").showAndWait();
        actionEvent.consume();
    }

    /**
     * called when the code list button for languages is clicked.
     * Opens a web page with language codes.
     * @param actionEvent the click event
     */
    public void onCodeListLangClicked(ActionEvent actionEvent) {
        if(Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI("https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes"));
                actionEvent.consume();
                return;
            } catch (IOException | URISyntaxException e) {
                System.err.println("Failed open language code list (Wikipedia)");
            }
        } else {
            System.err.println("Desktop not supported, cant open Wikipedia language code list");
        }
        AlertUtils.simpleClose("Sorry!", "For some reason we cant open the web page in your default browser, but this is the url:\n " +
                "https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes").showAndWait();
        actionEvent.consume();
    }
}
