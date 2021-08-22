package io.github.stekeblad.videouploader.extensions.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.stekeblad.videouploader.extensions.gson.booleanProperty.SimpleBooleanPropertyDeserializer;
import io.github.stekeblad.videouploader.extensions.gson.booleanProperty.SimpleBooleanPropertyInstanceCreator;
import io.github.stekeblad.videouploader.extensions.gson.booleanProperty.SimpleBooleanPropertySerializer;
import io.github.stekeblad.videouploader.extensions.gson.listProperty.SimpleListPropertyDeserializers;
import io.github.stekeblad.videouploader.extensions.gson.listProperty.SimpleListPropertyInstanceCreator;
import io.github.stekeblad.videouploader.extensions.gson.listProperty.SimpleListPropertySerializer;
import io.github.stekeblad.videouploader.extensions.gson.objectProperty.SimpleObjectPropertyDeserializers;
import io.github.stekeblad.videouploader.extensions.gson.objectProperty.SimpleObjectPropertyInstanceCreator;
import io.github.stekeblad.videouploader.extensions.gson.objectProperty.SimpleObjectPropertySerializer;
import io.github.stekeblad.videouploader.extensions.gson.stringProperty.SimpleStringPropertyDeserializer;
import io.github.stekeblad.videouploader.extensions.gson.stringProperty.SimpleStringPropertyInstanceCreator;
import io.github.stekeblad.videouploader.extensions.gson.stringProperty.SimpleStringPropertySerializer;
import io.github.stekeblad.videouploader.extensions.jfx.objectProperties.FileObjectProperty;
import io.github.stekeblad.videouploader.extensions.jfx.objectProperties.LocalCategoryObjectProperty;
import io.github.stekeblad.videouploader.extensions.jfx.objectProperties.LocalPlaylistObjectProperty;
import io.github.stekeblad.videouploader.extensions.jfx.objectProperties.VisibilityStatusObjectProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;

public class MyGsonFactory {
    /**
     * @return A instance of {@link Gson Gson} with all custom TypeAdapters, serializer, deserializers
     * and other settings configured to fit this project
     */
    public static Gson CreateGsonInstance() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(StringProperty.class, new SimpleStringPropertyInstanceCreator())
                .registerTypeAdapter(StringProperty.class, new SimpleStringPropertyDeserializer())
                .registerTypeAdapter(StringProperty.class, new SimpleStringPropertySerializer())

                .registerTypeAdapter(BooleanProperty.class, new SimpleBooleanPropertyInstanceCreator())
                .registerTypeAdapter(BooleanProperty.class, new SimpleBooleanPropertyDeserializer())
                .registerTypeAdapter(BooleanProperty.class, new SimpleBooleanPropertySerializer())

                .registerTypeAdapter(ListProperty.class, new SimpleListPropertyInstanceCreator<>())
                .registerTypeAdapter(ListProperty.class, new SimpleListPropertyDeserializers.StringListPropertyDeserializer())
                .registerTypeAdapter(ListProperty.class, new SimpleListPropertySerializer<>())

                .registerTypeAdapter(ObjectProperty.class, new SimpleObjectPropertyInstanceCreator<>())
                .registerTypeAdapter(VisibilityStatusObjectProperty.class, new SimpleObjectPropertySerializer.VisibilityStatusObjectSerializer())
                .registerTypeAdapter(LocalPlaylistObjectProperty.class, new SimpleObjectPropertySerializer.LocalPlaylistObjectSerializer())
                .registerTypeAdapter(LocalCategoryObjectProperty.class, new SimpleObjectPropertySerializer.LocalCategoryObjectSerializer())
                .registerTypeAdapter(FileObjectProperty.class, new SimpleObjectPropertySerializer.FileObjectSerializer())
                .registerTypeAdapter(VisibilityStatusObjectProperty.class, new SimpleObjectPropertyDeserializers.VisibilityStatusObjectPropertyDeserializer())
                .registerTypeAdapter(LocalPlaylistObjectProperty.class, new SimpleObjectPropertyDeserializers.LocalPlaylistObjectPropertyDeserializer())
                .registerTypeAdapter(LocalCategoryObjectProperty.class, new SimpleObjectPropertyDeserializers.LocalCategoryObjectPropertyDeserializer())
                .registerTypeAdapter(FileObjectProperty.class, new SimpleObjectPropertyDeserializers.FileObjectPropertyDeserializer())
                .registerTypeAdapter(ObjectProperty.class, new SimpleObjectPropertySerializer<>())
                .create();
    }
}
