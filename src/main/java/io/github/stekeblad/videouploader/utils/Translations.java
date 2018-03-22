package io.github.stekeblad.videouploader.utils;

import io.github.stekeblad.videouploader.main.mainWindowController;
import javafx.scene.Node;
import javafx.scene.control.Labeled;
import javafx.scene.control.Tooltip;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * A class for handling ResourceBundle translations. Works by loading the default translation and attempt to load a
 * translation for the users locale. When getString is called it attempt to get the string in the user's locale first
 * and if it fails it returns the default locale string.
 */
public class Translations {

    private ResourceBundle localized;
    private ResourceBundle fallback;
    private Locale locale;
    private String translationFamily;

    /**
     * Creates the translations object. It searches for translations files in the resources directory at strings/ for
     * bundleName.properties and bundleName[userLocale].properties there [userLocale] is the locale in a format like _en_US
     *
     * @param bundleName name of the ResourceBundle file family
     * @throws Exception if the default translations file could not be found with the given bundleName
     */
    public Translations(String bundleName) throws Exception {
        locale = Locale.getDefault();
        String localizedPath = "strings/" + bundleName + "/" + bundleName + "_" + locale.toString() + ".properties";
        String fallbackPath = "strings/" + bundleName + "/" + bundleName + ".properties";
        translationFamily = bundleName;

        // Load translation for user locale
        try {
            URL url = mainWindowController.class.getClassLoader().getResource(localizedPath);
            if (url != null) {
                FileInputStream fis = new FileInputStream(new File(url.toURI()));
                localized = new PropertyResourceBundle(fis);
            } else {
                System.err.println("Could not find translation for " + bundleName + " in locale " + locale);
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }

        // Load default locale as backup if translation is missing or partially added
        try {
            URL url = mainWindowController.class.getClassLoader().getResource(fallbackPath);
            if (url != null) {
                FileInputStream fis = new FileInputStream(new File(url.toURI()));
                fallback = new PropertyResourceBundle(fis);
            } else {
                throw new Exception("Could not find default translation file for " + bundleName);
            }
        } catch (IOException | URISyntaxException e) {
            throw new Exception("Could not find default translation file for " + bundleName);
        }
    }

    public String getString(String key) {
        try {
            return localized.getString(key);
        } catch (Exception e) {
            System.err.println("No translation for key \"" + key + "\" in locale \"" + locale +
                    "\" of translation family \"" + translationFamily + "\" found");
        }
        return fallback.getString(key); // if typo in key or translation is missing for default language I WANT AN EXCEPTION!
    }

    public void autoTranslate(Node window) {
        for (String key : fallback.keySet()) {
            // test if it is a tooltip translation
            if (key.endsWith("_tt")) {
                // All nodes that extends Labeled can have a Tooltip,
                // But textFields do not and they can also have a Tooltip.
                // Anything can have a tooltip this way.
                Node aNode = window.lookup("#" + key.substring(0, key.length() - 3));
                if (aNode != null) {
                    Tooltip tt = new Tooltip(getString(key));
                    Tooltip.install(aNode, tt);
                }
                // Else it is a text translation
            } else {
                // All nodes that can have text extends Labeled
                Node aNode = window.lookup("#" + key);
                if (aNode != null) {
                    ((Labeled) aNode).setText(getString(key));
                }
            }
        }
    }
}
