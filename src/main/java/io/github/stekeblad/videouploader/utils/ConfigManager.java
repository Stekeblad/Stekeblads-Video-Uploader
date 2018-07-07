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
            //if 0 properties was loaded, add default values
            if ( mainProp.size() == 0) {
                mainProp.setProperty("noSettings", "true");
                mainProp.setProperty("neverAuthed", "true");
                mainProp.setProperty("category_country", "");
                mainProp.setProperty("category_language", "");
                mainProp.setProperty("ui_language", String.valueOf(Locale.getDefault()));
            }
        }
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
            BufferedReader reader = new BufferedReader(new FileReader(
                    new File(PRESET_DIR + "/" + presetName)));
            StringBuilder stringBuilder = new StringBuilder();
            String line = reader.readLine();
            while (line != null) { // while not end of file
                stringBuilder.append(line);
                line = reader.readLine();
                if (line != null) {
                    stringBuilder.append("\n"); // do not end the last line with '\n'
                }
            }
            reader.close();
            return stringBuilder.toString();

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
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(
                    new File(PRESET_DIR + "/" + presetName)));
            writer.write(stringRepresentation);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(
                    new File(PLAYLIST_FILE)));
            writer.write(playlistData);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Reads stored playlists from disc
     * @return a ArrayList with one row of the save file per element
     */
    public ArrayList<String> loadPlaylistCache() {
        BufferedReader reader;
        ArrayList<String> playlistString = new ArrayList<>();
        try {
            if (!Files.exists(Paths.get(PLAYLIST_FILE))) {
                Files.createFile(Paths.get(PLAYLIST_FILE));
            } else {
                reader = new BufferedReader(new FileReader(
                        new File(PLAYLIST_FILE)));
                String line = reader.readLine();
                while (line != null) { // while not end of file
                    playlistString.add(line);
                    line = reader.readLine();
                }
            }
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
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(
                    new File(UPLOAD_DIR + "/" + fileName)));
            writer.write(waitingUpload);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Loads a waiting upload from disc.
     * @param waitingUpload name of the file
     * @return the content of the file (a string representation of the upload)
     */
    private String loadWaitingUploadsFile(File waitingUpload) {
        BufferedReader reader = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            reader = new BufferedReader(new FileReader(waitingUpload));
            String line = reader.readLine();
            while (line != null) { // while not end of file
                stringBuilder.append(line);
                line = reader.readLine();
                if (line != null) {
                    stringBuilder.append("\n"); // do not end the last line with '\n'
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading a waiting upload!");
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return stringBuilder.toString();
    }

    // Categories

    /**
     * Writes categoryData to the categories file
     * @param categoryData information about categories that should be saved
     */
    public void saveLocalizedCategories(String categoryData) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(
                    new File(CATEGORIES_FILE)));
            writer.write(categoryData);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Reads the content of the categories file
     * @return the content of the categories file with one line per element in the ArrayList
     */
    public ArrayList<String> loadLocalizedCategories() {
        BufferedReader reader;
        ArrayList<String> playlistString = new ArrayList<>();
        try {
            if (!Files.exists(Paths.get(CATEGORIES_FILE))) {
                Files.createFile(Paths.get(CATEGORIES_FILE));
            } else {
                reader = new BufferedReader(new FileReader(
                        new File(CATEGORIES_FILE)));
                String line = reader.readLine();
                while (line != null) { // while not end of file
                    playlistString.add(line);
                    line = reader.readLine();
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Could not find categories file");
        } catch (IOException e) {
            System.err.println("Could not create categories file");
        }
        return playlistString;
    }
}
