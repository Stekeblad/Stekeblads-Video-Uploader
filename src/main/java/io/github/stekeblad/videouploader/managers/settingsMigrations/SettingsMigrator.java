package io.github.stekeblad.videouploader.managers.settingsMigrations;

import com.google.gson.JsonObject;
import io.github.stekeblad.videouploader.managers.ConfigurationVersionException;

import java.util.Properties;

/**
 * This class looks at the content of a settings json object (or old Properties object),
 * checking if it is the latest format. The class also has methods for upgrading a old
 * settings object to the latest version.
 */
public class SettingsMigrator {
    public static final int latestFormatVersion = 3;

    /**
     * Converts from the settings.properties format to the latest version of the newer settings.json format
     *
     * @param oldProps The loaded settings.properties file
     * @return a json settings object using the latest format
     */
    public JsonObject migrate(Properties oldProps) {
        JsonObject newJson = new SettingsNaNTo3Migration().migrate(oldProps);
        return migrate(newJson);
    }

    /**
     * Returns an updated version of the given object after adding, removing or modifying values so it is
     * using the latest configuration format
     *
     * @param oldJson An object using an older format
     * @return the updated object
     */
    public JsonObject migrate(JsonObject oldJson) {
        final int currentVersion = getVersion(oldJson);
        if (currentVersion > latestFormatVersion)
            throw new ConfigurationVersionException("The settings file uses the version " + currentVersion +
                    " format, however the latest supported version is only " + latestFormatVersion +
                    ". The file may fail to load or you may run into other problems because of this");

        // Insert calls to newer migrations here if newer versions ever is introduced
        switch (currentVersion) {
            case 4:
                // oldJson = new Settings3To4Migrator().migrate(oldJson);
            case 5:
                // oldJson = new Settings4To5Migrator().migrate(oldJson);
            case 6:
                // etc.
            default:
                return oldJson;
        }
    }

    /**
     * Checks if the given object is using the latest settings format, it only checks the version
     * number field and do not perform a complete verification of the whole object
     *
     * @param json the object to check the version of
     * @return true if the object is using the latest version format, false if the current version is <b>smaller or
     * greater</b> than the latest
     */
    public boolean isLatestVersion(JsonObject json) {
        return getVersion(json) == latestFormatVersion;
    }

    /**
     * Returns the version number for the current format used in the object
     *
     * @param json the object to check
     * @return its current version number
     */
    private int getVersion(JsonObject json) {
        return json.get("versionFormat").getAsInt();
    }
}
