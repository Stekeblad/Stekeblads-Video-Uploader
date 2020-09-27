package io.github.stekeblad.videouploader.Managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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

    protected <T> T getClass(String prop, Class<T> classOfT) {
        assertProperty(prop);
        return gson.fromJson(config.get(prop), classOfT);
    }

    /**
     * Throws an exception if config does not contain a property with the name prop
     *
     * @param prop the name of a property to check if it exist
     */
    private void assertProperty(String prop) {
        if (!config.has(prop))
            throw new RuntimeException("Property \"" + prop + "\" not found");
    }
}
