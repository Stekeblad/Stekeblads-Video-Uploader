package io.github.stekeblad.videouploader.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class NewVideoPresetModel extends NewVideoInfoBaseModel {
    private final StringProperty presetName = new SimpleStringProperty();

    public String getPresetName() {
        return presetName.get();
    }

    public StringProperty presetNameProperty() {
        return presetName;
    }

    public void setPresetName(String presetName) {
        this.presetName.set(presetName);
    }
}
