package io.github.stekeblad.videouploader.extensions.gson.booleanProperty;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import javafx.beans.property.BooleanProperty;

import java.lang.reflect.Type;

public class SimpleBooleanPropertySerializer implements JsonSerializer<BooleanProperty> {
    @Override
    public JsonElement serialize(BooleanProperty src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.get());
    }
}
