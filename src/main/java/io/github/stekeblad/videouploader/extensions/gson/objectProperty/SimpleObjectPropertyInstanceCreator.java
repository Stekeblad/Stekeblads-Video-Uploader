package io.github.stekeblad.videouploader.extensions.gson.objectProperty;

import com.google.gson.InstanceCreator;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.lang.reflect.Type;

public class SimpleObjectPropertyInstanceCreator<E> implements InstanceCreator<ObjectProperty<E>> {
    @Override
    public ObjectProperty<E> createInstance(Type type) {
        return new SimpleObjectProperty<E>(null);
    }
}
