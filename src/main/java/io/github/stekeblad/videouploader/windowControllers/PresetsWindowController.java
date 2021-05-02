package io.github.stekeblad.videouploader.windowControllers;

import io.github.stekeblad.videouploader.ListControllers.PresetItemController;
import io.github.stekeblad.videouploader.jfxExtension.MyStage;
import io.github.stekeblad.videouploader.jfxExtension.NoneSelectionModel;
import io.github.stekeblad.videouploader.managers.CategoryManager;
import io.github.stekeblad.videouploader.managers.PresetManager;
import io.github.stekeblad.videouploader.managers.SettingsManager;
import io.github.stekeblad.videouploader.models.NewVideoPresetModel;
import io.github.stekeblad.videouploader.utils.AlertUtils;
import io.github.stekeblad.videouploader.utils.Constants;
import io.github.stekeblad.videouploader.utils.background.OpenInBrowser;
import io.github.stekeblad.videouploader.utils.translation.TranslationBundles;
import io.github.stekeblad.videouploader.utils.translation.Translations;
import io.github.stekeblad.videouploader.utils.translation.TranslationsManager;
import io.github.stekeblad.videouploader.youtube.VisibilityStatus;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.github.stekeblad.videouploader.utils.Constants.WindowPropertyNames;


public class PresetsWindowController {

    public AnchorPane presetWindow;
    public ListView<PresetItemController> listPresets;
    public ToolBar toolbar;
    public Button btn_tips;
    public Button btn_addNewPreset;
    public Button btn_localizeCategories;
    public Button btn_managePlaylists;
    public TextField txt_nameNewPreset;
    public Label label_savedPresets;

    private final SettingsManager settingsManager = SettingsManager.getSettingsManager();
    private final CategoryManager categoryManager = CategoryManager.getCategoryManager();
    private final PresetManager presetManager = PresetManager.getPresetManager();

    private ObservableList<PresetItemController> presetItems;
    ListChangeListener<NewVideoPresetModel> presetChangeListener;

    private Translations transPresetWin;
    private Translations transBasic;
    private Translations transPreset;

    private static final String PRESET_PANE_ID_PREFIX = "preset-";

    /**
     * Initialize a few things when the window is opened, used instead of initialize as that one does not have access to the scene
     */
    public void myInit(boolean disableLocaleChange) {
        // Set the default exception handler, hopefully it can catch some of the exceptions that is not already caught
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> AlertUtils.unhandledExceptionDialog(exception));

        // Load custom CSS (for improved readability of disabled ChoiceBoxes)
        URL css_path = PresetsWindowController.class.getClassLoader().getResource("css/disabled.css");
        if (css_path != null) {
            presetWindow.getScene().getStylesheets().add(css_path.toString());
        }

        // Load Translations
        transPresetWin = TranslationsManager.getTranslation(TranslationBundles.WINDOW_PRESET);
        transBasic = TranslationsManager.getTranslation(TranslationBundles.BASE);
        transPresetWin.autoTranslate(presetWindow);
        transPreset = TranslationsManager.getTranslation(TranslationBundles.PRESET_UPLOAD);

        // Children of Toolbars can not be detected through code currently (probably a bug)
        btn_managePlaylists.setText(transPresetWin.getString("btn_managePlaylists"));
        btn_managePlaylists.setTooltip(new Tooltip(transPresetWin.getString("btn_managePlaylists_tt")));
        btn_localizeCategories.setText(transPresetWin.getString("btn_localizeCategories"));
        btn_localizeCategories.setTooltip(new Tooltip(transPresetWin.getString("btn_localizeCategories_tt")));
        btn_tips.setText(transPresetWin.getString("btn_tips"));
        btn_addNewPreset.setText(transPresetWin.getString("btn_addNewPreset"));
        txt_nameNewPreset.setPromptText(transPresetWin.getString("txt_nameNewPreset_pt"));

        // Set selectionModel for the presets listView
        listPresets.setSelectionModel(new NoneSelectionModel<>());

