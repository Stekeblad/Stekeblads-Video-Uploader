package io.github.stekeblad.videouploader.windowControllers;

import io.github.stekeblad.videouploader.extensions.jfx.IWindowController;
import io.github.stekeblad.videouploader.tagProcessing.metaDataTagProcessor.MetaDataReader;
import io.github.stekeblad.videouploader.utils.AlertUtils;
import io.github.stekeblad.videouploader.utils.FileUtils;
import io.github.stekeblad.videouploader.utils.background.OpenInBrowser;
import io.github.stekeblad.videouploader.utils.translation.TranslationBundles;
import io.github.stekeblad.videouploader.utils.translation.Translations;
import io.github.stekeblad.videouploader.utils.translation.TranslationsManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.util.Pair;

import java.io.File;

public class MetaDataToolWindowController implements IWindowController {
    public AnchorPane MetaDataToolWindow;
    public Label label_description;
    public Button btn_pickFile;
    public Label label_selectedFile;
    public TableView<Pair<String, String>> table_metadata;
    public ProgressIndicator prog_readingFile;

    private Translations trans_metaDataTool;

    @Override
    public void myInit() {
        // Set the default exception handler, hopefully it can catch some of the exceptions that is not already caught
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> AlertUtils.unhandledExceptionDialog(exception));

        prog_readingFile.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        prog_readingFile.setVisible(false);
        trans_metaDataTool = TranslationsManager.getTranslation(TranslationBundles.WINDOW_TOOL_META);
        trans_metaDataTool.autoTranslate(MetaDataToolWindow);

        tableSetUp();

        // F1 for wiki on this window
        MetaDataToolWindow.getScene().setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.F1) {
                OpenInBrowser.openInBrowser("https://github.com/Stekeblad/Stekeblads-Video-Uploader/wiki/Meta-Data-Tool-Window");
                event.consume();
            }
        });
    }

    @Override
    public boolean onWindowClose() {
        return true;
    }

    public void onPickFile(ActionEvent actionEvent) {
        actionEvent.consume();
        btn_pickFile.setDisable(true);
        File video;

        try {
            video = FileUtils.pickVideo(Long.MAX_VALUE);
            if (video == null) {
                btn_pickFile.setDisable(false);
                return;
            }
        } catch (Exception e) {
            AlertUtils.simpleClose(trans_metaDataTool.getString("diag_invalidChoice_short"),
                    trans_metaDataTool.getString("diag_invalidChoice_full")).show();
            btn_pickFile.setDisable(false);
            return;
        }
        // Do not show the progress indicator while the file chooser is open, make it visible first when a valid file is chosen
        prog_readingFile.setVisible(true);
        getMetaDataOfFile(video);
    }

    private void getMetaDataOfFile(File file) {
        Task<Void> newTask = new Task<>() {
            @Override
            protected Void call() {
                MetaDataReader metaDataReader = new MetaDataReader(file);
                if (metaDataReader.isFileSupported()) {
                    Platform.runLater(() -> {
                        table_metadata.getItems().clear();
                        table_metadata.getItems().addAll(metaDataReader.getAllTagsAndValues());
                        label_selectedFile.setText(file.getName());
                    });
                } else {
                    Platform.runLater(() -> AlertUtils.simpleClose(trans_metaDataTool.getString("diag_metaNoSupport_short"),
                            trans_metaDataTool.getString("diag_metaNoSupport_full")).show());
                }
                btn_pickFile.setDisable(false);
                prog_readingFile.setVisible(false);
                return null;
            }
        };

        Thread thread = new Thread(newTask);
        thread.start();
    }

    private void tableSetUp() {
        TableColumn<Pair<String, String>, String> keyColumn = new TableColumn<>(trans_metaDataTool.getString("column_key"));
        keyColumn.setCellValueFactory(new PropertyValueFactory<>("key"));
        keyColumn.setPrefWidth(100);
        table_metadata.getColumns().add(keyColumn);
        TableColumn<Pair<String, String>, String> valueColumn = new TableColumn<>(trans_metaDataTool.getString("column_value"));
        valueColumn.prefWidthProperty().bind(table_metadata.widthProperty().subtract(keyColumn.widthProperty()).subtract(15));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        // make the text in the valueColumn resize and wrap when the column width is changed
        // note the difference here, CellValueFactory above and CellFactory below!
        valueColumn.setCellFactory(tc -> {
            TableCell<Pair<String, String>, String> cell = new TableCell<>();
            Text text = new Text();
            cell.setGraphic(text);
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(valueColumn.widthProperty());
            text.textProperty().bind(cell.itemProperty());
            return cell;
        });

        table_metadata.getColumns().add(valueColumn);
    }
}
