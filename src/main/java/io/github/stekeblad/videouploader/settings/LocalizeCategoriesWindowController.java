package io.github.stekeblad.videouploader.settings;

import io.github.stekeblad.videouploader.utils.AlertUtils;
import io.github.stekeblad.videouploader.utils.ConfigManager;
import io.github.stekeblad.videouploader.utils.background.OpenInBrowser;
import io.github.stekeblad.videouploader.youtube.utils.CategoryUtils;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.zip.DataFormatException;

import static io.github.stekeblad.videouploader.youtube.Auth.AUTHMSG_DESC;
import static io.github.stekeblad.videouploader.youtube.Auth.AUTHMSG_HEADER;

public class LocalizeCategoriesWindowController implements Initializable{
    public TextField txt_country;
    public TextField txt_lang;
    public TextArea txt_description;
    public Button btn_getCategories;
    public Button btn_cancel;
    public Button btn_codeListCountry;
    public Button btn_codeListLang;

    private ConfigManager configManager = ConfigManager.INSTANCE;
    private CategoryUtils categoryUtils = CategoryUtils.INSTANCE;

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

        // Insert the current country and language code in their text fields
        txt_country.setText(configManager.getCategoryCountry());
        txt_lang.setText(configManager.getCategoryLanguage());
    }

    /**
     * Called when the get categories button is clicked.
     * Performs a quick check of the input before sending it to YouTube
     * @param actionEvent the click event
     */
    public void onGetCategoriesClicked(ActionEvent actionEvent) {

        // test if the codes is of the correct length
        try {
            configManager.setCategoryCountry(txt_country.getText());
            configManager.setCategoryLanguage(txt_lang.getText());
            configManager.saveSettings();
        } catch (DataFormatException e) {
            AlertUtils.simpleClose("Invalid Content", "A valid code for both country and language is two characters long").show();
            actionEvent.consume();
            return;
        }

        // Authentication with youtube is required, check if the user has given permission, if not then ask for it
        if(configManager.getNeverAuthed()) {
            Optional<ButtonType> buttonChoice = AlertUtils.yesNo(AUTHMSG_HEADER, AUTHMSG_DESC).showAndWait();
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

        // Visually indicate the program is working
        btn_getCategories.setText("Downloading...");
        btn_cancel.setDisable(true);
        btn_getCategories.setDisable(true);

        // Send the request in the background
        // Tell it what to do
        Task<Void> backgroundTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                categoryUtils.downloadCategories();
                Platform.runLater(() -> {
                    if (categoryUtils.getCategoryNames().size() < 2) { // < 2 because of default "no categories" category
                        AlertUtils.simpleClose("Error", "The selected language or country code is not valid or not " +
                                "supported by Youtube, did not receive any categories").showAndWait();
                        btn_getCategories.setText("Get Categories");
                        btn_cancel.setDisable(false);
                        btn_getCategories.setDisable(false);
                        return;
                    }
                    onCancelClicked(new ActionEvent());
                });
                return null;
            }
        };

        Thread backgroundThread = new Thread(backgroundTask);
        // Define a handler for exceptions
        backgroundThread.setUncaughtExceptionHandler((t, e) -> Platform.runLater(() -> {
            AlertUtils.simpleClose("Error", "Request to get categories failed").showAndWait();
            e.printStackTrace();
            onCancelClicked(new ActionEvent());
        }));

        // Actually do the thing, start the process of getting the categories!
        backgroundThread.start();
        actionEvent.consume();
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
        try {
            new OpenInBrowser(new URI("https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2#Officially_assigned_code_elements"),
                    (t, e) -> Platform.runLater(() -> AlertUtils.simpleClose("Sorry!",
                            "For some reason we cant open the web page in your default browser, but this is the url:\n " +
                                    "https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2#Officially_assigned_code_elements").showAndWait()));
        } catch (URISyntaxException e) {
            System.err.println("URI Error");
        }
        actionEvent.consume();
    }

    /**
     * called when the code list button for languages is clicked.
     * Opens a web page with language codes.
     * @param actionEvent the click event
     */
    public void onCodeListLangClicked(ActionEvent actionEvent) {
        try {
            new OpenInBrowser(new URI("https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes"),
                    (t, e) -> Platform.runLater(() -> AlertUtils.simpleClose("Sorry!",
                            "For some reason we cant open the web page in your default browser, but this is the url:\n " +
                                    "https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes").showAndWait()));
        } catch (URISyntaxException e) {
            System.err.println("URI Error");

        }
        actionEvent.consume();
    }
}
