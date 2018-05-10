package io.github.stekeblad.videouploader.youtube.utils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.VideoCategory;
import com.google.api.services.youtube.model.VideoCategoryListResponse;
import io.github.stekeblad.videouploader.utils.ConfigManager;
import io.github.stekeblad.videouploader.youtube.Auth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A Enum-Singleton class that handles categories. Initialize ConfigManager with the configManager() method before using
 * this class
 */
public enum CategoryUtils {
    INSTANCE;

    private ConfigManager configManager = ConfigManager.INSTANCE;

    private HashMap<String, String> categories = null;

    /**
     * Gets Categories from Youtube. Does not check if permission has been given or not. If you want to display a warning
     * to the user that they will be sent to youtube for granting permission or similar, do it before calling this method
     */
    public void downloadCategories() {
        // Get stored language and country settings
        String lang = configManager.getCategoryLanguage();
        String region = configManager.getCategoryCountry();

        // quick check of stored settings
        if(lang.length() != 2 || region.length() != 2) {
            System.err.println("Invalid length of lang or region \nlang: " + lang + "\nregion: " + region);
            return;
        }

        try {
            // Authenticate user and create Youtube object
            Credential creds = Auth.authUser();
            YouTube youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, creds).setApplicationName(
                    "Stekeblads Video Uploader").build();

            // Prepare and send request
            YouTube.VideoCategories.List videoCategoriesListForRegionRequest = youtube.videoCategories().list("snippet");
            videoCategoriesListForRegionRequest.setHl(lang);
            videoCategoriesListForRegionRequest.setRegionCode(region);
            VideoCategoryListResponse response = videoCategoriesListForRegionRequest.execute();

            // Process result
            List<VideoCategory> vidCat = response.getItems();
            categories = new HashMap<>();
            for(VideoCategory cat : vidCat) {
                if(cat.getSnippet().getAssignable()) { // Check if category is allowed to be used
                    categories.put(cat.getSnippet().getTitle(), cat.getId());
                }
            }
            // Set default category if no results was returned
            if (categories.isEmpty()) {
                categories.put("Categories not localized", "-1");
            }
            saveCategories();

        } catch (IOException e) { // invalid country or language
            categories = new HashMap<>();
            categories.put("Categories not localized", "-1");
        }
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
