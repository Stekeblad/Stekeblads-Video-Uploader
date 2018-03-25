package io.github.stekeblad.videouploader.settings;

import io.github.stekeblad.videouploader.utils.AlertUtils;
import io.github.stekeblad.videouploader.utils.ConfigManager;
import io.github.stekeblad.videouploader.utils.PickFile;
import io.github.stekeblad.videouploader.utils.Translations;
import io.github.stekeblad.videouploader.youtube.VideoPreset;
import io.github.stekeblad.videouploader.youtube.utils.CategoryUtils;
import io.github.stekeblad.videouploader.youtube.utils.PlaylistUtils;
import io.github.stekeblad.videouploader.youtube.utils.VisibilityStatus;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import static io.github.stekeblad.videouploader.utils.Constants.*;


public class PresetsWindowController implements Initializable {

    public AnchorPane settingsWindow;
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
    
    private static final String PRESET_PANE_ID_PREFIX = "preset-";

    /**
     * Initialize a few things when the window is opened
     * @param location provided by fxml
     * @param resources provided by fxml
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configManager = ConfigManager.INSTANCE;
        playlistUtils = PlaylistUtils.INSTANCE;
        categoryUtils = CategoryUtils.INSTANCE;

        presetBackups = new HashMap<>();
        videoPresets = new ArrayList<>();

        // Load Translations
        try {
            transPresetWin = new Translations("presetWindow");
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.simpleClose("Error loading translations", "Failed loading translations for preset" +
                    " window, the window can not be opened. Sorry!\n\nDetected language: " + Locale.getDefault())
                    .showAndWait();
            return;
        }
        try {
            transBasic = new Translations("baseStrings");
        } catch (Exception e) {
            AlertUtils.simpleClose("Error loading translations", "Failed loading basic translations" +
                    ", the window can not be opened. Sorry!\n\nDetected language: " + Locale.getDefault()).showAndWait();
            return;
        }
        transPresetWin.autoTranslate(settingsWindow);

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
                try {
                    VideoPreset videoPreset = new VideoPreset(configManager.getPresetString(presetName), PRESET_PANE_ID_PREFIX + presetCounter);
                    Button editButton = new Button(transBasic.getString("edit"));
                    editButton.setId(PRESET_PANE_ID_PREFIX + presetCounter + BUTTON_EDIT);
                    editButton.setOnMouseClicked(event -> onPresetEdit(editButton.getId()));
                    Button deleteButton = new Button(transBasic.getString("delete"));
                    deleteButton.setId(PRESET_PANE_ID_PREFIX + presetCounter + BUTTON_DELETE);
                    deleteButton.setOnMouseClicked(event -> onPresetDelete(deleteButton.getId()));
                    videoPreset.setButton1(editButton);
                    videoPreset.setButton2(deleteButton);
                    videoPresets.add(videoPreset);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("Trying to load a preset that does not exist or missing read permission: " + presetName);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Bad format of preset or is another type of preset then the one trying to be created: " + presetName);
                }
                presetCounter++;
            }
        }
        updatePresetList();
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
                null, null, false, null, PRESET_PANE_ID_PREFIX + presetCounter, txt_nameNewPreset.getText());
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
        AlertUtils.simpleClose("Tips", transPresetWin.getString("diag_tips")).showAndWait();
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
            presetPanes.add(videoPreset.getPresetPane());
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
            if (videoPresets.get(i).getPresetPane().getId().equals(nameToTest)) {
                presetIndex = i;
                break;
            }
        }
        return presetIndex;
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
            File pickedThumbnail = PickFile.pickThumbnail();
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
        // Make sure the category selected is valid
        if (videoPresets.get(selected).getCategory() == null) {
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
