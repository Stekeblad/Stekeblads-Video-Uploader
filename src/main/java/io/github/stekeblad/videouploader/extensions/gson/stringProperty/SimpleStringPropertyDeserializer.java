package io.github.stekeblad.videouploader.extensions.gson.stringProperty;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.lang.reflect.Type;

public class SimpleStringPropertyDeserializer implements JsonDeserializer<StringProperty> {
    @Override
    public StringProperty deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return new SimpleStringProperty(json.getAsJsonPrimitive().getAsString());
    }
}
