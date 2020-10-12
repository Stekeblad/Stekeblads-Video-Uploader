package io.github.stekeblad.videouploader.managers.settingsMigrations;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.github.stekeblad.videouploader.jfxExtension.WindowFrame;
import io.github.stekeblad.videouploader.utils.Constants;

import java.util.Properties;

/**
 * Migration for turning the settings.properties file to version 3 of settings.json
 */
class SettingsNaNTo3Migration {
    JsonObject migrate(Properties oldProps) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject newJson = new JsonObject();

        // Settings that can be directly copied over
        newJson.addProperty("category_country", oldProps.getProperty("category_country"));
        newJson.addProperty("category_language", oldProps.getProperty("category_language"));
        newJson.addProperty("ui_language", oldProps.getProperty("ui_language"));
        newJson.addProperty("channelName", oldProps.getProperty("channelName"));
        newJson.addProperty("versionFormat", 3);

        // Convert so boolean settings actually are stored as boolean values
        boolean tempBool = Boolean.getBoolean(oldProps.getProperty("checkForUpdates"));
        newJson.addProperty("checkForUpdates", tempBool);

        tempBool = Boolean.getBoolean(oldProps.getProperty("silentUpdates"));
        newJson.addProperty("silentUpdates", tempBool);

        // Save WindowFrame data as an JsonObject
        newJson.add(Constants.WindowPropertyNames.MAIN,
                gson.toJsonTree(getWindowRectangle(oldProps, Constants.WindowPropertyNames.MAIN)));
        newJson.add(Constants.WindowPropertyNames.PRESETS,
                gson.toJsonTree(getWindowRectangle(oldProps, Constants.WindowPropertyNames.PRESETS)));
        newJson.add(Constants.WindowPropertyNames.SETTINGS,
                gson.toJsonTree(getWindowRectangle(oldProps, Constants.WindowPropertyNames.SETTINGS)));
        newJson.add(Constants.WindowPropertyNames.LOCALIZE,
                gson.toJsonTree(getWindowRectangle(oldProps, Constants.WindowPropertyNames.LOCALIZE)));
        newJson.add(Constants.WindowPropertyNames.PLAYLISTS,
                gson.toJsonTree(getWindowRectangle(oldProps, Constants.WindowPropertyNames.PLAYLISTS)));
        newJson.add(Constants.WindowPropertyNames.META_TOOL,
                gson.toJsonTree(getWindowRectangle(oldProps, Constants.WindowPropertyNames.META_TOOL)));

        return newJson;
    }

    /**
     * Copied directly from the old "ConfigManager" for getting the window sizes and locations
     *
     * @param mainProp The properties object
     * @param window   the name constant for the window settings will be retrieved from
     * @return a WindowFrame with size and location data for the requested window
     */
    private WindowFrame getWindowRectangle(Properties mainProp, String window) {
        final String WIN_LOC = "window_location_";
        final String WIN_SIZE = "window_size_";

        String[] loc = mainProp.getProperty(WIN_LOC + window).split("x");
        String[] size = mainProp.getProperty(WIN_SIZE + window).split("x");
        return new WindowFrame(Double.parseDouble(loc[0]), Double.parseDouble(loc[1]),
                Double.parseDouble(size[0]), Double.parseDouble(size[1]));
    }
}
