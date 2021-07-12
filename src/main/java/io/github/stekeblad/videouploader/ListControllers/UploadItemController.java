package io.github.stekeblad.videouploader.ListControllers;

import io.github.stekeblad.videouploader.models.NewVideoUploadModel;
import io.github.stekeblad.videouploader.models.NewVideoUploadState;
import io.github.stekeblad.videouploader.utils.AlertUtils;
import io.github.stekeblad.videouploader.utils.background.OpenInBrowser;
import io.github.stekeblad.videouploader.utils.translation.TranslationBundles;
import io.github.stekeblad.videouploader.utils.translation.Translations;
import io.github.stekeblad.videouploader.utils.translation.TranslationsManager;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.Cursor;
import javafx.scene.control.*;
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
    public Button resetButton;
    public Button startUploadButton;
    public Button abortUploadButton;
    public Button hideButton;

    private static final String NODE_ID_PROGRESS = "uploadProgress";
    private static final String NODE_ID_STATUS = "status";

    private static final String NODE_ID_BUTTON_SAVE = "save";
    private static final String NODE_ID_BUTTON_EDIT = "edit";
    private static final String NODE_ID_BUTTON_CANCEL = "cancel";
    private static final String NODE_ID_BUTTON_DELETE = "delete";
    private static final String NODE_ID_BUTTON_RESET = "reset";
    private static final String NODE_ID_BUTTON_UPLOAD = "upload";
    private static final String NODE_ID_BUTTON_ABORT = "abort";
    private static final String NODE_ID_BUTTON_HIDE = "hide";

    private static final Translations transUpload = TranslationsManager.getTranslation(TranslationBundles.PRESET_UPLOAD);

    private Consumer<NewVideoUploadModel> deleteButtonActionConsumer;
    private Consumer<NewVideoUploadModel> startUploadButtonActionConsumer;
    private Consumer<NewVideoUploadModel> resetUploadButtonActionConsumer;
    private Consumer<NewVideoUploadModel> abortUploadButtonActionConsumer;
    private Consumer<NewVideoUploadModel> hideUploadButtonActionConsumer;

    private NewVideoUploadState state;

    public UploadItemController(NewVideoUploadModel videoUpload, ReadOnlyDoubleProperty parentPrefWidthProperty) {
        super(videoUpload, parentPrefWidthProperty);
        extendPane();
        createBindings();
        transUpload.autoTranslate(this);

        // If model is missing required details then do not allow to cancel edit, offer to delete instead
        if (model.getVideoDescription() == null || model.getSelectedCategory() == null) {
            cancelButton.setVisible(false);
            deleteButton.setVisible(true);
            setState(NewVideoUploadState.NEW);
        } else {
            setState(NewVideoUploadState.SAVED);
        }
    }

    @Override
    public NewVideoUploadModel getModel() {
        return model;
    }

    public NewVideoUploadState getState() {
        return state;
    }

    public void registerDeleteButtonActionHandler(Consumer<NewVideoUploadModel> deleteActionHandler) {
        deleteButtonActionConsumer = deleteActionHandler;
    }

    public void registerStartUploadButtonActionHandler(Consumer<NewVideoUploadModel> startUploadActionHandler) {
        startUploadButtonActionConsumer = startUploadActionHandler;
    }

    public void registerResetUploadButtonActionHandler(Consumer<NewVideoUploadModel> resetUploadActionHandler) {
        resetUploadButtonActionConsumer = resetUploadActionHandler;
    }

    public void registerAbortUploadButtonActionHandler(Consumer<NewVideoUploadModel> abortUploadActionHandler) {
        abortUploadButtonActionConsumer = abortUploadActionHandler;
    }

    public void registerHideUploadButtonActionHandler(Consumer<NewVideoUploadModel> hideUploadActionHandler) {
        hideUploadButtonActionConsumer = hideUploadActionHandler;
    }

    public void delete(ActionEvent actionEvent) {
        delete(actionEvent, true);
    }

    public void delete(ActionEvent actionEvent, boolean showWarning) {
        if (showWarning) {
            if (state != NewVideoUploadState.NEW
                    && state != NewVideoUploadState.SAVED
                    && state != NewVideoUploadState.COMPLETED
                    && state != NewVideoUploadState.FAILED) {
                throwState("Delete");
            }
            // TODO: confirmation dialog?
            ButtonType userChoice = AlertUtils.yesNo("TODO", "delete upload?", ButtonType.NO);
            if (userChoice == ButtonType.NO)
                return;
        }
        releaseBindings();
        setState(NewVideoUploadState.DELETED);
        if (deleteButtonActionConsumer != null)
            deleteButtonActionConsumer.accept(model);

    }

    public void startUpload(ActionEvent actionEvent) {
        if (state != NewVideoUploadState.SAVED)
            throwState("Start upload");
        uploadProgress.setVisible(true);
        setState(NewVideoUploadState.UPLOADING);
        if (startUploadButtonActionConsumer != null)
            startUploadButtonActionConsumer.accept(model);
    }

    @Override
    public void startEdit(ActionEvent actionEvent) {
        if (state != NewVideoUploadState.SAVED)
            throwState("Edit");
        super.startEdit(actionEvent);
        setState(NewVideoUploadState.EDITING);
    }

    @Override
    public void commitEdit(ActionEvent actionEvent) {
        if (state != NewVideoUploadState.EDITING
                && state != NewVideoUploadState.NEW)
            throwState("Save");
        // TODO: Validate
        super.commitEdit(actionEvent);
        setState(NewVideoUploadState.SAVED);
    }

    @Override
    public void cancelEdit(ActionEvent actionEvent) {
        if (state != NewVideoUploadState.EDITING)
            throwState("Cancel edit");
        super.cancelEdit(actionEvent);
        setState(NewVideoUploadState.SAVED);
    }

    public void reset(ActionEvent actionEvent) {
        if (state != NewVideoUploadState.FAILED)
            throwState("Reset upload");
        setState(NewVideoUploadState.SAVED);
        if (resetUploadButtonActionConsumer != null)
            resetUploadButtonActionConsumer.accept(model);
    }

    public void abort(ActionEvent actionEvent) {
        abort(actionEvent, true);
    }

    public void abort(ActionEvent actionEvent, boolean showWarning) {
        if (state != NewVideoUploadState.UPLOADING)
            throwState("Abort upload");
        if (showWarning) { // TODO: Better warning text
            ButtonType userChoice = AlertUtils.yesNo("Abort?",
                    "Abort the uploading of " + model.getVideoName() + "?",
                    ButtonType.NO);
            if (userChoice == ButtonType.NO)
                return;
        }
        setState(NewVideoUploadState.FAILED);
        if (abortUploadButtonActionConsumer != null)
            abortUploadButtonActionConsumer.accept(model);
        actionEvent.consume();
    }

    public void hide(ActionEvent actionEvent) {
        if (state != NewVideoUploadState.FAILED)
            throwState("Hide");
        setState(NewVideoUploadState.DELETED);
        if (hideUploadButtonActionConsumer != null)
            hideUploadButtonActionConsumer.accept(model);
        actionEvent.consume();
    }

    public void complete() {
        if (state != NewVideoUploadState.UPLOADING)
            throwState("Complete");
        setState(NewVideoUploadState.COMPLETED);
    }

    public void fail() {
        if (state != NewVideoUploadState.UPLOADING)
            throwState("Fail");
        setState(NewVideoUploadState.FAILED);
        setProgressBarColor("red");
        model.setStatusText(transUpload.getString("failed"));
    }

    private void extendPane() {
        innerPane.getRowConstraints().add(new RowConstraints(30));

        statusLabel = new Label();
        statusLabel.setId(NODE_ID_STATUS);
        innerPane.add(statusLabel, 1, 5);

        uploadProgress = new ProgressBar();
        uploadProgress.setId(NODE_ID_PROGRESS);
        uploadProgress.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        innerPane.add(uploadProgress, 2, 5);

        saveButton = new Button();
        saveButton.setId(NODE_ID_BUTTON_SAVE);
        saveButton.setOnAction(this::commitEdit);

        editButton = new Button();
        editButton.setId(NODE_ID_BUTTON_EDIT);
        editButton.setOnAction(this::startEdit);

        cancelButton = new Button();
        cancelButton.setId(NODE_ID_BUTTON_CANCEL);
        cancelButton.setOnAction(this::cancelEdit);

        deleteButton = new Button();
        deleteButton.setId(NODE_ID_BUTTON_DELETE);
        deleteButton.setOnAction(this::delete);

        resetButton = new Button();
        resetButton.setId(NODE_ID_BUTTON_RESET);
        resetButton.setOnAction(this::reset);

        startUploadButton = new Button();
        startUploadButton.setId(NODE_ID_BUTTON_UPLOAD);
        startUploadButton.setOnAction(this::startUpload);

        abortUploadButton = new Button();
        abortUploadButton.setId(NODE_ID_BUTTON_ABORT);
        abortUploadButton.setOnAction(this::abort);

        hideButton = new Button();
        hideButton.setId(NODE_ID_BUTTON_HIDE);
        hideButton.setOnAction(this::hide);

        buttonBox.getChildren().addAll(saveButton, editButton,
                cancelButton, deleteButton, resetButton,
                startUploadButton, abortUploadButton, hideButton);
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
                    if (model.getStatusTextLink() != null)
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

    private void releaseBindings() {
        statusLabel.textProperty().unbind();
        uploadProgress.progressProperty().unbind();
    }

    private void setProgressBarColor(String color) {
        if (color == null) {
            color = "#0096c9"; // default -fx-accent color in used style (Modena)
        }
        uploadProgress.setStyle("-fx-accent: " + color);
    }

    private void throwState(String attemptedAction) {
        throw new IllegalStateException(attemptedAction + " not permitted for upload "
                + model.getVideoName() + ", its currently in the state " + state.name());
    }

    /**
     * Changes the state of this upload and update visible action buttons
     *
     * @param newState The state to change this upload to
     */
    private void setState(NewVideoUploadState newState) {
        state = newState;

        cancelButton.setVisible(false);
        deleteButton.setVisible(false);
        editButton.setVisible(false);
        saveButton.setVisible(false);
        resetButton.setVisible(false);
        startUploadButton.setVisible(false);
        abortUploadButton.setVisible(false);
        hideButton.setVisible(false);

        setProgressBarColor(null);

        switch (newState) {

            case NEW:
                deleteButton.setVisible(true);
                saveButton.setVisible(true);
                break;
            case EDITING:
                cancelButton.setVisible(true);
                saveButton.setVisible(true);
                break;
            case SAVED:
                startUploadButton.setVisible(true);
                editButton.setVisible(true);
                deleteButton.setVisible(true);
                break;
            case UPLOADING:
                abortUploadButton.setVisible(true);
                break;
            case COMPLETED:
                hideButton.setVisible(true);
                break;
            case FAILED:
                resetButton.setVisible(true);
                deleteButton.setVisible(true);
                break;
            case DELETED:
                break;
        }
    }
}
