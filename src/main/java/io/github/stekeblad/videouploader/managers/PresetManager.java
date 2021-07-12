package io.github.stekeblad.videouploader.managers;

import com.google.gson.JsonObject;
import io.github.stekeblad.videouploader.managers.presetMigrators.PresetMigrator;
import io.github.stekeblad.videouploader.models.NewVideoPresetModel;
import io.github.stekeblad.videouploader.utils.AlertUtils;
import io.github.stekeblad.videouploader.utils.Constants;
import io.github.stekeblad.videouploader.utils.RecursiveDirectoryDeleter;
import io.github.stekeblad.videouploader.utils.TimeUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

import static io.github.stekeblad.videouploader.utils.Constants.*;

public class PresetManager extends ManagerBase {

    private static PresetManager _manager;

    /**
     * Returns the shared PresetManager, creating it if this is the first time this
     * method gets called since the program started.
     */
    public static PresetManager getPresetManager() {
        if (_manager == null)
            _manager = new PresetManager();

        return _manager;
    }

    private final Path presetsPath;
    private final ObservableList<NewVideoPresetModel> rawPresets;
    private final FilteredList<NewVideoPresetModel> presets;

    private PresetManager() {
        presetsPath = Paths.get(PRESETS_FILE).toAbsolutePath();
        PresetMigrator presetMigrator = new PresetMigrator();

        Path oldPresetsDir = Paths.get(DATA_DIR + "/presets");
        // Check for the oldest preset format
        if (Files.exists(oldPresetsDir)) {
            try {
                // back up
                final Path presetBackupsDir = Paths.get(CONFIG_BACKUP_DIR + "/presets-" + TimeUtils.currentTimeStringPathSafe());
                Files.copy(oldPresetsDir, presetBackupsDir);
                Files.walk(oldPresetsDir, 3, FileVisitOption.FOLLOW_LINKS)
                        .forEach(file -> {
                            try {
                                Files.copy(file, Paths.get(presetBackupsDir.toAbsolutePath().toString() + file.getFileName()));
                            } catch (IOException ignored) {
                            }
                        });

                // read all preset files, send them to migration and get a JsonObject back
                ArrayList<ArrayList<String>> oldPresets = new ArrayList<>();
                for (File oldFile : Objects.requireNonNull(oldPresetsDir.toFile().listFiles())) {
                    if (!oldFile.isFile())
                        continue;
                    oldPresets.add((ArrayList<String>) Files.readAllLines(oldFile.toPath()));
                }

                config = presetMigrator.migrate(oldPresets);

                // Delete old preset files
                Files.walkFileTree(oldPresetsDir, new RecursiveDirectoryDeleter());
            } catch (IOException ignored) {
            }
        } else if (Files.exists(presetsPath)) {
            // Data in json format found
            try {
                loadConfigFromFile(presetsPath);
                if (!presetMigrator.isLatestVersion(config)) {
                    // File is in a older format, create a backup of it and then upgrade to latest format
                    final String backupFileName = "/presets-" + TimeUtils.currentTimeStringPathSafe() + ".json";
                    Files.copy(presetsPath, Paths.get(CONFIG_BACKUP_DIR + backupFileName));
                    presetMigrator.migrate(config);
                    writeConfigToFile(presetsPath);
                }
            } catch (IOException e) {
                AlertUtils.exceptionDialog("Failed to load or update presets file",
                        "The presets file could not be read. If the program have updated since last run something" +
                                " could have failed while updating the presets file to a newer version",
                        e);
            }
        } else {
            // No saved presets found, create empty object
            config = new JsonObject();
            set(Constants.VERSION_FORMAT_KEY, PresetMigrator.latestFormatVersion);
            set("presets", new ArrayList<NewVideoPresetModel>());
        }

        rawPresets = FXCollections.observableArrayList(getArrayList("presets", NewVideoPresetModel.class)).sorted();
        presets = rawPresets.filtered(preset -> !preset.getVideoName().equals(NewVideoPresetModel.MAGIC_VIDEO_NAME));
    }

    /**
     * Saves all preset data to the presets save file
     *
     * @throws IOException if exception occurred when writing to the save file
     */
    public void savePresets() throws IOException {
        set("playlists", presets);
        writeConfigToFile(presetsPath);
    }

    /**
     * Returns an observable list with all stored presets, this list (probably) sorts itself to always be ordered by
     * preset name
     *
     * @return An observable list with all stored presets as VideoPresetModel objects
     */
    public ObservableList<NewVideoPresetModel> getAllPresets() {
        return presets;
    }

    public ObservableList<NewVideoPresetModel> getAllPresetsIncludingDefault() {
        return rawPresets;
    }

    /**
     * Looks for the first preset that have a name exactly matching the parameter presetName
     *
     * @param presetName The name of the preset to find
     * @return An Optional&lt;VideoPresetModel&gt; that either contains the first matching preset or is empty if
     * no preset matches
     */
    public Optional<NewVideoPresetModel> findByName(String presetName) {
        return rawPresets.stream().filter(p -> p.getPresetName().equals(presetName)).findFirst();
    }

    /**
     * Adds a new preset to the presets list
     *
     * @param newPreset the preset to add
     */
    public void addPreset(NewVideoPresetModel newPreset) {
        rawPresets.add(newPreset);
    }

    /**
     * Removes a preset from the presets list
     *
     * @param oldPreset the preset to remove
     */
    public void removePreset(NewVideoPresetModel oldPreset) {
        rawPresets.remove(oldPreset);
    }
}
