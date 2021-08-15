package io.github.stekeblad.videouploader.extensions.jfx.objectProperties;

import io.github.stekeblad.videouploader.youtube.LocalPlaylist;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Its difficult to configure serialization and deserialization with Gson for classes with type parameters,
 * so here is a small wrapping class for SimpleObjectProperty<LocalPlaylist>
 */
public class LocalPlaylistObjectProperty extends SimpleObjectProperty<LocalPlaylist> {
    public LocalPlaylistObjectProperty(LocalPlaylist initialValue) {
        super(null, "", initialValue);
    }

    public LocalPlaylistObjectProperty() {
        super(null, "");
    }
}
