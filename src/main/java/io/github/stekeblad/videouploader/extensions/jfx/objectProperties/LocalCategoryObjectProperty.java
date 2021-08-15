package io.github.stekeblad.videouploader.extensions.jfx.objectProperties;

import io.github.stekeblad.videouploader.youtube.LocalCategory;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Its difficult to configure serialization and deserialization with Gson for classes with type parameters,
 * so here is a small wrapping class for SimpleObjectProperty<LocalCategory>
 */
public class LocalCategoryObjectProperty extends SimpleObjectProperty<LocalCategory> {
    public LocalCategoryObjectProperty(LocalCategory initialValue) {
        super(null, "", initialValue);
    }

    public LocalCategoryObjectProperty() {
        super(null, "");
    }
}
