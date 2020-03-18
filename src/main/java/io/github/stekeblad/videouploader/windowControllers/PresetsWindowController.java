package io.github.stekeblad.videouploader.windowControllers;

import io.github.stekeblad.videouploader.jfxExtension.MyStage;
import io.github.stekeblad.videouploader.utils.AlertUtils;
import io.github.stekeblad.videouploader.utils.ConfigManager;
import io.github.stekeblad.videouploader.utils.Constants;
import io.github.stekeblad.videouploader.utils.FileUtils;
import io.github.stekeblad.videouploader.utils.background.OpenInBrowser;
import io.github.stekeblad.videouploader.utils.state.ButtonProperties;
import io.github.stekeblad.videouploader.utils.state.VideoPresetState;
import io.github.stekeblad.videouploader.utils.translation.TranslationBundles;
import io.github.stekeblad.videouploader.utils.translation.Translations;
import io.github.stekeblad.videouploader.utils.translation.TranslationsManager;
import io.github.stekeblad.videouploader.youtube.VideoPreset;
import io.github.stekeblad.videouploader.youtube.utils.CategoryUtils;
import io.github.stekeblad.videouploader.youtube.utils.VisibilityStatus;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import static io.github.stekeblad.videouploader.utils.Constants.*;
import static io.github.stekeblad.videouploader.youtube.VideoInformationBase.MAX_THUMB_SIZE;
import static io.github.stekeblad.videouploader.youtube.VideoInformationBase.THUMBNAIL_FILE_FORMAT;


public class PresetsWindowController {

    public AnchorPane presetWindow;
    public ListView<GridPane> listPresets;
    public ToolBar toolbar;
    public Button btn_tips;
    public Button btn_addNewPreset;
    public Button btn_localizeCategories;
    public Button btn_managePlaylists;
    public TextField txt_nameNewPreset;
    public Label label_savedPresets;

    private ArrayList<VideoPreset> videoPresets;
    private ConfigManager configManager;
    private CategoryUtils categoryUtils;
    private int presetCounter = 0;
    private HashMap<String, VideoPreset> presetBackups;
    private VideoPresetState buttonStates;
    private boolean disableLocaleChange;

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

        configManager = ConfigManager.INSTANCE;
        categoryUtils = CategoryUtils.INSTANCE;

        presetBackups = new HashMap<>();
        videoPresets = new ArrayList<>();

        // It will cause problems if locale is changed while something is being edited or uploaded
        this.disableLocaleChange = disableLocaleChange;
        setLocaleButtonDisabled();

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

        // Set up button sets for different preset states (locked, editing)
        definePresetStates();

        // Load all saved presets
        ArrayList<String> savedPresetNames = configManager.getPresetNames();
        if (savedPresetNames != null) {
            for (String presetName : savedPresetNames) {
                VideoPreset videoPreset;
                try {
                    videoPreset = new VideoPreset(configManager.getPresetString(presetName), PRESET_PANE_ID_PREFIX + presetCounter);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Failed loading preset: " + presetName);
                    AlertUtils.exceptionDialog("Could not load preset", "An error occurred while trying to " +
                            "load the preset " + presetName + ". \nThis may be because it has been externally modified or " +
                            "because the selected thumbnail file can not be found. \nMore details below.", e);
                    continue;
                }
                videoPreset.setThumbnailCursorEventHandler(this::updateCursor);
                videoPreset.getPane().prefWidthProperty().bind(listPresets.widthProperty().subtract(35)); // Auto Resize width
                transPreset.autoTranslate(videoPreset.getPane(), videoPreset.getPaneId());
                buttonStates.setLocked(videoPreset);
                videoPresets.add(videoPreset);

                presetCounter++;
            }
        }
        updatePresetList();

