package io.github.stekeblad.videouploader.extensions.jfx.objectProperties;

import io.github.stekeblad.videouploader.youtube.VisibilityStatus;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Its difficult to configure serialization and deserialization with Gson for classes with type parameters,
 * so here is a small wrapping class for SimpleObjectProperty<VisibilityStatus>
 */
public class VisibilityStatusObjectProperty extends SimpleObjectProperty<VisibilityStatus> {
    public VisibilityStatusObjectProperty(VisibilityStatus initialValue) {
        super(null, "", initialValue);
    }

    public VisibilityStatusObjectProperty() {
        super(null, "");
    }
}
