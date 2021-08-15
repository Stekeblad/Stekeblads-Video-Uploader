package io.github.stekeblad.videouploader.extensions.gson.listProperty;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

public class SimpleListPropertyDeserializers {
    public static class StringListPropertyDeserializer implements JsonDeserializer<ListProperty<String>> {

        @Override
        public ListProperty<String> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new SimpleListProperty<String>(FXCollections.observableArrayList((Collection<String>) context.deserialize(json, ArrayList.class)));
        }
    }
}
