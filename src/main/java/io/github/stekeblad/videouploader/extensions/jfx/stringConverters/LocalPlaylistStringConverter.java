package io.github.stekeblad.videouploader.extensions.jfx.stringConverters;

import io.github.stekeblad.videouploader.youtube.LocalPlaylist;
import javafx.util.StringConverter;

public class LocalPlaylistStringConverter extends StringConverter<LocalPlaylist> {
    @Override
    public String toString(LocalPlaylist object) {
        return object.getName();
    }

    @Override
    public LocalPlaylist fromString(String string) {
        return null;
    }
}
