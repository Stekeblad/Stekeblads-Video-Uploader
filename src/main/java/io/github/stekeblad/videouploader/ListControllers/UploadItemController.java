package io.github.stekeblad.videouploader.ListControllers;

import io.github.stekeblad.videouploader.models.NewVideoUploadModel;
import io.github.stekeblad.videouploader.utils.background.OpenInBrowser;
import io.github.stekeblad.videouploader.utils.translation.TranslationBundles;
import io.github.stekeblad.videouploader.utils.translation.Translations;
import io.github.stekeblad.videouploader.utils.translation.TranslationsManager;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.RowConstraints;

import java.util.function.Consumer;

public class UploadItemController extends VideoInfoItemController<NewVideoUploadModel> {

    // FX Nodes
    // TODO: Try making private and annotate @FXML?
    public ProgressBar uploadProgress;
    public Label statusLabel;
    public Button saveButton;
    public Button editButton;
    public Button cancelButton;
    public Button deleteButton;

    private static final String NODE_ID_PROGRESS = "uploadProgress";
    private static final String NODE_ID_STATUS = "status";
    private static final String NODE_ID_BUTTON_SAVE = "save";
    private static final String NODE_ID_BUTTON_EDIT = "edit";
    private static final String NODE_ID_BUTTON_CANCEL = "cancel";
    private static final String NODE_ID_BUTTON_DELETE = "delete";

    private static final Translations transUpload = TranslationsManager.getTranslation(TranslationBundles.PRESET_UPLOAD);

    private Consumer<NewVideoUploadModel> deleteButtonActionConsumer;
    private Consumer<NewVideoUploadModel> startUploadButtonActionConsumer;

    public UploadItemController(NewVideoUploadModel videoUpload, ReadOnlyDoubleProperty parentPrefWidthProperty) {
        super(videoUpload, parentPrefWidthProperty);
        extendPane();
        createBindings();
        transUpload.autoTranslate(this);
    }

    @Override
    public NewVideoUploadModel getModel() {
        return model;
    }

    public void registerDeleteButtonActionHandler(Consumer<NewVideoUploadModel> deleteActionHandler) {
        deleteButtonActionConsumer = deleteActionHandler;
    }

    public void registerStartUploadButtonActionHandler(Consumer<NewVideoUploadModel> startUploadActionHandler) {
        startUploadButtonActionConsumer = startUploadActionHandler;
    }

    public void delete(ActionEvent actionEvent) {
        if (deleteButtonActionConsumer != null)
            deleteButtonActionConsumer.accept(model);
    }

    public void startUpload(ActionEvent actionEvent) {
        //TODO: Here and all other action, validate state before doing things, like do not start upload if editing is currently enabled
        if (startUploadButtonActionConsumer != null)
            startUploadButtonActionConsumer.accept(model);
    }

    @Override
    public void startEdit(ActionEvent actionEvent) {
        super.startEdit(actionEvent);
        saveButton.setVisible(true);
        editButton.setVisible(false);
        cancelButton.setVisible(true);
        deleteButton.setVisible(false);
    }

    @Override
    public void commitEdit(ActionEvent actionEvent) {
        super.commitEdit(actionEvent);
        saveButton.setVisible(false);
        editButton.setVisible(true);
        cancelButton.setVisible(false);
        deleteButton.setVisible(true);
    }

    @Override
    public void cancelEdit(ActionEvent actionEvent) {
        super.cancelEdit(actionEvent);
        saveButton.setVisible(false);
        editButton.setVisible(true);
        cancelButton.setVisible(false);
        deleteButton.setVisible(true);
    }


    private void extendPane() {
        innerPane.getRowConstraints().add(new RowConstraints(30));

        statusLabel = new Label();
        statusLabel.setId(NODE_ID_STATUS);
        innerPane.add(statusLabel, 1, 5);

        uploadProgress = new ProgressBar();
        uploadProgress.setId(NODE_ID_PROGRESS);
        uploadProgress.setVisible(false);
        innerPane.add(uploadProgress, 2, 5);

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

        buttonBox.getChildren().addAll(saveButton, editButton,
                cancelButton, deleteButton);
    }

    private void createBindings() {
        statusLabel.textProperty().bind(model.statusTextProperty());
        uploadProgress.progressProperty().bind(model.uploadProgressProperty());

        model.statusTextLinkProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                statusLabel.setOnMouseClicked(Event::consume);
                statusLabel.setOnMouseEntered(Event::consume);
                statusLabel.setOnMouseExited(Event::consume);
            } else {
                statusLabel.setOnMouseClicked(event -> {
                    OpenInBrowser.openInBrowser(model.getStatusTextLink());
                    event.consume();
                });
                statusLabel.setOnMouseEntered(event -> {
                    this.getScene().setCursor(Cursor.HAND);
                    event.consume();
                });
                statusLabel.setOnMouseExited(event -> {
                    this.getScene().setCursor(Cursor.DEFAULT);
                    event.consume();
                });
            }
        });
    }

    //TODO: make sure it gets called at the right time
    private void releaseBindings() {
        statusLabel.textProperty().unbind();
        uploadProgress.progressProperty().unbind();
    }
}
