package io.github.stekeblad.videouploader.extensions.jfx.stringConverters;

import io.github.stekeblad.videouploader.youtube.LocalCategory;
import javafx.util.StringConverter;

public class LocalCategoryStringConverter extends StringConverter<LocalCategory> {
    @Override
    public String toString(LocalCategory object) {
        return object.getName();
    }

    @Override
    public LocalCategory fromString(String string) {
        return null;
    }
}
