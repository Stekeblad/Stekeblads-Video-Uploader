package io.github.stekeblad.youtubeuploader.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static io.github.stekeblad.youtubeuploader.utils.Constants.DATA_DIR;

public class ConfigManager {

    private Path filesDir;
    private Properties mainProp;

    public ConfigManager() {
        filesDir = Paths.get(DATA_DIR).toAbsolutePath();
        mainProp = new Properties();

        if (!Files.exists(filesDir)) {
            try {
                Files.createDirectory(filesDir);
            } catch (IOException e) {
                System.err.println("Could not find or create directory for program files!");
                e.printStackTrace();
            }
        }

        loadSettings();
    }

    public void loadSettings() {
        InputStream input = null;
        try {
            input = new FileInputStream(filesDir + "/settings.properties");
            mainProp.load(input);
        } catch (FileNotFoundException e) {
            try {
                Files.createFile(Paths.get(filesDir + "/settings.properties"));
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
            //if 0 properties was loaded
            if ( mainProp.size() == 0) {
                mainProp.setProperty("noSettings", "true");
            }
        }
    }

    public void saveSettings() {
        OutputStream output = null;
        try {
            output = new FileOutputStream(filesDir + "/settings.properties");
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
}
