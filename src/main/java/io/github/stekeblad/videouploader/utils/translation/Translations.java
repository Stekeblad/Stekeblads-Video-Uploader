package io.github.stekeblad.videouploader.utils.translation;

import io.github.stekeblad.videouploader.main.mainWindowController;
import io.github.stekeblad.videouploader.utils.FileUtils;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Tooltip;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
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
    private final String translationFamily;

    /**
     * Creates the translations object. It searches for translations files in the resources directory at strings/ for
     * bundleName.properties and bundleName[userLocale].properties there [userLocale] is the locale in a format like _en_US.
     * If there is not a directly matching locale an attempt to search for a partially matching one is performed,
     * say a translation for en_US is not found it may be one for en_GB to use instead
     *
     * @param bundleName name of the ResourceBundle file family
     * @param primaryLocale locale for primary language, DEFAULT_LOCALE will be used as fallback if any translation is missing
     *                      if primaryLocale is null, DEFAULT_LOCALE will be used as primary language
     * @throws Exception if the default translations file could not be found with the given bundleName
     */
    public Translations(String bundleName, Locale primaryLocale) throws Exception {
        String localizedPath = "";
        String fallbackPath = "strings/" + bundleName + "/" + bundleName + ".properties";
        translationFamily = bundleName;
        boolean existsLocalLocale = true;
        if (primaryLocale == null) {
            localizedPath = fallbackPath;
        } else {
            // For some reason does locale.toString() and locale.toLanguageTag() return all in lowercase... FIX
            String locStr = primaryLocale.toString();
            int separatorIndex = locStr.indexOf("_");
            try {
                locStr = locStr.substring(0, separatorIndex).toLowerCase() + "_" + locStr.substring(separatorIndex + 1).toUpperCase();
                locale = new Locale(locStr);
                localizedPath = "strings/" + bundleName + "/" + bundleName + "_" + locStr + ".properties";
            } catch (IndexOutOfBoundsException e) {
                // Could not properly detect locale or language set to default english
                existsLocalLocale = false;
                localized = null;
            }
        }

        // Load translation for user locale
        if (existsLocalLocale) {
            try {
                InputStream inputStream = mainWindowController.class.getClassLoader().getResourceAsStream(localizedPath);
                if (inputStream != null) {
                    localized = new PropertyResourceBundle(inputStream);
                } else {
                    // Did not find translation for user locale. Try find a similar translation, like en_GB instead of en_US
                    try {
                        List<String> availableTranslations = FileUtils.getContentOfResourceDir("strings/" + bundleName);
                        if (availableTranslations != null) {
                            String matchLocalePart = bundleName + "_" + locale.toString().substring(0, 2);
                            for (String aTranslation : availableTranslations) {
                                if (aTranslation.contains(matchLocalePart)) {
                                    InputStream inputStream2 = mainWindowController.class.getClassLoader()
                                            .getResourceAsStream("strings/" + bundleName + "/" + aTranslation);
                                    localized = new PropertyResourceBundle(inputStream2);
                                    String[] localeStrings = aTranslation.split("_");
                                    String localeLang = localeStrings[1];
                                    String localeCountry = localeStrings[2].substring(0, localeStrings[2].indexOf("."));
                                    locale = new Locale(localeLang, localeCountry);
                                    break;
                                }
                            }
                        }

                    } catch (NullPointerException e1) {
                        System.err.println("exception occurred while looking for similar translations");
                        System.err.println(bundleName + " : " + locale);
                        e1.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                localized = null;
            }
        }
        // Load default locale as backup if translation is missing or partially added
        try {
            InputStream inputStream = mainWindowController.class.getClassLoader().getResourceAsStream(fallbackPath);
            if (inputStream != null) {
                fallback = new PropertyResourceBundle(inputStream);
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
     * ends with _tt it looks for a Node with that id (excluding the _tt) and if found sets the translation as the Node
     * Tooltip. If a translation key ends with _pt it searches for a Node with that id (excluding _pt) and if found
     * sets the translation as the Node PromptText. The second parameter is for if all Nodes have a prefix that should
     * be ignored when looking for a matching translation key.
     *
     * @param window A parent with children to translate (like an entire window pane)
     * @param prefix the prefix of the Nodes that is not in the translation keys
     */
    public void autoTranslate(Parent window, String prefix) {
        HashMap<String, Node> children = childScanner(window);
        if (prefix == null) {
            prefix = "";
        }

        for (String key : fallback.keySet()) {
            if (key.endsWith("_tt")) {
                // All nodes that extends Labeled can have a Tooltip,
                // But textFields do not and they can also have a Tooltip.
                // Anything can have a tooltip this way.
                Node aNode = children.get(prefix + key.substring(0, key.length() - 3));
                if (aNode != null) {
                    Tooltip tt = new Tooltip(getString(key));
                    Tooltip.install(aNode, tt);
                }
            } else if (key.endsWith("_pt")) { // Test if it is a promptText translation
                // All nodes that extend TextInputControl can have a prompt text
                Node aNode = children.get(prefix + key.substring(0, key.length() - 3));
                if (aNode instanceof TextInputControl) {
                    ((TextInputControl) aNode).setPromptText(getString(key));
                }
                // Else it is a text translation
            } else {
                // All nodes that can have text extends Labeled
                Node aNode = children.get(prefix + key);
                if (aNode instanceof Labeled) {
                    ((Labeled) aNode).setText(getString(key));
                }
            }
        }
    }

    /**
     * Automatically tries to translate all Nodes that are children or any level of grandchildren of window.
     * This is done by first generating a list of children of all levels and then iterating over all translation key and
     * comparing them to the node ids. On top of that, if a translation key
     * ends with _tt it looks for a Node with that id (excluding the _tt) and if found sets the translation as the Node
     * Tooltip. If a translation key ends with _pt it searches for a Node with that id (excluding _pt) and if found
     * sets the translation as the Node PromptText.
     *
     * @param window A parent with children to translate (like an entire window pane)
     */
    public void autoTranslate(Parent window) {
        autoTranslate(window, "");
    }

    /**
     * Builds a list of all children of parent and if any child also is a parent there children are checked and added by recursion.
     *
     * @param parent a Node to start listing children from
     * @return An HashMap&lt String, Node&gt with all children and any level of grand children of parent
     */
    private HashMap<String, Node> childScanner(Parent parent) {
        HashMap<String, Node> children = new HashMap<>();
        for (Node aNode : parent.getChildrenUnmodifiable()) {
            if (aNode instanceof Parent) {
                children.putAll(childScanner((Parent) aNode));
            }
            children.put(aNode.getId(), aNode);
        }
        return children;
    }
}
