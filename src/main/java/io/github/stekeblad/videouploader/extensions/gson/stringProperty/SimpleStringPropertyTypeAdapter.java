package io.github.stekeblad.videouploader.extensions.gson.stringProperty;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.IOException;

public class SimpleStringPropertyTypeAdapter extends TypeAdapter<StringProperty> {
    @Override
    public void write(JsonWriter out, StringProperty value) throws IOException {
        if (value == null || value.get() == null) {
            out.nullValue();
            return;
        }
        out.value(value.get());
    }

    @Override
    public StringProperty read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        return new SimpleStringProperty(in.nextString());
    }
}
