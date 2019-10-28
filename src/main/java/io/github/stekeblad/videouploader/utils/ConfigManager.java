package io.github.stekeblad.videouploader.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;
import java.util.zip.DataFormatException;

import static io.github.stekeblad.videouploader.utils.Constants.*;

/**
 * A Enum-Singleton class for handling different settings
 */
public enum ConfigManager {
    INSTANCE;

    private Path filesPath;
    private Properties mainProp;
    private HashMap<String, String> presetStringsMap;

    /**
     * Like a constructor, call this method once before calling any other method in this class anywhere in the project
     */
    public void configManager() {
        // Check for the existence of configuration files and create their directories if not found
        filesPath = Paths.get(DATA_DIR).toAbsolutePath();
        Path presetsPath = Paths.get(PRESET_DIR).toAbsolutePath();
        Path waitingUploadsPath = Paths.get(UPLOAD_DIR).toAbsolutePath();
        mainProp = new Properties();

        if (!Files.exists(filesPath)) {
            try {
                Files.createDirectory(filesPath);
            } catch (IOException e) {
                System.err.println("Could not find or create directory for program files!");
                e.printStackTrace();
            }
        }

        if (!Files.exists(presetsPath)) {
            try {
                Files.createDirectory(presetsPath);
            } catch (IOException e) {
                System.err.println("Could not find or create directory for presets!");
                e.printStackTrace();
            }
            // just initialize presetStringsMap
            presetStringsMap = new HashMap<>();
        } else { // do not attempt to load presets if presets directory did not exist

            ArrayList<String> presetNames = loadSavedPresetNamesList();
            loadSavedPresets(presetNames);
        }

        if (!Files.exists(waitingUploadsPath)) {
            try {
                Files.createDirectory(waitingUploadsPath);
            } catch (IOException e) {
                System.err.println("Could not find or create directory for waiting uploads!");
                e.printStackTrace();
            }
        }

        loadSettings();
    }

    /**
     * Reads the settings.properties file. If it is not found then it is created and default values is assigned to the variables
     */
    private void loadSettings() {
        InputStream input = null;
        try {
            input = new FileInputStream(filesPath + "/settings.properties");
            mainProp.load(input);
        } catch (FileNotFoundException e) {
            try {
                Files.createFile(Paths.get(filesPath + "/settings.properties"));
            } catch (IOException e1) {
                System.err.println("Could not generate empty settings file");
            }
        } catch (IOException e) {
            System.err.println("Failed reading settings file");
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    System.err.println("Could not close settings file");
                }
            }

            // Set missing properties
            setIfMissing("noSettings", "true");
            setIfMissing("neverAuthed", "true");
            setIfMissing("category_country", "");
            setIfMissing("category_language", "");
            setIfMissing("ui_language", String.valueOf(Locale.getDefault()));

