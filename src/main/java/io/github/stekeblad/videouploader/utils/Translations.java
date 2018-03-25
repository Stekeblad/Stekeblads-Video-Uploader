package io.github.stekeblad.videouploader.utils;

import io.github.stekeblad.videouploader.main.mainWindowController;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Tooltip;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

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
     * bundleName.properties and bundleName[userLocale].properties there [userLocale] is the locale in a format like _en_US.
     * If there is not a directly matching locale an attempt to search for a partially matching one is performed,
     * say a translation for en_US is not found it may be one for en_GB to use instead
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
                try {
                    URL url2 = mainWindowController.class.getClassLoader().getResource("strings/" + bundleName);
                    if (url2 != null) {
                        File dir = new File(url2.toURI());
                        File[] files = dir.listFiles();
                        if (files != null) {
                            for (File aFile : files) {
                                String matchString = bundleName + "_" + locale.toString().substring(0, 2);
                                if (aFile.getName().contains(matchString)) {
                                    FileInputStream fis = new FileInputStream(new File(aFile.toURI()));
                                    localized = new PropertyResourceBundle(fis);
                                    String[] localeStrings = aFile.getName().split("_");
                                    String localeLang = localeStrings[1];
                                    String localeCountry = localeStrings[2].substring(0, localeStrings[2].indexOf("."));
                                    locale = new Locale(localeLang, localeCountry);
                                    break;
                                }
                            }
                        }
                    }
                    if (localized == null) {
                        System.out.println("Could not find a matching or partially matching translation for " + bundleName + " and " + locale);
                    }

                } catch (NullPointerException e1) {
                    System.err.println("exception occurred while looking for similar translations");
                    System.err.println(bundleName + " : " + locale);
                    e1.printStackTrace();
                }
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            localized = null;
            // Try find a similar translation, like en_GB instead of en_US

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

    /**
     * Returns the translation for the key in the user's locale if possible, if no translation was found the default
     * english translation is returned
     *
     * @param key the translation key
     * @return the translation of the translation key
     */
    public String getString(String key) {
        if (localized != null) {
            try {
                return localized.getString(key);
            } catch (Exception e) {
                System.err.println(String.format("Missing translation for key \"%s\" in locale \"%s\" of translation " +
                        "family \"%s\"", key, locale, translationFamily));
            }
        }
        return fallback.getString(key); // if typo in key or translation is missing for default language I WANT AN EXCEPTION!
    }

    /**
     * Automatically tries to translate all Nodes that are children or any level of grandchildren of window.
     * This is done by first generating a list of children of all levels and then iterating over all translation key and
     * comparing them to the node ids. On top of that, if a translation key
     * ends with _tt it looks for a Node with that name (excluding the _tt) and if found sets the translation as the Node
     * Tooltip. If a translation key ends with _pt it searches for a Node with that name (excluding _pt) and if found
     * sets the translation as the Node PromptText.
     *
     * @param window A parent with children to translate (like an entire window pane)
     */
    public void autoTranslate(Parent window) {
        ArrayList<Node> children = childScanner(window);
        HashMap<String, Node> hashMap = new HashMap<>();
        for (Node child : children) {
            if (child.getId() != null) {
                hashMap.put(child.getId(), child);
            }
        }

        for (String key : fallback.keySet()) {
            if (key.endsWith("_tt")) {
                // All nodes that extends Labeled can have a Tooltip,
                // But textFields do not and they can also have a Tooltip.
                // Anything can have a tooltip this way.
                Node aNode = hashMap.get(key.substring(0, key.length() - 3));
                if (aNode != null) {
                    Tooltip tt = new Tooltip(getString(key));
                    Tooltip.install(aNode, tt);
                }
            } else if (key.endsWith("_pt")) { // Test if it is a promptText translation
                // All nodes that extend TextInputControl can have a prompt text
                Node aNode = hashMap.get(key.substring(0, key.length() - 3));
                if (aNode != null && aNode instanceof TextInputControl) {
                    ((TextInputControl) aNode).setPromptText(getString(key));
                }
                // Else it is a text translation
            } else {
                // All nodes that can have text extends Labeled
                Node aNode = hashMap.get(key);
                if (aNode != null && aNode instanceof Labeled) {
                    ((Labeled) aNode).setText(getString(key));
                }
            }
        }
    }

    /**
     * Builds a list of all children of parent and if any child also is a parent there children are checked and added by recursion.
     *
     * @param parent a Node to start listing children from
     * @return An ArrayList&lt Node&gt with all children and any level of grand children of parent
     */
    private ArrayList<Node> childScanner(Parent parent) {
        ArrayList<Node> children = new ArrayList<>();
        for (Node aNode : parent.getChildrenUnmodifiable()) {
            if (aNode instanceof Parent) {
                children.addAll(childScanner((Parent) aNode));
            }
            children.add(aNode);
        }
        return children;
    }
}
