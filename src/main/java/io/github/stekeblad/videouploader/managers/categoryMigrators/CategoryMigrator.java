package io.github.stekeblad.videouploader.managers.categoryMigrators;

import com.google.gson.JsonObject;
import io.github.stekeblad.videouploader.managers.ConfigurationVersionException;

import java.util.List;

/**
 * Convert old formats for storing categories to the latest format.
 */
public class CategoryMigrator {
    public static final int latestFormatVersion = 3;

    public JsonObject migrate(List<String> oldFormat) {
        JsonObject newJson = new CategoryNaNTo3Migrator().migrate(oldFormat);
        return migrate(newJson);
    }

    public JsonObject migrate(JsonObject oldJson) {
        final int currentVersion = getVersion(oldJson);
        if (currentVersion > latestFormatVersion)
            throw new ConfigurationVersionException("The categories file uses the version " + currentVersion +
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
     * Checks if the given object is using the latest format, it only checks the version
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
