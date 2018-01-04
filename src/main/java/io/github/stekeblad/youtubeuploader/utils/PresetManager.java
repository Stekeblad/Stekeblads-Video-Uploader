package io.github.stekeblad.youtubeuploader.utils;

import java.util.ArrayList;

public enum PresetManager {
    INSTANCE;

    private ConfigManager configManager = ConfigManager.INSTANCE;
    private ArrayList<String> presetNames = configManager.getSavedPresetNamesList();

    public ArrayList<String> getPresetNames() {
        return presetNames;
    }
}
