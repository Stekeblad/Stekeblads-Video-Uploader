package io.github.stekeblad.videouploader.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.jetbrains.annotations.NotNull;

public class NewVideoPresetModel extends NewVideoInfoBaseModel implements Comparable<NewVideoPresetModel> {
    private final StringProperty presetName = new SimpleStringProperty();

    public static final String MAGIC_VIDEO_NAME = "¤¤   ¤ ¤¤";

    public String getPresetName() {
        return presetName.get();
    }

    public StringProperty presetNameProperty() {
        return presetName;
    }

    public void setPresetName(String presetName) {
        this.presetName.set(presetName);
    }

    @Override
    public int compareTo(@NotNull NewVideoPresetModel other) {
        // A preset with the magic video name "¤¤   ¤ ¤¤" is the placeholder for when no preset is selected
        // This preset should be sorted first
        if (this.getVideoName().equals(MAGIC_VIDEO_NAME))
            return -1;
        else if (other.getVideoName().equals(MAGIC_VIDEO_NAME))
            return 1;
        else
            return this.getPresetName().compareTo(other.getPresetName());
    }

}
