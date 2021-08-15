package io.github.stekeblad.videouploader.extensions.gson.booleanProperty;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.lang.reflect.Type;

public class SimpleBooleanPropertyDeserializer implements JsonDeserializer<BooleanProperty> {
    @Override
    public BooleanProperty deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return new SimpleBooleanProperty((json.getAsJsonPrimitive().getAsBoolean()));
    }
}
