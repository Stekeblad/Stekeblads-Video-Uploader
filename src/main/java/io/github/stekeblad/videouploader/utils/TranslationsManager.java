package io.github.stekeblad.videouploader.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static io.github.stekeblad.videouploader.utils.Constants.DEFAULT_LOCALE;

/**
 * TranslationManager load and hold all translations so they do not need to be loaded more than once.
 */
public class TranslationsManager {
    private static HashMap<String, Translations> loadedTranslations;

    private TranslationsManager() {
    }

    /**
     * Returns an instance of {@code Translations} with the strings of the named group of translation strings,
     * if it is loaded. If this method is called to request a translation that is not loaded a Runtime Exception is thrown
     *
     * @param translationName name of the translations resource to get.
     * @return a Translations with the translation data
     * @throws RuntimeException if translation is not loaded
     */
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

    /**
     * Attempts to load the correct translation from the resource bundles with the name {@code translationName}
     * @param translationName name of the translation resource to load
     * @param locale the primary locale to load, uses DEFAULT_LOCALE as secondary/fallback, a value of null uses
     *               DEFAULT_LOCALE as primary locale
     * @throws Exception if translation could not be found
     */
    private static void loadTranslation(String translationName, Locale locale) throws Exception {
        if (loadedTranslations == null) {
            loadedTranslations = new HashMap<>();
        }
        if (loadedTranslations.containsKey(translationName)) {
            return;
        }
        Translations translation = new Translations(translationName, locale);
        loadedTranslations.put(translationName, translation);
    }

    /**
     * Attempts to load all translations from the resources/strings/ directory
     * @param locale the primary locale to load, uses DEFAULT_LOCALE as secondary/fallback
     * @throws Exception if no translations exist or the directory for translations does not exist
     */
    public static void loadAllTranslations(Locale locale) throws Exception {
        if (locale.toString().equals(DEFAULT_LOCALE)) {
            locale = null;
        }
        List<String> resources = FileUtils.getContentOfResourceDir("strings/");
        if (resources != null) {
            for (String resource : resources) {
                loadTranslation(resource, locale);
            }
        } else {
            throw new Exception("No Translations Found");
        }
    }
}