        // Load all saved presets
        presetItems = FXCollections.observableArrayList(
                presetManager.getAllPresets().stream()
                        .map((NewVideoPresetModel videoPreset)
                                -> new PresetItemController(videoPreset, listPresets.prefWidthProperty()))
                        .collect(Collectors.toList()))
                .sorted();
        listPresets.setItems(presetItems);
        configurePresetChangeListener();

        // Set so pressing enter in txt_nameNewPreset triggers onPresetAddNewClicked
        txt_nameNewPreset.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                onPresetAddNewClicked(new ActionEvent());
                keyEvent.consume();
            } // On Java 8, function key events is not passed on by TextFields, lets add it because handler already exists (Bug fixed in Java 9)
            //TODO: now on java-11 so try removing this
            if (keyEvent.getCode() == KeyCode.F1) {
                OpenInBrowser.openInBrowser("https://github.com/Stekeblad/Stekeblads-Video-Uploader/wiki/Preset-Window");
                keyEvent.consume();
            }
        });

        // Set so pressing F1 opens the wiki page for this window
        presetWindow.getScene().setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.F1) {
                OpenInBrowser.openInBrowser("https://github.com/Stekeblad/Stekeblads-Video-Uploader/wiki/Preset-Window");
                event.consume();
            }
        });
    }

    private void configurePresetChangeListener() {
        presetChangeListener = c -> {
            while (c.next()) {
                if (c.wasPermutated()) {
                    // ignore permutations
                } else if (c.wasUpdated()) {
                    for (int i = c.getFrom(); i < c.getTo(); ++i) {
                        NewVideoPresetModel updated = c.getList().get(i);
                        Optional<PresetItemController> controller = presetItems.stream()
                                .filter(p -> p.getUniqueModelId().compareTo(updated.getUniqueId()) == 0)
                                .findFirst();
                        // I wish this works, UNTESTED!
                        // cancelEdit updates all nodes in the pane with data from the model (undoing all unsaved edits)
                        controller.ifPresent((contr) -> contr.cancelEdit(new ActionEvent()));
                    }
                } else {
                    for (NewVideoPresetModel removed : c.getRemoved()) {
                        Optional<PresetItemController> controller = presetItems.stream()
                                .filter(p -> p.getUniqueModelId().compareTo(removed.getUniqueId()) == 0)
                                .findFirst();
                        controller.ifPresent(pic -> presetItems.remove(pic));
                    }
                    for (NewVideoPresetModel added : c.getAddedSubList()) {
                        presetItems.add(new PresetItemController(added, listPresets.prefWidthProperty()));
                    }
                }
            }
        };
        presetManager.getAllPresets().addListener(presetChangeListener);
    }

    /**
     * Executed when the user click on the close button on this window
     *
     * @param windowEvent provided by FXML, if the event is consumed in the method the window will not be closed
     */
    public void onWindowClose(WindowEvent windowEvent) {
        int editIndex = listPresets.getEditingIndex();

        // A value greater than -1 means that one (or more) items in the list is currently being edited
        // Warn the users that unsaved changes will be lost if they close now.
        if (editIndex != -1) {
            ButtonType userChoice = AlertUtils.yesNo(transPresetWin.getString("diag_closeWarn_short"),
                    transPresetWin.getString("diag_closeWarn_full"), ButtonType.NO);
            if (userChoice == ButtonType.NO) {
                windowEvent.consume(); // do not close the window
                return;
            }
        }

        //TODO: call some kind of state validation: check category and playlist are valid choices and have not been deleted

        presetManager.getAllPresets().removeListener(presetChangeListener);
    }

    /**
     * Called when the add preset button is clicked.
     * Takes the text in the text field to the left of the button and creates a preset with that name.
     * The text field is not allowed to be empty or have the name of a already existing preset.
     * @param actionEvent the click event
     */
    public void onPresetAddNewClicked(ActionEvent actionEvent) {
        // Test so the name of the new preset is not blank or the same as an existing one
        if (txt_nameNewPreset.getText().strip().equals("")) {
            AlertUtils.simpleClose(transPresetWin.getString("diag_presetNeedName_short"),
                    transPresetWin.getString("diag_presetNeedName_full")).show();
            return;
        }
        for (var videoPreset : listPresets.getItems()) {
            if (videoPreset.getModel().getPresetName().equals(txt_nameNewPreset.getText())) {
                AlertUtils.simpleClose(transPresetWin.getString("diag_presetExist_short"),
                        transPresetWin.getString("diag_presetExist_full")).show();
                return;
            }
        }

        // Create the new preset, enable editing on it and scroll so it is in focus (in case the user has a lot of presets)
        NewVideoPresetModel newVideoPreset = new NewVideoPresetModel();
        newVideoPreset.setPresetName(txt_nameNewPreset.getText());
        newVideoPreset.setVisibility(VisibilityStatus.PUBLIC);
        newVideoPreset.setMadeForKids(false);

        presetManager.addPreset(newVideoPreset);

        // bind width??
        // enable edit
        // change cancel to delete?

        listPresets.scrollTo(listPresets.getItems().size() - 1);
        txt_nameNewPreset.setText("");
        actionEvent.consume();
    }

    /**
     * Called when the localize categories button is clicked.
     * Opens the localize categories window.
     * @param actionEvent the click event
     */
    public void onLocalizeCategoriesClicked(ActionEvent actionEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(PresetsWindowController.class.getClassLoader().getResource("fxml/LocalizeCategoriesWindow.fxml"));
            MyStage stage = new MyStage(WindowPropertyNames.LOCALIZE);
            stage.makeScene(fxmlLoader.load(), Constants.LOCALIZE_WINDOW_DIMENSIONS_RESTRICTION);
            stage.setTitle(transBasic.getString("app_locCatWindowTitle"));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.prepareControllerAndShowAndWait(fxmlLoader.getController());
        } catch (IOException e) {
            e.printStackTrace();
        }
        actionEvent.consume();
    }

    /**
     * Called when the manage playlists button is clicked.
     * Opens the manage playlists window
     * @param actionEvent the click event
     */
    public void onManagePlaylistsClicked(ActionEvent actionEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(PresetsWindowController.class.getClassLoader().getResource("fxml/ManagePlaylistsWindow.fxml"));
            MyStage stage = new MyStage(WindowPropertyNames.PLAYLISTS);
            stage.makeScene(fxmlLoader.load(), Constants.PLAYLISTS_WINDOW_DIMENSIONS_RESTRICTION);
            stage.setTitle(transBasic.getString("app_manPlayWindowTitle"));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.prepareControllerAndShowAndWait(fxmlLoader.getController());
        } catch (IOException e) {
            e.printStackTrace();
        }
        actionEvent.consume();
    }

    /**
     * Called when the delete button on a preset is clicked.
     * Shows a confirmation dialog and if the user press yes, delete the preset.
     *
     * @param presetModel the NewVideoPresetModel the delete action fired for
     */
    private void onPresetDelete(NewVideoPresetModel presetModel) {

        String desc = String.format(transPresetWin.getString("diag_presetDelete_full"),
                presetModel.getPresetName());

        ButtonType userChoice = AlertUtils.yesNo(transPresetWin.getString("diag_presetDelete_short"),
                desc, ButtonType.NO);
        if (userChoice == ButtonType.YES) {
            presetManager.removePreset(presetModel);
        } //else if ButtonType.NO or closed [X] do nothing

    }

    /**
     * Creates a copy of the preset this button belongs to
     *
     * @param clonedPresetModel A clone of the clicked preset that must be saved
     */
    private void onPresetClone(NewVideoPresetModel clonedPresetModel) {

        // bind dimensions??
        //copy.getPane().prefWidthProperty().bind(listPresets.widthProperty().subtract(35));

        presetManager.addPreset(clonedPresetModel);

        // translate?
        //transPreset.autoTranslate(copy.getPane(), copy.getPaneId());

        // set state and buttons?
    }
}