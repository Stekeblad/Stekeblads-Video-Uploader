package io.github.stekeblad.videouploader.Managers;

import com.google.gson.JsonObject;
import io.github.stekeblad.videouploader.Managers.SettingsMigrations.SettingsMigrator;
import io.github.stekeblad.videouploader.jfxExtension.WindowFrame;
import io.github.stekeblad.videouploader.utils.AlertUtils;
import io.github.stekeblad.videouploader.utils.TimeUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Properties;
import java.util.stream.Stream;

import static io.github.stekeblad.videouploader.utils.Constants.*;

/**
 * Class for handling all settings of the program. Does not handle for example
 * presets or playlist as they have there own manager classes.
 */
public class SettingsManager extends ManagerBase {

    private static SettingsManager _manager;

    /**
     * Returns the shared SettingsManager, creating it if this is the first time this
     * method gets called since the program started.
     */
    public static SettingsManager getSettingsManager() {
        if (_manager == null)
            _manager = new SettingsManager();

        return _manager;
    }

    private final Path filesPath;
    private final Path presetsPath;
    private final Path waitingUploadsPath;
    private final Path backupPath;
    private final Path authPath;

    private SettingsManager() {
        filesPath = Paths.get(DATA_DIR).toAbsolutePath();
        presetsPath = Paths.get(PRESET_DIR).toAbsolutePath();
        waitingUploadsPath = Paths.get(UPLOAD_DIR).toAbsolutePath();
        backupPath = Paths.get(CONFIG_BACKUP_DIR).toAbsolutePath();
        authPath = Paths.get(AUTH_DIR).toAbsolutePath();

        ensureConfigurationDirectoryStructure();
        loadSettings();
    }

    /**
     * Create the Uploader data settings directory and the subdirectories used by the program
     */
    private void ensureConfigurationDirectoryStructure() {
        try {
            if (!Files.exists(filesPath))
                Files.createDirectory(filesPath);
            if (!Files.exists(presetsPath))
                Files.createDirectory(presetsPath);
            if (!Files.exists(waitingUploadsPath))
                Files.createDirectory(waitingUploadsPath);
            if (!Files.exists(backupPath))
                Files.createDirectory(backupPath);
        } catch (IOException e) {
            AlertUtils.exceptionDialog("Could not create settings files",
                    "An error occurred when trying to create the \"" + DATA_DIR +
                            "\" settings directory or one of its subdirectories",
                    e);
        }
    }

