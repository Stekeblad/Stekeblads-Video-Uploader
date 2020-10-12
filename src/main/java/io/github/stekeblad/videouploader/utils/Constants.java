package io.github.stekeblad.videouploader.utils;

import io.github.stekeblad.videouploader.jfxExtension.WindowDimensionsRestriction;

public class Constants {
    // Constants for directory names used to store settings and other configuration
    public static final String DATA_DIR = "VideoUploader data";
    public static final String AUTH_DIR = DATA_DIR + "/.auth";
    public static final String PRESET_DIR = DATA_DIR + "/presets";
    public static final String UPLOAD_DIR = DATA_DIR + "/waiting uploads";
    public static final String PLAYLIST_FILE = DATA_DIR + "/playlist.json";
    public static final String CATEGORIES_FILE = DATA_DIR + "/categories.json";
    public static final String CONFIG_BACKUP_DIR = DATA_DIR + "/backup";

    public static final String BUTTON_EDIT = "_buttonEdit";
    public static final String BUTTON_SAVE = "_buttonSave";
    public static final String BUTTON_CANCEL = "_buttonCancel";
    public static final String BUTTON_DELETE = "_buttonDelete";
    public static final String BUTTON_START_UPLOAD = "_buttonStartUpload";
    public static final String BUTTON_ABORT_UPLOAD = "_buttonUploadAbort";
    public static final String BUTTON_FINISHED_UPLOAD = "_buttonFinishedUpload";
    public static final String BUTTON_RESET = "_buttonReset";
    public static final String BUTTON_CLONE = "_buttonClone";

    public static final String DEFAULT_LOCALE = "default (english)";

    public static final WindowDimensionsRestriction MAIN_WINDOW_DIMENSIONS_RESTRICTION =
            new WindowDimensionsRestriction(800, 2000, 400, 3000);
    public static final WindowDimensionsRestriction SETTINGS_WINDOW_DIMENSIONS_RESTRICTION =
            new WindowDimensionsRestriction(450, 850, 350, 850);
    public static final WindowDimensionsRestriction PLAYLISTS_WINDOW_DIMENSIONS_RESTRICTION =
            new WindowDimensionsRestriction(350, 700, 250, 1000);
    public static final WindowDimensionsRestriction LOCALIZE_WINDOW_DIMENSIONS_RESTRICTION =
            new WindowDimensionsRestriction(300, 600, 350, 650);
    public static final WindowDimensionsRestriction META_TOOL_WINDOW_DIMENSIONS_RESTRICTION =
            new WindowDimensionsRestriction(350, 1500, 350, 1000);

    public static final class WindowPropertyNames {
        private static final String WINDOW_PREFIX = "window_";
        public static final String MAIN = WINDOW_PREFIX + "main";
        public static final String PRESETS = WINDOW_PREFIX + "preset";
        public static final String SETTINGS = WINDOW_PREFIX + "settings";
        public static final String LOCALIZE = WINDOW_PREFIX + "localize";
        public static final String PLAYLISTS = WINDOW_PREFIX + "playlist";
        public static final String META_TOOL = WINDOW_PREFIX + "meta-tool";
    }
}
