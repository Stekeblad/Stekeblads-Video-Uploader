package io.github.stekeblad.videouploader.youtube.utils;

import com.google.api.services.youtube.model.VideoCategory;
import io.github.stekeblad.videouploader.utils.AlertUtils;
import io.github.stekeblad.videouploader.utils.ConfigManager;
import io.github.stekeblad.videouploader.utils.TimeUtils;
import io.github.stekeblad.videouploader.utils.translation.TranslationBundles;
import io.github.stekeblad.videouploader.utils.translation.Translations;
import io.github.stekeblad.videouploader.utils.translation.TranslationsManager;
import io.github.stekeblad.videouploader.youtube.YouTubeApiLayer;
import io.github.stekeblad.videouploader.youtube.exceptions.*;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A Enum-Singleton class that handles categories. Initialize ConfigManager with the configManager() method before using
 * this class
 */
public enum CategoryUtils {
    INSTANCE;

    private final ConfigManager configManager = ConfigManager.INSTANCE;
    private final Translations transBasic = TranslationsManager.getTranslation(TranslationBundles.BASE);
    private HashMap<String, String> categories = null;

    /**
     * Gets Categories from Youtube. Does not check if permission has been given or not. If you want to display a warning
     * to the user that they will be sent to youtube for granting permission or similar, do it before calling this method
     */
    public boolean downloadCategories(String lang, String region) {

        // quick check of stored settings
        if (lang.length() != 2 || region.length() != 2) {
            System.err.println("Invalid length of lang or region \nlang: " + lang + "\nregion: " + region);
            return false;
        }

        List<VideoCategory> receivedCategories = null;
        try {
            // Perform the request
            receivedCategories = YouTubeApiLayer.requestVideoCategories(region, lang);
            // Handle different types of errors
        } catch (InvalidRegionCodeException regionException) {
            Platform.runLater(() ->
                    AlertUtils.simpleClose("Bad region code - " + transBasic.getString("app_name"),
                            regionException.getReason()).show()
            );
        } catch (InvalidLanguageException languageException) {
            Platform.runLater(() ->
                    AlertUtils.simpleClose("Bad language code - " + transBasic.getString("app_name"),
                            languageException.getReason()).show()
            );
        } catch (QuotaLimitExceededException quotaException) {
            String userClockAtPacificMidnight = TimeUtils.fromMidnightPacificToUserTimeZone();
            Platform.runLater(() ->
                    AlertUtils.simpleClose(transBasic.getString("app_name"), "Get categories failed because " +
                            transBasic.getString("app_name") + " has reached its daily limit in the YouTube API. The limit " +
                            "will be reset at midnight pacific time (" + userClockAtPacificMidnight + " in your timezone.)" +
                            " Please retry after when.").show()
            );
        } catch (OtherYouTubeException otherException) {
            Platform.runLater(() ->
                    AlertUtils.exceptionDialog(transBasic.getString("app_name"),
                            "An error was returned from YouTube: ",
                            otherException)
            );
        } catch (YouTubeException e) {
            Platform.runLater(() ->
                    AlertUtils.unhandledExceptionDialog(e)
            );
        }

        if (receivedCategories == null)
            return false;

        // Update categories
        categories = new HashMap<>();
        for (VideoCategory cat : receivedCategories) {
            if (cat.getSnippet().getAssignable()) { // Check if category is allowed to be used
                categories.put(cat.getSnippet().getTitle(), cat.getId());
            }
        }

        // Set default category if no results was returned
        if (categories.isEmpty()) {
            categories.put("Categories not localized", "-1");
        }
        saveCategories();
        return true;
    }

    /**
     *
     * @return a list of all category names that is allowed to be used
     */
    public ArrayList<String> getCategoryNames() {
        if (categories == null) {
            loadCategories();
        }
        return new ArrayList<>(categories.keySet());
    }

    /**
     * Returns the category Id that belongs to the category categoryName
     * @param categoryName the category that you want the Id for
     * @return the Id of categoryName as a String or "-1" if category does not exist
     */
    public String getCategoryId(String categoryName) {
        if (categories == null) {
            loadCategories();
        }
        return categories.getOrDefault(categoryName, "-1");
    }

    /**
     * Saves the categories to disc
     */
    private void saveCategories() {
        StringBuilder saveString = new StringBuilder();
        categories.forEach((k, v) -> saveString.append(v).append(":").append(k).append("\n"));
        saveString.deleteCharAt(saveString.length() - 1);
        configManager.saveLocalizedCategories(saveString.toString());
    }

    /**
     * Loads saved categories from disc, if no saved categories was found then a default no categories category is added.
     */
    public void loadCategories() {
        categories = new HashMap<>();
        ArrayList<String> data = configManager.loadLocalizedCategories();
        for (String category : data) {
            String id = category.substring(0, category.indexOf(':'));
            String name = category.substring(category.indexOf(':') + 1);
            categories.put(name, id);
        }
        if(categories.isEmpty()) {
            categories.put("Categories not localized", "-1");
        }
    }
}
