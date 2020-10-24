package io.github.stekeblad.videouploader.managers.presetMigrators;

import com.google.gson.JsonObject;
import io.github.stekeblad.videouploader.managers.ConfigurationVersionException;
import io.github.stekeblad.videouploader.utils.Constants;

import java.util.ArrayList;

/**
 * This class looks at the content of the presets json object (or a old preset format file),
 * checking if it is the latest format. The class also has methods for upgrading a old
 * preset to the latest version.
 */
public class PresetMigrator {
    public static final int latestFormatVersion = 3;

    /**
     * Converts from the old preset format to the latest version of the newer presets.json format
     *
     * @param oldFormat A preset on the old format
     * @return a json preset object using the latest format
     */
    public JsonObject migrate(ArrayList<ArrayList<String>> oldFormat) {
        JsonObject newJson = new PresetNaNTo3Migration().migrate(oldFormat);
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
            throw new ConfigurationVersionException("The presets file uses the version " + currentVersion +
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
        return json.get(Constants.VERSION_FORMAT_KEY).getAsInt();
    }
}
