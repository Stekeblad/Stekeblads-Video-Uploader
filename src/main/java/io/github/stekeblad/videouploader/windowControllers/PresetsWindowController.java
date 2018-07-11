package io.github.stekeblad.videouploader.windowControllers;

import io.github.stekeblad.videouploader.utils.*;
import io.github.stekeblad.videouploader.utils.background.OpenInBrowser;
import io.github.stekeblad.videouploader.youtube.VideoPreset;
import io.github.stekeblad.videouploader.youtube.utils.CategoryUtils;
import io.github.stekeblad.videouploader.youtube.utils.PlaylistUtils;
import io.github.stekeblad.videouploader.youtube.utils.VisibilityStatus;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import static io.github.stekeblad.videouploader.utils.Constants.*;


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
    private PlaylistUtils playlistUtils;
    private CategoryUtils categoryUtils;
    private int presetCounter = 0;
    private HashMap<String, VideoPreset> presetBackups;

    private Translations transPresetWin;
    private Translations transBasic;
    private Translations transPreset;
    
    private static final String PRESET_PANE_ID_PREFIX = "preset-";

    /**
     * Initialize a few things when the window is opened, used instead of initialize as that one does not have access to the scene
     */
    public void myInit() {
        configManager = ConfigManager.INSTANCE;
        playlistUtils = PlaylistUtils.INSTANCE;
        categoryUtils = CategoryUtils.INSTANCE;

        presetBackups = new HashMap<>();
        videoPresets = new ArrayList<>();

        // Load Translations
        transPresetWin = TranslationsManager.getTranslation("presetWindow");
        transBasic = TranslationsManager.getTranslation("baseStrings");
        transPresetWin.autoTranslate(presetWindow);
        transPreset = TranslationsManager.getTranslation("presetsUploads");

        // Children of Toolbars can not be detected through code currently (probably a bug)
        btn_managePlaylists.setText(transPresetWin.getString("btn_managePlaylists"));
        btn_managePlaylists.setTooltip(new Tooltip(transPresetWin.getString("btn_managePlaylists_tt")));
        btn_localizeCategories.setText(transPresetWin.getString("btn_localizeCategories"));
        btn_localizeCategories.setTooltip(new Tooltip(transPresetWin.getString("btn_localizeCategories_tt")));
        btn_tips.setText(transPresetWin.getString("btn_tips"));
        btn_addNewPreset.setText(transPresetWin.getString("btn_addNewPreset"));
        txt_nameNewPreset.setPromptText(transPresetWin.getString("txt_nameNewPreset_pt"));

        // Load all saved presets
        ArrayList<String> savedPresetNames = configManager.getPresetNames();
        if (savedPresetNames != null) {
            for (String presetName : savedPresetNames) {
                VideoPreset videoPreset;
                try {
                    videoPreset = new VideoPreset(configManager.getPresetString(presetName), PRESET_PANE_ID_PREFIX + presetCounter);
                } catch (Exception e) { // thumbnail file no longer available is handled as no thumbnail is selected and using default, change please?
                    e.printStackTrace();
                    System.err.println("Failed loading preset: " + presetName);
                    AlertUtils.exceptionDialog("Could not load preset", "An error occurred while trying to " +
                            "load the preset " + presetName + ". This may be because it has been externally modified or " +
                            "because the selected thumbnail file can not be found. More details below.", e);
                    continue;
                }
                videoPreset.setThumbnailCursorEventHandler(this::updateCursor);

                Button editButton = new Button(transBasic.getString("edit"));
                editButton.setId(PRESET_PANE_ID_PREFIX + presetCounter + BUTTON_EDIT);
                editButton.setOnMouseClicked(event -> onPresetEdit(editButton.getId()));
                Button deleteButton = new Button(transBasic.getString("delete"));
                deleteButton.setId(PRESET_PANE_ID_PREFIX + presetCounter + BUTTON_DELETE);
                deleteButton.setOnMouseClicked(event -> onPresetDelete(deleteButton.getId()));
                videoPreset.setButton1(editButton);
                videoPreset.setButton2(deleteButton);
                videoPreset.getPane().prefWidthProperty().bind(listPresets.widthProperty()); // Auto Resize width
                transPreset.autoTranslate(videoPreset.getPane(), videoPreset.getPaneId());
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
        for(VideoPreset aPreset : videoPresets) {
            if(aPreset.getButton2Id().contains(BUTTON_SAVE)) {
                editingPreset = true;
                break;
            }
        }
        if(editingPreset) {
            Optional<ButtonType> buttonChoice = AlertUtils.yesNo(transPresetWin.getString("diag_closeWarn_short"),
                    transPresetWin.getString("diag_closeWarn_full")).showAndWait();
            if(buttonChoice.isPresent()) {
                if(buttonChoice.get() == ButtonType.NO) {
                    windowEvent.consume(); // do not close the window
                }
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
                null, null, false, null,
                PRESET_PANE_ID_PREFIX + presetCounter, txt_nameNewPreset.getText());
        // Make so the preset change its width together with the list and the window
        newPreset.getPane().prefWidthProperty().bind(listPresets.widthProperty());
        transPreset.autoTranslate(newPreset.getPane(), newPreset.getPaneId());

        newPreset.setThumbnailCursorEventHandler(this::updateCursor);
        videoPresets.add(newPreset);
        onPresetEdit(PRESET_PANE_ID_PREFIX + presetCounter + "_fakeButton");
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
            Scene scene = new Scene(fxmlLoader.load(), 400, 450);
            Stage stage = new Stage();
            stage.setMinWidth(400);
            stage.setMinHeight(450);
            stage.setMaxWidth(400);
            stage.setMaxHeight(450);
            stage.setTitle(transBasic.getString("app_locCatWindowTitle"));
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            LocalizeCategoriesWindowController controller = fxmlLoader.getController();
            controller.myInit();
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
        actionEvent.consume();
    }

    /**
     * Called when the refresh playlist button is clicked.
     * Downloads the user's playlist from youtube
     * @param actionEvent the click event
     */
    public void onManagePlaylistsClicked(ActionEvent actionEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(PresetsWindowController.class.getClassLoader().getResource("fxml/ManagePlaylistsWindow.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 400, 500);
            Stage stage = new Stage();
            stage.setMinWidth(350);
            stage.setMinHeight(250);
            stage.setTitle(transBasic.getString("app_manPlayWindowTitle"));
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            ManagePlaylistsWindowController controller = fxmlLoader.getController();
            stage.setOnCloseRequest(controller::onWindowClose);
            controller.myInit();
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
        actionEvent.consume();
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
            File pickedThumbnail = FileUtils.pickThumbnail();
            if(pickedThumbnail != null) {
                try {
                    videoPresets.get(selected).setThumbNailFile(pickedThumbnail);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // set the playlists list
        videoPresets.get(selected).setOnPlaylistsClicked(event -> {
            if (configManager.getNeverAuthed()) {
                onManagePlaylistsClicked(new ActionEvent());
            }
            if (!configManager.getNeverAuthed()) {
                videoPresets.get(selected).setPlaylists(playlistUtils.getVisiblePlaylistnames());
            }
        });

        //set categories
        videoPresets.get(selected).setOnCategoriesClicked(event ->
                videoPresets.get(selected).setCategories(categoryUtils.getCategoryNames())
        );

        // Change buttons from "edit" and "delete" to "save" and "cancel"
        Button saveButton = new Button(transBasic.getString("save"));
        saveButton.setId(parentId + BUTTON_SAVE);
        saveButton.setOnMouseClicked(event-> onPresetSave(saveButton.getId()));
        Button cancelButton = new Button(transBasic.getString("cancel"));
        cancelButton.setId(parentId + BUTTON_CANCEL);
        cancelButton.setOnMouseClicked(event-> onPresetCancelEdit(cancelButton.getId()));

        videoPresets.get(selected).setButton1(cancelButton);
        videoPresets.get(selected).setButton2(saveButton);
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
        if (videoPresets.get(selected).getCategory() == null &&
                !categoryUtils.getCategoryId(videoPresets.get(selected).getCategory()).equals("-1")) {
            AlertUtils.simpleClose(transBasic.getString("diag_invalidCategory_short"),
                    transBasic.getString("diag_invalidCategory_full")).show();
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

        //change back buttons
        Button editButton = new Button(transBasic.getString("edit"));
        editButton.setId(parentId + BUTTON_EDIT);
        editButton.setOnMouseClicked(event -> onPresetEdit(editButton.getId()));
        Button deleteButton = new Button(transBasic.getString("delete"));
        deleteButton.setId(parentId + BUTTON_DELETE);
        deleteButton.setOnMouseClicked(event -> onPresetDelete(deleteButton.getId()));

        videoPresets.get(selected).setButton1(editButton);
        videoPresets.get(selected).setButton2(deleteButton);
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
            return;
        } else {
            // restore backup
            videoPresets.set(selected, presetBackups.get(videoPresets.get(selected).getPaneId()));
            videoPresets.get(selected).getPane().prefWidthProperty().bind(listPresets.widthProperty());
            presetBackups.remove(videoPresets.get(selected).getPaneId());
        }

        // Test if this preset exist on disc, if not it should be deleted from the UI
        ArrayList<String> presetNames = configManager.getPresetNames();
        if (presetNames != null && !presetNames.contains(videoPresets.get(selected).getPresetName())) {
            videoPresets.remove(selected);
            updatePresetList();
            return;
        }
        updatePresetList();

        //change buttons
        Button editButton = new Button(transBasic.getString("edit"));
        editButton.setId(parentId + BUTTON_EDIT);
        editButton.setOnMouseClicked(event -> onPresetEdit(editButton.getId()));
        Button deleteButton = new Button(transBasic.getString("delete"));
        deleteButton.setId(parentId + BUTTON_DELETE);
        deleteButton.setOnMouseClicked(event -> onPresetDelete(deleteButton.getId()));

        videoPresets.get(selected).setButton1(editButton);
        videoPresets.get(selected).setButton2(deleteButton);
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
        Optional<ButtonType> buttonChoice = AlertUtils.yesNo(
                transPresetWin.getString("diag_presetDelete_short"), desc).showAndWait();
        if(buttonChoice.isPresent()) {
            if(buttonChoice.get() == ButtonType.YES) {
                if (!configManager.deletePreset(videoPresets.get(selected).getPresetName())) {
                    AlertUtils.simpleClose(transBasic.getString("error"), "Could not delete preset").show();
                } else {
                    videoPresets.remove(selected);
                    updatePresetList();
                }
            } //else if ButtonType.NO or closed [X] do nothing
        }
    }
}
