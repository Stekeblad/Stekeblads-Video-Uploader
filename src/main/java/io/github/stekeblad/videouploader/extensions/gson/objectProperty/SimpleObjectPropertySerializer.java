package io.github.stekeblad.videouploader.extensions.gson.objectProperty;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.github.stekeblad.videouploader.extensions.jfx.objectProperties.FileObjectProperty;
import io.github.stekeblad.videouploader.extensions.jfx.objectProperties.LocalCategoryObjectProperty;
import io.github.stekeblad.videouploader.extensions.jfx.objectProperties.LocalPlaylistObjectProperty;
import io.github.stekeblad.videouploader.extensions.jfx.objectProperties.VisibilityStatusObjectProperty;
import io.github.stekeblad.videouploader.youtube.LocalCategory;
import io.github.stekeblad.videouploader.youtube.LocalPlaylist;
import io.github.stekeblad.videouploader.youtube.VisibilityStatus;
import javafx.beans.property.ObjectProperty;

import java.lang.reflect.Type;

public class SimpleObjectPropertySerializer<E> implements JsonSerializer<ObjectProperty<E>> {
    @Override
    public JsonElement serialize(ObjectProperty<E> src, Type typeOfSrc, JsonSerializationContext context) {
        return context.serialize(src.get());
    }

    public static class VisibilityStatusObjectSerializer implements JsonSerializer<VisibilityStatusObjectProperty> {

        @Override
        public JsonElement serialize(VisibilityStatusObjectProperty src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src.get(), VisibilityStatus.class);
        }
    }

    public static class LocalPlaylistObjectSerializer implements JsonSerializer<LocalPlaylistObjectProperty> {

        @Override
        public JsonElement serialize(LocalPlaylistObjectProperty src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src.get(), LocalPlaylist.class);
        }
    }

    public static class LocalCategoryObjectSerializer implements JsonSerializer<LocalCategoryObjectProperty> {

        @Override
        public JsonElement serialize(LocalCategoryObjectProperty src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src.get(), LocalCategory.class);
        }
    }

    public static class FileObjectSerializer implements JsonSerializer<FileObjectProperty> {

        @Override
        public JsonElement serialize(FileObjectProperty src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src.get(), LocalPlaylist.class);
        }
    }
}
