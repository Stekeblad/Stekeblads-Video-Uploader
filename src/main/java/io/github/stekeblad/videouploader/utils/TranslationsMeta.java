package io.github.stekeblad.videouploader.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * TranslationsMeta loads all meta about existing translations to make it easy to list all existing translations
 * and get the meta data about them
 */
public class TranslationsMeta {
    private HashMap<String, ResourceBundle> transMeta;

    /**
     * The constructor for TranslationsMeta, loads all files in strings/meta as PropertyResourceBundle
     */
    public TranslationsMeta() {
        transMeta = new HashMap<>();
        List<String> resources = FileUtils.getContentOfResourceDir("strings/meta");
        if (resources != null && !resources.isEmpty()) {
            for (String resource : resources) {
                InputStream inputStream = getClass().getClassLoader().getResourceAsStream("strings/meta/" + resource);
                if (inputStream != null) {
                    try {
                        PropertyResourceBundle bundle = new PropertyResourceBundle(inputStream);
                        transMeta.put(bundle.getString("translationName"), bundle);
                    } catch (IOException e) {
                        System.err.println("Could not load meta translation file: \"" + resource + "\"");
                    }
                } else {
                    System.err.println("InputStream for " + resource + " is null");
                }
            }
        } else {
            System.err.println("No meta files found!");
        }
    }

    /**
     * Returns the locale names of all translations found in strings/meta resource directory
     *
     * @return ArrayList with strings of locale names
     */
    public ArrayList<String> getAllTranslationLocales() {
        return new ArrayList<>(transMeta.keySet());
    }

    /**
     * Request a string from any of the loaded meta translation files
     *
     * @param language the locale you want the string in/for
     * @param meta     the string in the translation to get
     * @return the requested string or a empty string if the requested string does not exist, no loaded translation matches
     * the given locale or if any of the parameters is null
     */
    public String getMetaForLanguage(String language, String meta) {
        if (language == null || meta == null) {
            return "";
        }
        ResourceBundle langBundle = transMeta.get(language);
        if (langBundle == null) {
            return "";
        }
        try {
            return langBundle.getString(meta);
        } catch (MissingResourceException e) {
            return "";
        }
    }

    /**
     * returns the locale for the translation with the name languageName
     *
     * @param languageName the translation to get the locale for
     * @return the locale name or throws an exception if not found
     */
    public String langNameToLocaleCode(String languageName) {
        return transMeta.get(languageName).getString("locale");
    }

    /**
     * Tries to find the name of a existing translation from a locale code
     *
     * @param localeCode the locale code those translation name
     * @return the translation name or an empty string if not found
     */
    public String localeCodeToLangName(String localeCode) {
        Optional<Map.Entry<String, ResourceBundle>> optionalMatch = transMeta.entrySet().stream().filter(stringResourceBundleEntry ->
                stringResourceBundleEntry.getValue().getString("locale").equals(localeCode)).findFirst();
        if (optionalMatch.isPresent() && optionalMatch.get().getKey() != null && !optionalMatch.get().getKey().isEmpty()) {
            return optionalMatch.get().getKey();
        }
        return "";

    }
}