    /**
     * Loads the settings file if it exists.
     * <p>
     * If the settings file uses an old format it is backed up and then updated to the newest format.
     * If it does not exist then a new is created with default values.
     * </p>
     */
    private void loadSettings() {
        config = null;
        SettingsMigrator settingsMigrator = new SettingsMigrator();

        final String oldSettingsFilePath = filesPath + "/settings.properties";
        if (Files.exists(Paths.get(oldSettingsFilePath))) {
            FileInputStream propInputStream = null;
            try {
                // Backup old config file before converting to new format
                final String backupFileName = "/settings-" + TimeUtils.currentTimeString() + ".properties";
                Files.copy(Paths.get(oldSettingsFilePath), Paths.get(CONFIG_BACKUP_DIR + backupFileName));
                // Read, convert, save and then delete the old file
                Properties prop = new Properties();
                propInputStream = new FileInputStream(oldSettingsFilePath);
                prop.load(propInputStream);
                config = settingsMigrator.migrate(prop);
                writeConfigToFile(Paths.get(filesPath + "/settings.json"));
                Files.delete(Paths.get(oldSettingsFilePath));
            } catch (IOException ignored) {
            } finally {
                if (propInputStream != null) {
                    try {
                        propInputStream.close();
                    } catch (IOException e) {
                        AlertUtils.exceptionDialog("Failed to load or update settings file",
                                "The settings file could not be read. If the program have updated since last run something" +
                                        " could have failed while updating the settings file to a newer version",
                                e);
                    }
                }
            }
            return;
        }

        if (Files.exists(Paths.get(filesPath + "/settings.json"))) {
            try {
                loadConfigFromFile(Paths.get(filesPath + "/settings.json"));
                if (!settingsMigrator.isLatestVersion(config)) {
                    // File is in a older format, create a backup of it and then upgrade to latest format
                    final String backupFileName = "/settings-" + TimeUtils.currentTimeString() + ".json";
                    Files.copy(Paths.get(filesPath + "/settings.json"), Paths.get(CONFIG_BACKUP_DIR + backupFileName));
                    settingsMigrator.migrate(config);
                    writeConfigToFile(Paths.get(filesPath + "/settings.json"));
                }
            } catch (IOException e) {
                AlertUtils.exceptionDialog("Failed to load or update settings file",
                        "The settings file could not be read. If the program have updated since last run something" +
                                " could have failed while updating the settings file to a newer version",
                        e);
            }
            return;
        }

        // create default configuration
        config = new JsonObject();

        set("category_country", "");
        set("category_language", "");
        set("ui_language", String.valueOf(Locale.getDefault()));
        set("checkForUpdates", true);
        set("silentUpdates", false);
        set("channelName", "");
        set("versionFormat", SettingsMigrator.latestFormatVersion);

        // Default window sizes and locations:
        set(WindowPropertyNames.MAIN, new WindowFrame(150, 100, 900, 750));
        set(WindowPropertyNames.PRESETS, new WindowFrame(150, 100, 725, 700));
        set(WindowPropertyNames.SETTINGS, new WindowFrame(200, 150, 600, 500));
        set(WindowPropertyNames.LOCALIZE, new WindowFrame(275, 250, 400, 450));
        set(WindowPropertyNames.PLAYLISTS, new WindowFrame(250, 200, 400, 500));
        set(WindowPropertyNames.META_TOOL, new WindowFrame(250, 200, 400, 500));
    }

    /**
     * Checks if there is any waiting uploads stored
     *
     * @return true if there are at least one, false if zero.
     */
    public boolean hasWaitingUploads() {
        try (Stream<Path> stream = Files.list(waitingUploadsPath)) {
            return stream.findAny().isPresent();
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Checks if the user have authenticated with YouTube
     *
     * @return false if they have, true if not.
     */
    public boolean getNeverAuthed() {
        try (Stream<Path> stream = Files.list(authPath)) {
            return stream.findAny().isEmpty();
        } catch (IOException e) {
            return true;
        }
    }

    // ----------------------------------------------------
    // Simple get and set methods for a bunch of settings
    // ----------------------------------------------------

    // Stores the country code used for the last successful retrieval of video categories
    public String getCategoryCountry() {
        return getString("category_country");
    }

    public void setCategoryCountry(String twoCharCode) {
        set("category_country", twoCharCode.toUpperCase());
    }

    // Stores the language code used for the last successful retrieval of video categories
    public String getCategoryLanguage() {
        return getString("category_language");
    }

    public void setCategoryLanguage(String twoCharCode) {
        set("category_language", twoCharCode.toLowerCase());
    }

    // The language to show Stekeblads Video Uploader in, if it is available
    public String getSelectedLanguage() {
        return getString("ui_language");
    }

    public void setSelectedLanguage(String languageName) {
        set("ui_language", languageName);
    }

    // If this is set to true should Stekeblads Video Uploader check for updates when starting
    public boolean getCheckForUpdates() {
        return getBoolean("checkForUpdates");
    }

    public void setCheckForUpdates(boolean enable) {
        set("checkForUpdates", enable);
    }

    // If this is set to true should Stekeblads Video Uploader download new updates
    // without asking the user for confirmation first
    public boolean getSilentUpdates() {
        return getBoolean("silentUpdates");
    }

    public void setSilentUpdates(boolean enable) {
        set("silentUpdates", enable);
    }

    // The name of the user's channel, displayed in the title of the main window
    public String getChannelName() {
        return getString("channelName");
    }

    public void setChannelName(String channelName) {
        set("channelName", channelName);
    }

    // getter and setter for window sizes and locations
    public WindowFrame getWindowFrame(String windowName) {
        return getClass(windowName, WindowFrame.class);
    }

    public void setWindowFrame(String windowName, WindowFrame frame) {
        set(windowName, frame);
    }
}
