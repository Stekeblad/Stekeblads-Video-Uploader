package io.github.stekeblad.videouploader.extensions.gson.listProperty;

import com.google.gson.InstanceCreator;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

import java.lang.reflect.Type;

public class SimpleListPropertyInstanceCreator<E> implements InstanceCreator<ListProperty<E>> {
    @Override
    public ListProperty<E> createInstance(Type type) {
        return new SimpleListProperty<E>(FXCollections.emptyObservableList());
    }
}