        // Set so pressing enter in txt_nameNewPreset triggers onPresetAddNewClicked
        txt_nameNewPreset.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                onPresetAddNewClicked(new ActionEvent());
                event.consume();
            } // On Java 8, function key events is not passed on by TextFields, lets add it because handler already exists (Bug fixed in Java 9)
            if (event.getCode() == KeyCode.F1) {
                OpenInBrowser.openInBrowser("https://github.com/Stekeblad/Stekeblads-Video-Uploader/wiki/Preset-Window");
                event.consume();
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

    /**
     * Executed when the user click on the close button on this window
     * @param windowEvent provided by FXML, if the event is consumed in the method the window will not be closed
     */
    public void onWindowClose(WindowEvent windowEvent) {
        boolean editingPreset = false;
        for (VideoPreset aPreset : videoPresets) {
            if (aPreset.getButton2Id().contains(BUTTON_SAVE)) {
                editingPreset = true;
                break;
            }
        }
        if(editingPreset) {
            ButtonType userChoice = AlertUtils.yesNo(transPresetWin.getString("diag_closeWarn_short"),
                    transPresetWin.getString("diag_closeWarn_full"), ButtonType.NO);
            if (userChoice == ButtonType.NO) {
                windowEvent.consume(); // do not close the window
            }
        }
    }

    /**
     * Called when the add preset button is clicked.
     * Takes the text in the text field to the left of the button and creates a preset with that name.
     * The text field is not allowed to be empty or have the name of a already existing preset.
     * @param actionEvent the click event
     */
    public void onPresetAddNewClicked(ActionEvent actionEvent) {
        // Test so the name of the new preset is not blank or the same as an existing one
        if (txt_nameNewPreset.getText().equals("")) {
            AlertUtils.simpleClose(transPresetWin.getString("diag_presetNeedName_short"),
                    transPresetWin.getString("diag_presetNeedName_full")).show();
            return;
        }
        for (VideoPreset videoPreset : videoPresets) {
            if (videoPreset.getPresetName().equals(txt_nameNewPreset.getText())) {
                AlertUtils.simpleClose(transPresetWin.getString("diag_presetExist_short"),
                        transPresetWin.getString("diag_presetExist_full")).show();
                return;
            }
        }

        // Create the new preset, enable editing on it and scroll so it is in focus (in case the user has a lot of presets)
        VideoPreset newPreset = new VideoPreset("", "", VisibilityStatus.PUBLIC, null,
                null, null, false, null, false,
                PRESET_PANE_ID_PREFIX + presetCounter, txt_nameNewPreset.getText());
        // Make so the preset change its width together with the list and the window
        newPreset.getPane().prefWidthProperty().bind(listPresets.widthProperty().subtract(35));
        transPreset.autoTranslate(newPreset.getPane(), newPreset.getPaneId());

        newPreset.setThumbnailCursorEventHandler(this::updateCursor);
        videoPresets.add(newPreset);
        onPresetEdit(PRESET_PANE_ID_PREFIX + presetCounter + "_fakeButton");

        // Change the cancel button to a delete button, the backed up state created by onEdit is not valid
        Button deleteButton = new Button(transBasic.getString("delete"));
        deleteButton.setId(PRESET_PANE_ID_PREFIX + presetCounter + BUTTON_DELETE);
        deleteButton.setOnMouseClicked(event -> onPresetDelete(deleteButton.getId()));
        videoPresets.get(videoPresets.size() - 1).setButton2(deleteButton);

        listPresets.scrollTo(listPresets.getItems().size() -1);
        txt_nameNewPreset.setText("");
        presetCounter++;
        updatePresetList();
        actionEvent.consume();
    }

    /**
     * Called when the tips button is clicked.
     * Shows a small dialog with a few tips and warnings.
     * @param actionEvent the click event
     */
    public void onTipsClicked(ActionEvent actionEvent) {
        AlertUtils.simpleClose_longContent("Tips", transPresetWin.getString("diag_tips"));
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
            MyStage stage = new MyStage(ConfigManager.WindowPropertyNames.LOCALIZE);
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
            MyStage stage = new MyStage(ConfigManager.WindowPropertyNames.PLAYLISTS);
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
     * Defines the different states for buttonStates, this is made to make it much simpler to manage the buttons
     * for the different states a preset can be in. Previously buttons was created all over the place and set
     * *  to call methods. Now there is sets of buttons defined here and this sets is then used when setting the
     * *  buttons to show.
     */
    private void definePresetStates() {
        buttonStates = new VideoPresetState();

        // Define Locked from editing
        buttonStates.defineLocked(new ButtonProperties[]{
                new ButtonProperties(BUTTON_EDIT, transBasic.getString("edit"), this::onPresetEdit),
                new ButtonProperties(BUTTON_DELETE, transBasic.getString("delete"), this::onPresetDelete),
                new ButtonProperties(BUTTON_CLONE, transBasic.getString("clone"), this::onPresetClone)
        });

        buttonStates.defineEditing(new ButtonProperties[]{
                new ButtonProperties(BUTTON_SAVE, transBasic.getString("save"), this::onPresetSave),
                new ButtonProperties(BUTTON_CANCEL, transBasic.getString("cancel"), this::onPresetCancelEdit),
                new ButtonProperties("_ghost", "", null)
        });
    }

    /**
     * Re-adds all presets to the listPreset to make sure the UI is up-to-date
     */
    private void updatePresetList() {
        ArrayList<GridPane> presetPanes = new ArrayList<>();
        for (VideoPreset videoPreset : videoPresets) {
            presetPanes.add(videoPreset.getPane());
        }
        listPresets.setItems(FXCollections.observableArrayList(presetPanes));
    }

    /**
     * Returns the index in videoPresets that has a preset with the paneId nameToTest or -1 if no preset has that name
     * @param nameToTest paneId to test for
     * @return the index of where the preset with the paneId nameToTest inside videoPresets or -1 if it does not exist a preset with that paneId
     */
    private int getPresetIndexByPaneId(String nameToTest) {
        int presetIndex = -1;
        for (int i = 0; i < videoPresets.size(); i++) {
            if (videoPresets.get(i).getPaneId().equals(nameToTest)) {
                presetIndex = i;
                break;
            }
        }
        return presetIndex;
    }

    /**
     * Set this method to trigger when the cursor enters or leaves a node to change how it looks.
     *
     * @param entered if true, sets the cursor to a pointing hand (usually on enter event).
     *                if false, sets the cursor to default (usually on exit event).
     */
    private void updateCursor(boolean entered) {
        if (entered) {
            presetWindow.getScene().setCursor(Cursor.HAND);
        } else {
            presetWindow.getScene().setCursor(Cursor.DEFAULT);
        }
    }

    /**
     * Updates the enabled/disabled state for the localize categories button, categories should not be re-localized if
     * one or more videos/presets is in edit mode or an upload is in progress because updates WILL fail and preset/uploads
     * that was in edit mode will not be possible to save!
     */
    private void setLocaleButtonDisabled() {
        if (disableLocaleChange) {
            btn_localizeCategories.setDisable(true);
        } else {
            btn_localizeCategories.setDisable(!presetBackups.isEmpty());

        }
    }

    /**
     * Called when the edit button on a preset is clicked.
     * Enables editing of the preset and takes a backup of it to be able to revert
     * @param callerId the id of the preset + button name
     */
    private void onPresetEdit(String callerId) {
        String parentId = callerId.substring(0, callerId.indexOf('_'));
        int selected = getPresetIndexByPaneId(parentId);
        if(selected == -1) {
            System.err.println("Non-existing edit button was pressed!!!");
            return;
        }

        // create backup and enable editing
        presetBackups.put(videoPresets.get(selected).getPaneId(), videoPresets.get(selected).copy(null));
        videoPresets.get(selected).setEditable(true);

        // Sets the thumbnail clickable for changing
        videoPresets.get(selected).setOnThumbnailClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY)
                return; // Conflicting with context menu, only do this on left click
            File pickedThumbnail = FileUtils.pickThumbnail(THUMBNAIL_FILE_FORMAT, MAX_THUMB_SIZE);
            if(pickedThumbnail != null) {
                try {
                    videoPresets.get(selected).setThumbNailFile(pickedThumbnail);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        // Sets the thumbnail right click context menu
        ContextMenu thumbnailRClickMenu = new ContextMenu();
        MenuItem item1 = new MenuItem(transBasic.getString("resetToDefault"));
        item1.setOnAction(actionEvent -> {
            try {
                videoPresets.get(selected).setThumbNailFile(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            actionEvent.consume();
        });
        thumbnailRClickMenu.getItems().add(item1);
        videoPresets.get(selected).setThumbnailContextMenu(thumbnailRClickMenu);

        buttonStates.setEditing(videoPresets.get(selected));
        setLocaleButtonDisabled();
    }

    /**
     * Called when the save button on a preset is clicked.
     * Saves the changes, disables editing and deletes the backup.
     * @param callerId the preset id + button name
     */
    private void onPresetSave(String callerId) {
        String parentId = callerId.substring(0, callerId.indexOf('_'));
        // locate this preset
        int selected = getPresetIndexByPaneId(parentId);
        if(selected == -1) {
            System.err.println("Can't find witch preset to save");
            return;
        }
        // Make sure a category is selected and the category name still match a stored category
        // (will not match stored if categories have been re-localized)
        if (videoPresets.get(selected).getCategory() == null) {
            AlertUtils.simpleClose(transBasic.getString("diag_invalidCategory_short"),
                    transBasic.getString("diag_invalidCategory_full")).show();
            return;
        }
        if (!categoryUtils.getCategoryNames().contains(videoPresets.get(selected).getCategory())) {
            AlertUtils.simpleClose(transBasic.getString("diag_categoryRemoved_short"),
                    transBasic.getString("diag_categoryRemoved_full")).show();
            return;
        }
        // make sure preset name is not empty
        if(videoPresets.get(selected).getPresetName().equals("")) {
            AlertUtils.simpleClose(transPresetWin.getString("diag_presetNeedName_short"),
                    transPresetWin.getString("diag_presetNeedName_full")).show();
            return;
        }
        // Test if the preset name has been changed and now is equal to another preset, if so abort saving
        int otherPreset = -1;
        for (int i = 0; i < videoPresets.size(); i++) {
            if (i == selected) {
                continue; // this case is not interesting
            }
            if (videoPresets.get(i).getPresetName().equals(videoPresets.get(selected).getPresetName())) {
                otherPreset = i;
                break;
            }
        }
        if (otherPreset > -1) {
            AlertUtils.simpleClose(transPresetWin.getString("diag_presetExist_short"),
                    transPresetWin.getString("diag_presetExist_full")).show();
            return;
        }
        // if there is a backup it needs to be deleted
        if (presetBackups.containsKey(videoPresets.get(selected).getPaneId())) {
            // if preset name changed then the preset save file needs to be changed
            String oldPresetName = presetBackups.get(videoPresets.get(selected).getPaneId()).getPresetName();
            if (!videoPresets.get(selected).getPresetName().equals(oldPresetName)) {
                configManager.deletePreset(oldPresetName);
            }
            presetBackups.remove(videoPresets.get(selected).getPaneId());
        }
        videoPresets.get(selected).setEditable(false);
        configManager.savePreset(videoPresets.get(selected).getPresetName(), videoPresets.get(selected).toString());
        buttonStates.setLocked(videoPresets.get(selected));
        setLocaleButtonDisabled();
    }

    /**
     * Called when the cancel button on a preset is clicked.
     * Disables editing and reverts the preset to how it was before editing.
     * @param callerId the preset id + button name
     */
    private void onPresetCancelEdit(String callerId) {
        String parentId = callerId.substring(0, callerId.indexOf('_'));
        int selected = getPresetIndexByPaneId(parentId);
        if (selected == -1) {
            System.err.println("Non-existing cancelEdit button was pressed!!!");
            return;
        }

        videoPresets.get(selected).setEditable(false);

        // test for the existence of a backup
        if (! presetBackups.containsKey(videoPresets.get(selected).getPaneId())) {
            // If no backup, assume preset is a newly added not saved preset, delete it directly
            videoPresets.remove(selected);
            updatePresetList();
            setLocaleButtonDisabled();
            return;
        } else {
            // restore backup
            videoPresets.set(selected, presetBackups.get(videoPresets.get(selected).getPaneId()));
            videoPresets.get(selected).getPane().prefWidthProperty().bind(listPresets.widthProperty().subtract(35));
            presetBackups.remove(videoPresets.get(selected).getPaneId());
        }

        buttonStates.setLocked(videoPresets.get(selected));
        updatePresetList();
        setLocaleButtonDisabled();
    }

    /**
     * Called when the delete button on a preset is clicked.
     * Shows a confirmation dialog and if the user press yes, delete the preset.
     * @param callerId the preset id + button name
     */
    private void onPresetDelete(String callerId) {
        String parentId = callerId.substring(0, callerId.indexOf('_'));
        int selected = getPresetIndexByPaneId(parentId);
        if(selected == -1) {
            System.err.println("Non-existing delete button was pressed!!!");
            return;
        }
        String desc = String.format(transPresetWin.getString("diag_presetDelete_full"),
                videoPresets.get(selected).getPresetName());

        ButtonType userChoice = AlertUtils.yesNo(transPresetWin.getString("diag_presetDelete_short"),
                desc, ButtonType.NO);
        if (userChoice == ButtonType.YES) {
            if (!configManager.deletePreset(videoPresets.get(selected).getPresetName())) {
                AlertUtils.simpleClose(transBasic.getString("error"), "Could not delete preset").show();
            } else {
                videoPresets.remove(selected);
                updatePresetList();
                setLocaleButtonDisabled();
            }
        } //else if ButtonType.NO or closed [X] do nothing

    }

    /**
     * Creates a copy of the preset this button belongs to
     * @param callerId the preset id + button name
     */
    private void onPresetClone(String callerId) {
        String parentId = callerId.substring(0, callerId.indexOf('_'));
        int selected = getPresetIndexByPaneId(parentId);
        if (selected == -1) {
            System.err.println("Non-existing clone button was pressed!!!");
            return;
        }

        VideoPreset orig = videoPresets.get(selected);
        VideoPreset copy = orig.copy(PRESET_PANE_ID_PREFIX + presetCounter);
        copy.setPresetName(transPresetWin.getString("copyOf") + orig.getPresetName());
        copy.getPane().prefWidthProperty().bind(listPresets.widthProperty().subtract(35));
        copy.setThumbnailCursorEventHandler(this::updateCursor);

        transPreset.autoTranslate(copy.getPane(), copy.getPaneId());
        buttonStates.setEditing(copy);
        videoPresets.add(selected + 1, copy); // add right after original in list
        onPresetEdit(PRESET_PANE_ID_PREFIX + presetCounter + "_fakeButton");

        // Change the cancel button to a delete button, the backed up state created by onEdit is not valid
        Button deleteButton = new Button(transBasic.getString("delete"));
        deleteButton.setId(PRESET_PANE_ID_PREFIX + presetCounter + BUTTON_DELETE);
        deleteButton.setOnMouseClicked(event -> onPresetDelete(deleteButton.getId()));
        videoPresets.get(videoPresets.size() - 1).setButton2(deleteButton);

        presetCounter++;
        updatePresetList();
        listPresets.scrollTo(selected + 1);
    }
}