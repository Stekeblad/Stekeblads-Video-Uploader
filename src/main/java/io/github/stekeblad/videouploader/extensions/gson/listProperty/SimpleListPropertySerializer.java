package io.github.stekeblad.videouploader.extensions.gson.listProperty;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import javafx.beans.property.ListProperty;

import java.lang.reflect.Type;

public class SimpleListPropertySerializer<E> implements JsonSerializer<ListProperty<E>> {
    @Override
    public JsonElement serialize(ListProperty<E> src, Type typeOfSrc, JsonSerializationContext context) {
        return context.serialize(src.get());
    }
}
