package io.github.stekeblad.youtubeuploader.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Properties;

import static io.github.stekeblad.youtubeuploader.utils.Constants.DATA_DIR;
import static io.github.stekeblad.youtubeuploader.utils.Constants.PRESET_DIR;

// Using enum to make class singleton, something about that just sounds weird
public enum ConfigManager {
    INSTANCE;

    private Path filesPath;
    private Properties mainProp;

    public void configManager() {
        filesPath = Paths.get(DATA_DIR).toAbsolutePath();
        Path presetsPath = Paths.get(PRESET_DIR).toAbsolutePath();
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
        }

        loadSettings();
    }

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
            }
        }
    }

    public void saveSettings() {
        OutputStream output = null;
        try {
            output = new FileOutputStream(filesPath + "/settings.properties");
                    mainProp.store(output, "main settings file for Stekeblads Youtube Uploader");
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

    public boolean getNoSettings() {
        return "true".equals(mainProp.getProperty("noSettings"));
    }

    public void setNoSettings(boolean noSettings) {
        mainProp.setProperty("noSettings", noSettings ? "true" : "false");
    }

    public boolean getNeverAuthed() {
        return "true".equals(mainProp.getProperty("neverAuthed"));
    }

    public void setNeverAuthed(boolean neverAuthed) {
        mainProp.setProperty("neverAuthed", neverAuthed ? "true" : "false");
    }

    public void savePreset(String presetName, String stringRepresentation) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(
                    new File(PRESET_DIR + "/" + presetName)));
            writer.write(stringRepresentation);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public boolean deletePreset(String presetName) {
        Path path = Paths.get(PRESET_DIR + "/" + presetName);
        if (!Files.exists(path)) {
            return true; //does not exist, job already done
        }
        try {
            Files.delete(path);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public ArrayList<String> getSavedPresetNamesList() {
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

    public String loadPreset(String presetName) throws IOException {
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

    public void savePlaylistCache(String playlistsStringData) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(
                    new File("")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
