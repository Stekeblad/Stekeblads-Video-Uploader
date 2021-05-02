package io.github.stekeblad.videouploader.ListControllers;

import io.github.stekeblad.videouploader.models.NewVideoPresetModel;
import io.github.stekeblad.videouploader.utils.translation.TranslationBundles;
import io.github.stekeblad.videouploader.utils.translation.Translations;
import io.github.stekeblad.videouploader.utils.translation.TranslationsManager;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.RowConstraints;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class PresetItemController extends VideoInfoItemController<NewVideoPresetModel> implements Comparable<PresetItemController> {
    // FX Nodes
    public TextField presetName;
    public Button saveButton;
    public Button editButton;
    public Button cancelButton;
    public Button deleteButton;
    public Button cloneButton;

    private Consumer<NewVideoPresetModel> deleteButtonActionConsumer;
    private Consumer<NewVideoPresetModel> cloneButtonActionConsumer;

    private static final String NODE_ID_PRESET_NAME = "presetName";
    private static final String NODE_ID_BUTTON_SAVE = "save";
    private static final String NODE_ID_BUTTON_EDIT = "edit";
    private static final String NODE_ID_BUTTON_CANCEL = "cancel";
    private static final String NODE_ID_BUTTON_DELETE = "delete";
    private static final String NODE_ID_BUTTON_CLONE = "clone";

    private static final Translations transPreset = TranslationsManager.getTranslation(TranslationBundles.PRESET_UPLOAD);

    public PresetItemController(NewVideoPresetModel videoPreset, ReadOnlyDoubleProperty parentPrefWidthProperty) {
        super(videoPreset, parentPrefWidthProperty);
        extendPane();
        transPreset.autoTranslate(this);
    }

    public NewVideoPresetModel getModel() {
        return this.model;
    }

    public void registerDeleteButtonActionHandler(Consumer<NewVideoPresetModel> deleteActionHandler) {
        deleteButtonActionConsumer = deleteActionHandler;
    }

    public void registerCloneButtonActionHandler(Consumer<NewVideoPresetModel> cloneActionHandler) {
        cloneButtonActionConsumer = cloneActionHandler;
    }

    public void delete(ActionEvent actionEvent) {
        if (deleteButtonActionConsumer != null)
            deleteButtonActionConsumer.accept(model);
    }

    public void clone(ActionEvent actionEvent) {
        if (cloneButtonActionConsumer != null) {
            NewVideoPresetModel clonedPreset = new NewVideoPresetModel();
            super.clone(model, clonedPreset);
            Translations presetTrans = TranslationsManager.getTranslation(TranslationBundles.WINDOW_PRESET);
            clonedPreset.setPresetName(presetTrans.getString("copyOf") + model.getPresetName());
            cloneButtonActionConsumer.accept(clonedPreset);
        }
    }

    @Override
    public int compareTo(@NotNull PresetItemController o) {
        return model.compareTo(o.model);
    }

    @Override
    public void startEdit(ActionEvent actionEvent) {
        super.startEdit(actionEvent);
        saveButton.setVisible(true);
        editButton.setVisible(false);
        cancelButton.setVisible(true);
        deleteButton.setVisible(false);
        cloneButton.setVisible(false);
    }

    @Override
    public void commitEdit(ActionEvent actionEvent) {
        super.commitEdit(actionEvent);
        saveButton.setVisible(false);
        editButton.setVisible(true);
        cancelButton.setVisible(false);
        deleteButton.setVisible(true);
        cloneButton.setVisible(true);
        // update the model with values from the input fields
        model.setPresetName(presetName.getText());
    }

    @Override
    public void cancelEdit(ActionEvent actionEvent) {
        super.cancelEdit(actionEvent);
        saveButton.setVisible(false);
        editButton.setVisible(true);
        cancelButton.setVisible(false);
        deleteButton.setVisible(true);
        cloneButton.setVisible(true);
        // restore the input field values from the model
        presetName.setText(model.getPresetName());
    }

    private void extendPane() {
        innerPane.getRowConstraints().add(new RowConstraints(30));
        TextField presetName = new TextField(model.getPresetName());
        presetName.setId(NODE_ID_PRESET_NAME);
        innerPane.add(presetName, 1, 5);

        saveButton = new Button();
        saveButton.setId(NODE_ID_BUTTON_SAVE);
        saveButton.setVisible(false);
        saveButton.setOnAction(this::commitEdit);

        editButton = new Button();
        editButton.setId(NODE_ID_BUTTON_EDIT);
        editButton.setOnAction(this::startEdit);

        cancelButton = new Button();
        cancelButton.setId(NODE_ID_BUTTON_CANCEL);
        cancelButton.setVisible(false);
        cancelButton.setOnAction(this::cancelEdit);

        deleteButton = new Button();
        deleteButton.setId(NODE_ID_BUTTON_DELETE);
        deleteButton.setOnAction(this::delete);

        cloneButton = new Button();
        cloneButton.setId(NODE_ID_BUTTON_CLONE);
        cloneButton.setOnAction(this::clone);

        buttonBox.getChildren().addAll(saveButton, editButton,
                cancelButton, deleteButton, cloneButton);
    }
}
