package io.github.stekeblad.videouploader.windowControllers;

import io.github.stekeblad.videouploader.models.NewVideoPresetModel;
import io.github.stekeblad.videouploader.youtube.LocalCategory;
import io.github.stekeblad.videouploader.youtube.LocalPlaylist;
import io.github.stekeblad.videouploader.youtube.utils.VisibilityStatus;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;

public class PresetItemController extends ListCell<NewVideoPresetModel> implements Initializable {
    // FX Nodes
    public GridPane presetPane;
    public TextField title;
    public TextArea description;
    public ChoiceBox<VisibilityStatus> visibility;
    public TextArea tags;
    public ChoiceBox<LocalPlaylist> playlist;
    public ChoiceBox<LocalCategory> category;
    public CheckBox notifySubscribers;
    public ImageView thumbnail;
    public CheckBox madeForKids;
    public HBox buttonBox;
    public TextField presetName;

    // Other fields
    private NewVideoPresetModel model;

    public static PresetItemController newInstance() {
        FXMLLoader loader = new FXMLLoader(PresetItemController.class.getResource("fxml/VideoPresetPane.fxml"));
        try {
            loader.load();
            return loader.getController();
        } catch (IOException ignored) {
            return null;
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Do things like configuring the nodes
    }

    @Override
    protected void updateItem(NewVideoPresetModel preset, boolean empty) {
        super.updateItem(preset, empty);
        if (!empty && preset != null && !preset.equals(this.model)) {
            title.setText(preset.getVideoName());
            description.setText(preset.getVideoDescription());
            visibility.getSelectionModel().select(VisibilityStatus.valueOf(preset.getVisibility()));
            tags.setText(String.join(", ", preset.getVideoTags()));
            playlist.getSelectionModel().select(preset.getSelectedPlaylist());
            category.getSelectionModel().select(preset.getSelectedCategory());
            notifySubscribers.setSelected(preset.isTellSubs());
            thumbnail.setImage(getSelectedOrDefaultImage(preset.getThumbnailPath()));
            madeForKids.setSelected(preset.isMadeForKids());
            presetName.setText(preset.getPresetName());
        }
        // Remember this model instance
        this.model = preset;
    }

    private Image getSelectedOrDefaultImage(String thumbnailPath) {
        try {
            InputStream stream;
            try {
                stream = new FileInputStream(new File(thumbnailPath));
            } catch (FileNotFoundException | NullPointerException e) {
                stream = this.getClass().getResourceAsStream("/images/no_image.png");
            }
            Image image = new Image(stream);
            stream.close();
            return image;
        } catch (IOException ignored) {
            return null;
        }
    }

    @Override
    public void commitEdit(NewVideoPresetModel newValue) {
        // use saved model instance if the provided is null
        newValue = (newValue == null) ? this.model : newValue;
        super.commitEdit(newValue); // <-- important
        // update the model with values from the text fields
    }
}
