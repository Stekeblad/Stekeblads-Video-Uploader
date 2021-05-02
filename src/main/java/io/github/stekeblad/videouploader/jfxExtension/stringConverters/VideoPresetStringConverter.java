package io.github.stekeblad.videouploader.jfxExtension.stringConverters;

import io.github.stekeblad.videouploader.models.NewVideoPresetModel;
import javafx.util.StringConverter;

/**
 * For ChoiceBoxes and similar cases to specify how a object should be presented as a string
 * <p>
 * Presets are displayed by their name
 * </p>
 */
public class VideoPresetStringConverter extends StringConverter<NewVideoPresetModel> {
    @Override
    public String toString(NewVideoPresetModel object) {
        return object.getPresetName();
    }

    @Override
    public NewVideoPresetModel fromString(String string) {
        return null;
    }
}
