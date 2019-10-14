package io.github.stekeblad.videouploader.utils;

public class Constants {
    public static final String DATA_DIR = "VideoUploader data";
    public static final String AUTH_DIR = DATA_DIR + "/.auth";
    public static final String PRESET_DIR = DATA_DIR + "/presets";
    public static final String UPLOAD_DIR = DATA_DIR + "/waiting uploads";
    public static final String PLAYLIST_FILE = DATA_DIR + "/playlist";
    public static final String CATEGORIES_FILE = DATA_DIR + "/categories";

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

    public static final WindowDimensionsRestriction SETTINGS_WINDOW_DIMENSIONS_RESTRICTION =
            new WindowDimensionsRestriction(500, 500, 500, 500);


}
