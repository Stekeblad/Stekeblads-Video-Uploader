package io.github.stekeblad.videouploader.extensions.gson.objectProperty;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import io.github.stekeblad.videouploader.extensions.jfx.objectProperties.FileObjectProperty;
import io.github.stekeblad.videouploader.extensions.jfx.objectProperties.LocalCategoryObjectProperty;
import io.github.stekeblad.videouploader.extensions.jfx.objectProperties.LocalPlaylistObjectProperty;
import io.github.stekeblad.videouploader.extensions.jfx.objectProperties.VisibilityStatusObjectProperty;
import io.github.stekeblad.videouploader.youtube.LocalCategory;
import io.github.stekeblad.videouploader.youtube.LocalPlaylist;
import io.github.stekeblad.videouploader.youtube.VisibilityStatus;
import javafx.beans.property.ObjectProperty;

import java.io.File;
import java.lang.reflect.Type;

public class SimpleObjectPropertyDeserializers {

    public static class VisibilityStatusObjectPropertyDeserializer implements JsonDeserializer<VisibilityStatusObjectProperty> {

        @Override
        public VisibilityStatusObjectProperty deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new VisibilityStatusObjectProperty(context.deserialize(json, VisibilityStatus.class));
        }
    }

    public static class LocalPlaylistObjectPropertyDeserializer implements JsonDeserializer<ObjectProperty<LocalPlaylist>> {

        @Override
        public LocalPlaylistObjectProperty deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new LocalPlaylistObjectProperty(context.deserialize(json, LocalPlaylist.class));
        }
    }

    public static class LocalCategoryObjectPropertyDeserializer implements JsonDeserializer<ObjectProperty<LocalCategory>> {

        @Override
        public LocalCategoryObjectProperty deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new LocalCategoryObjectProperty(context.deserialize(json, LocalCategory.class));
        }
    }

    public static class FileObjectPropertyDeserializer implements JsonDeserializer<ObjectProperty<File>> {

        @Override
        public FileObjectProperty deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new FileObjectProperty(context.deserialize(json, File.class));
        }
    }
}