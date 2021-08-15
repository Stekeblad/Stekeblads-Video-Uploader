package io.github.stekeblad.videouploader.extensions.gson.booleanProperty;

import com.google.gson.InstanceCreator;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.lang.reflect.Type;

public class SimpleBooleanPropertyInstanceCreator implements InstanceCreator<BooleanProperty> {
    @Override
    public BooleanProperty createInstance(Type type) {
        return new SimpleBooleanProperty(false);
    }
}
