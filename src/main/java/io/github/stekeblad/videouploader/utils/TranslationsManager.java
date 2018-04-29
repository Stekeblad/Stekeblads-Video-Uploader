package io.github.stekeblad.videouploader.utils;

import io.github.stekeblad.videouploader.main.mainWindowController;

import java.io.File;
import java.net.URL;
import java.util.HashMap;

public class TranslationsManager {
    private static HashMap<String, Translations> loadedTranslations;

    private TranslationsManager() {
    }

    public static Translations getTranslation(String translationName) throws RuntimeException {
        if (loadedTranslations == null) {
            loadedTranslations = new HashMap<>();
        }
        if (loadedTranslations.containsKey(translationName)) {
            return loadedTranslations.get(translationName);
        } else {
            System.err.println("[TranslationsManager] tried to get not loaded translation \"" + translationName + "\"");
            throw new RuntimeException("Translation " + translationName + " is not loaded");
        }
    }

    public static void loadTranslation(String translationName) throws Exception {
        if (loadedTranslations == null) {
            loadedTranslations = new HashMap<>();
        }
        if (loadedTranslations.containsKey(translationName)) {
            return;
        }
        Translations translation = new Translations(translationName);
        loadedTranslations.put(translationName, translation);
    }

    public static void loadAllTranslations() throws Exception {
        URL url = mainWindowController.class.getClassLoader().getResource("strings/");
        if (url != null) {
            File stringsDir = new File(url.toURI());
            String[] translationDirs = stringsDir.list();
            if (translationDirs != null) {
                for (String item : translationDirs) {
                    loadTranslation(item);
                }
            } else {
                throw new Exception("There is no translations in the translations directory");
            }
        } else {
            throw new Exception("The translations directory does not exist");
        }
    }
}
