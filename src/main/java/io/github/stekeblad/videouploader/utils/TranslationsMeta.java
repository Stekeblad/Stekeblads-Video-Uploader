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
        if (resources != null) {
            for (String resource : resources) {
                InputStream inputStream = getClass().getClassLoader().getResourceAsStream("strings/meta/" + resource);
                String locale;
                if (resource.equals("meta.properties")) {
                    locale = "default (English)";
                } else {
                    locale = resource.substring(5, 10);  // substring("meta_".length(), resource.length()-".properties".length())
                }
                if (inputStream != null) {
                    try {
                        transMeta.put(locale, new PropertyResourceBundle(inputStream));
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
}