            setIfMissing(WIN_SIZE + WindowPropertyNames.MAIN, "900x825");
            setIfMissing(WIN_LOC + WindowPropertyNames.MAIN, "50x50");
            setIfMissing(WIN_SIZE + WindowPropertyNames.PRESETS, "725x700");
            setIfMissing(WIN_LOC + WindowPropertyNames.PRESETS, "150x100");
            setIfMissing(WIN_SIZE + WindowPropertyNames.SETTINGS, "600x450");
            setIfMissing(WIN_LOC + WindowPropertyNames.SETTINGS, "200x150");
            setIfMissing(WIN_SIZE + WindowPropertyNames.LOCALIZE, "400x450");
            setIfMissing(WIN_LOC + WindowPropertyNames.LOCALIZE, "275x250");
            setIfMissing(WIN_SIZE + WindowPropertyNames.PLAYLISTS, "400x500");
            setIfMissing(WIN_LOC + WindowPropertyNames.PLAYLISTS, "250x200");

        }
    }

    private void setIfMissing(String prop, String value) {
        if (mainProp.getProperty(prop) == null)
            mainProp.setProperty(prop, value);
    }

    /**
     * Saves the properties to the settings.properties file
     */
    public void saveSettings() {
        OutputStream output = null;
        try {
            output = new FileOutputStream(filesPath + "/settings.properties");
            mainProp.store(output, "main settings file for Stekeblads Video Uploader");
        } catch (FileNotFoundException e) {
            System.err.println("File is not a file or do not have permission to create settings file");
        } catch (IOException e) {
            System.err.println("Error writing settings to file");
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Properties (Set/Get)

    public boolean getNeverAuthed() {
        return mainProp.getProperty("neverAuthed").equals("true");
    }

    public void setNeverAuthed(boolean neverAuthed) {
        mainProp.setProperty("neverAuthed", neverAuthed ? "true" : "false");
    }

    public String getCategoryCountry() {
        return mainProp.getProperty("category_country");
    }

    public void setCategoryCountry(String twoCharCode) throws DataFormatException {
        if(twoCharCode.length() != 2) {
            throw new DataFormatException("Invalid country code format");
        }
        mainProp.setProperty("category_country", twoCharCode.toUpperCase());
    }

    public String getCategoryLanguage() {
        return mainProp.getProperty("category_language");
    }

    public void setCategoryLanguage(String twoCharCode) throws DataFormatException {
        if(twoCharCode.length() != 2) {
            throw new DataFormatException("Invalid country code format");
        }
        mainProp.setProperty("category_language", twoCharCode.toLowerCase());
    }

    public String getSelectedLanguage() {
        return mainProp.getProperty("ui_language");
    }

    public void setSelectedLanguage(String languageName) {
        mainProp.setProperty("ui_language", languageName);
    }

    private static final String WIN_LOC = "window_location_";
    private static final String WIN_SIZE = "window_size_";

    public static final class WindowPropertyNames {
        public static final String MAIN = "main";
        public static final String PRESETS = "preset";
        public static final String SETTINGS = "settings";
        public static final String LOCALIZE = "localize";
        public static final String PLAYLISTS = "playlist";
    }

    public void setWindowRectangle(String window, WindowFrame rect) {
        String data = rect.x + "x" + rect.y;
        mainProp.setProperty(WIN_LOC + window, data);
        data = rect.width + "x" + rect.height;
        mainProp.setProperty(WIN_SIZE + window, data);
    }

    public WindowFrame getWindowRectangle(String window) {
        String[] loc = mainProp.getProperty(WIN_LOC + window).split("x");
        String[] size = mainProp.getProperty(WIN_SIZE + window).split("x");
        return new WindowFrame(Double.parseDouble(loc[0]), Double.parseDouble(loc[1]),
                Double.parseDouble(size[0]), Double.parseDouble(size[1]));
    }

    // Presets

    /**
     * Reads the names of all saved presets from disc and return their names
     * @return a ArrayList with the names of all saved presets
     */
    private ArrayList<String> loadSavedPresetNamesList() {
        ArrayList<String> presetNames = new ArrayList<>();
        File dir = new File(PRESET_DIR);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File preset : directoryListing) {
                presetNames.add(preset.getName());
            }
        }
        return presetNames;
    }

    /**
     * Loads the presets in presetNames from disc
     * @param presetNames the presets to load
     */
    private void loadSavedPresets(ArrayList<String> presetNames) {
        presetStringsMap = new HashMap<>();
        for(String presetName : presetNames) {
            try {
                presetStringsMap.put(presetName, loadPreset(presetName));
            } catch (IOException e) {
                System.err.println("Error reading preset file: \"" + presetName + "\"");
                e.printStackTrace();
            }
        }
    }

    /**
     * Loads the preset presetName from disc
     * @param presetName name of the preset to load
     * @return the string representation of the preset
     * @throws IOException if presetName does not exist or could not be accessed
     */
    private String loadPreset(String presetName) throws IOException {
        if (Files.exists(Paths.get(PRESET_DIR + "/" + presetName))) {
            try {
                return FileUtils.readAll(PRESET_DIR + "/" + presetName);
            } catch (IOException e) {
                throw new IOException("Failed reading preset save file for preset \"" + presetName + "\"", e);
            }
        } else {
            throw new IOException("Preset " + presetName + " does not exist or cant be accessed");
        }
    }

    /**
     * Writes a preset to disc
     * @param presetName the name of the preset
     * @param stringRepresentation the string representation of the preset.
     */
    public void savePreset(String presetName, String stringRepresentation) {
        try {
            FileUtils.writeAll(PRESET_DIR + "/" + presetName, stringRepresentation);
        } catch (IOException e) {
            System.err.println("Can not save preset \"" + presetName + "\"");
            e.printStackTrace();
        }
        presetStringsMap.put(presetName, stringRepresentation);
    }

    /**
     * Deletes a preset from disc.
     * @param presetName the name of the preset file to delete.
     * @return true on success, false on failure.
     */
    public boolean deletePreset(String presetName) {
        Path path = Paths.get(PRESET_DIR + "/" + presetName);
        if (!Files.exists(path)) {
            presetStringsMap.remove(presetName);
            return true; //does not exist, job already done
        }
        try {
            Files.delete(path);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        presetStringsMap.remove(presetName);
        return true;
    }

    /**
     *
     * @return a ArrayList with the names of all loaded presets.
     */
    public ArrayList<String> getPresetNames() {
        if(presetStringsMap != null) {
            return new ArrayList<>(presetStringsMap.keySet());
        }
        return null;
    }

    /**
     *
     * @param presetName the name of a loaded preset
     * @return the string representation of that preset or null if it does not exist
     */
    public String getPresetString(String presetName) {
        if (presetStringsMap != null) {
            return presetStringsMap.getOrDefault(presetName, null);
        }
        return null;
    }

    // Playlists

    /**
     * Writes the list of playlists to disc
     * @param playlistData the playlist data to save
     */
    public void savePlaylistCache(String playlistData) {
        try {
            FileUtils.writeAll(PLAYLIST_FILE, playlistData);
        } catch (IOException e) {
            System.err.println("Could not save playlists to file");
            e.printStackTrace();
        }
    }

    /**
     * Reads stored playlists from disc
     * @return a ArrayList with one row of the save file per element
     */
    public ArrayList<String> loadPlaylistCache() {
        ArrayList<String> playlistString = null;
        try {
            playlistString = FileUtils.readAllLines(PLAYLIST_FILE);
        } catch (FileNotFoundException e) {
            System.err.println("Could not find playlist cache file");
        } catch (IOException e) {
            System.err.println("Could not create playlists cache file");
        }
        return playlistString;
    }

    // Waiting Uploads

    /**
     * Checks if there is any waiting uploads stored
     * @return true if there are at least one, false if zero.
     */
    public boolean hasWaitingUploads() {
        File dir = new File(UPLOAD_DIR);
        File[] directoryListing = dir.listFiles();
        return directoryListing != null;
    }

    /**
     *
     * @return a list of string representations of uploads that was saved
     */
    public ArrayList<String> getWaitingUploads() {
        File dir = new File(UPLOAD_DIR);
        File[] directoryListing = dir.listFiles();
        if(directoryListing == null) {
            return null;
        }
        ArrayList<String> uploads = new ArrayList<>();
        for (File waitingUpload : directoryListing) {
            String loaded = loadWaitingUploadsFile(waitingUpload);
            if (loaded != null) {
                uploads.add(loaded);
            }
            if(! waitingUpload.delete()) {
                System.err.println("Failed to delete: " + waitingUpload.getAbsolutePath());
            }
        }
        return uploads;
    }

    /**
     * Save waiting uploads to disc so they can be recreate next time.
     * @param waitingUpload string representation of the upload to save
     * @param fileName name of the file to save it in, does not really matter as long as it is valid for the OS. It is
     *                 to the caller to make sure a previously saved upload is not overwritten.
     */
    public void saveWaitingUpload(String waitingUpload, String fileName) {
        try {
            FileUtils.writeAll(UPLOAD_DIR + "/" + fileName, waitingUpload);
        } catch (IOException e) {
            System.err.println("Failed saving waiting upload \"" + fileName + "\"");
            e.printStackTrace();
        }
    }

    /**
     * Loads a waiting upload from disc.
     * @param waitingUpload name of the file
     * @return the content of the file (a string representation of the upload)
     */
    private String loadWaitingUploadsFile(File waitingUpload) {
        try {
            return FileUtils.readAll(waitingUpload.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error loading a waiting upload \"" + waitingUpload.getName() + "\"");
            e.printStackTrace();
            return null;
        }
    }

    // Categories

    /**
     * Writes categoryData to the categories file
     * @param categoryData information about categories that should be saved
     */
    public void saveLocalizedCategories(String categoryData) {
        try {
            FileUtils.writeAll(CATEGORIES_FILE, categoryData);
        } catch (IOException e) {
            System.err.println("Could not save categories");
            e.printStackTrace();
        }
    }

    /**
     * Reads the content of the categories file
     * @return the content of the categories file with one line per element in the ArrayList
     */
    public ArrayList<String> loadLocalizedCategories() {
        try {
            if (!Files.exists(Paths.get(CATEGORIES_FILE))) {
                Files.createFile(Paths.get(CATEGORIES_FILE));
                return new ArrayList<>();
            }
        } catch (FileNotFoundException e) {
            System.err.println("Could not find or access categories file");
            e.printStackTrace();
            return new ArrayList<>();
        } catch (IOException e1) {
            System.err.println("Could not create new categories file");
            e1.printStackTrace();
            return new ArrayList<>();
        }

        try {
            return FileUtils.readAllLines(CATEGORIES_FILE);
        } catch (IOException e) {
            System.err.println("Could not read categories file");
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
