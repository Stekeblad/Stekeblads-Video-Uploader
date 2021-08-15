package io.github.stekeblad.videouploader.extensions.gson.stringProperty;

import com.google.gson.InstanceCreator;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.lang.reflect.Type;

public class SimpleStringPropertyInstanceCreator implements InstanceCreator<StringProperty> {
    @Override
    public StringProperty createInstance(Type type) {
        return new SimpleStringProperty("placeholder");
    }
}
