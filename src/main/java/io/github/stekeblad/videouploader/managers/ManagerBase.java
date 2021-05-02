package io.github.stekeblad.videouploader.managers;

import com.google.gson.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

abstract class ManagerBase {

    protected JsonObject config;
    protected Gson gson;

    protected ManagerBase() {
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    protected void loadConfigFromFile(Path path) throws IOException {
        final String settingsFileContent = Files.readString(path);
        config = JsonParser.parseString(settingsFileContent).getAsJsonObject();
    }

    protected void writeConfigToFile(Path path) throws IOException {
        Files.writeString(path, gson.toJson(config));
    }

    protected void setIfMissing(String prop, String value) {
        if (!config.has(prop))
            config.addProperty(prop, value);
    }

    protected void setIfMissing(String prop, boolean value) {
        if (!config.has(prop))
            config.addProperty(prop, value);
    }

    /**
     * Use for int, double, float, long, short etc.
     */
    protected void setIfMissing(String prop, Number value) {
        if (!config.has(prop))
            config.addProperty(prop, value);
    }

    protected void setIfMissing(String prop, Object value) {
        if (!config.has(prop))
            config.add(prop, gson.toJsonTree(value));
    }

    protected void set(String prop, String value) {
        config.addProperty(prop, value);
    }

    protected void set(String prop, boolean value) {
        config.addProperty(prop, value);
    }

    /**
     * Use for int, double, float, long, short etc.
     */
    protected void set(String prop, Number value) {
        config.addProperty(prop, value);
    }

    protected void set(String prop, Object value) {
        config.add(prop, gson.toJsonTree(value));
    }

    protected String getString(String prop) {
        assertProperty(prop);
        return config.get(prop).getAsString();
    }

    protected boolean getBoolean(String prop) {
        assertProperty(prop);
        return config.get(prop).getAsBoolean();
    }

    // no Number generic thing on get methods
    protected int getInt(String prop) {
        assertProperty(prop);
        return config.get(prop).getAsInt();
    }

    // no Number generic thing on get methods
    protected double getDouble(String prop) {
        assertProperty(prop);
        return config.get(prop).getAsDouble();
    }

    /**
     * Reads a jsonObject with the given key from the config object and attempts to deserialize
     * it to an instance of the given class
     *
     * @param prop     the object's key
     * @param classOfT The class to deserialize the object to
     * @param <T>      Class type to deserialize the object to
     * @return an instance of T created from the jsonObject under the key prop
     */
    protected <T> T getClass(String prop, Class<T> classOfT) {
        assertProperty(prop);
        return gson.fromJson(config.get(prop), classOfT);
    }

    /**
     * Reads an jsonArray with the given key from the config object and attempts to deserialize
     * it to an ArrayList with elements of the given class
     *
     * @param prop         the array's key
     * @param listItemType the class the elements in the array should be deserialized to
     * @param <T>          the class type the elements in the array should be deserialized to
     * @return an ArrayList&lt;T&gt; created from the objects in the array under the key prop
     */
    protected <T> ArrayList<T> getArrayList(String prop, Class<T> listItemType) {
        assertProperty(prop);
        JsonArray jsonArray = config.getAsJsonArray(prop);
        ArrayList<T> arrayList = new ArrayList<>(jsonArray.size());
        for (JsonElement elem : jsonArray) {
            arrayList.add(gson.fromJson(elem, listItemType));
        }

        return arrayList;
    }

    /**
     * Throws an exception if config does not contain a property with the name prop.
     * Throws an NullPointerException if prop is null
     *
     * @param prop the name of a property to check if it exist
     */
    private void assertProperty(String prop) {
        if (prop == null)
            throw new NullPointerException();

        if (!config.has(prop)) {
            String errorMessage = "Property " + prop + " not found in the following object:";
            try {
                errorMessage += gson.toJson(config);
            } catch (Exception ignored) {
                errorMessage += "Error - Could not convert to a string";
            }
            throw new RuntimeException(errorMessage);
        }
    }
}
