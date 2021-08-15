package io.github.stekeblad.videouploader.extensions.gson.stringProperty;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import javafx.beans.property.StringProperty;

import java.lang.reflect.Type;

public class SimpleStringPropertySerializer implements JsonSerializer<StringProperty> {
    @Override
    public JsonElement serialize(StringProperty src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.get());
    }
}
