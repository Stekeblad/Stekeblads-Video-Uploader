package io.github.stekeblad.videouploader.extensions.jfx.objectProperties;

import javafx.beans.property.SimpleObjectProperty;

import java.io.File;

/**
 * Its difficult to configure serialization and deserialization with Gson for classes with type parameters,
 * so here is a small wrapping class for SimpleObjectProperty<File>
 */
public class FileObjectProperty extends SimpleObjectProperty<File> {
    public FileObjectProperty(File initialValue) {
        super(null, "", initialValue);
    }

    public FileObjectProperty() {
        super(null, "");
    }
}
