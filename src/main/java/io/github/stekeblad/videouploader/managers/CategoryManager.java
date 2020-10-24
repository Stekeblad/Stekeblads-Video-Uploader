package io.github.stekeblad.videouploader.managers;

import com.google.api.services.youtube.model.VideoCategory;
import com.google.gson.JsonObject;
import io.github.stekeblad.videouploader.managers.categoryMigrators.CategoryMigrator;
import io.github.stekeblad.videouploader.utils.AlertUtils;
import io.github.stekeblad.videouploader.utils.Constants;
import io.github.stekeblad.videouploader.utils.TimeUtils;
import io.github.stekeblad.videouploader.utils.translation.TranslationBundles;
import io.github.stekeblad.videouploader.utils.translation.Translations;
import io.github.stekeblad.videouploader.utils.translation.TranslationsManager;
import io.github.stekeblad.videouploader.youtube.LocalCategory;
import io.github.stekeblad.videouploader.youtube.YouTubeApiLayer;
import io.github.stekeblad.videouploader.youtube.exceptions.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.github.stekeblad.videouploader.utils.Constants.*;

/**
 * Handles loading, saving and retrieving video categories
 */
public class CategoryManager extends ManagerBase {

    private static CategoryManager _manager;

    /**
     * Returns the shared CategoryManager, creating it if this is the first time this
     * method gets called since the program started.
     */
    public static CategoryManager getCategoryManger() {
        if (_manager == null)
            _manager = new CategoryManager();

        return _manager;
    }

    private final Path categoriesPath;
    private final ObservableList<LocalCategory> categories;
    private final Translations transBasic = TranslationsManager.getTranslation(TranslationBundles.BASE);

    private CategoryManager() {
        categoriesPath = Paths.get(CATEGORIES_FILE).toAbsolutePath();
        CategoryMigrator categoryMigrator = new CategoryMigrator();

        // If no file in the json format exists
        if (!Files.exists(categoriesPath)) {
            final Path oldCategoryFilePath = Paths.get(DATA_DIR + "/categories");
            if (Files.exists(oldCategoryFilePath)) {
                // Found data file in the oldest format. Back it up, migrate and delete original
                try {
                    Files.copy(oldCategoryFilePath, Paths.get(CONFIG_BACKUP_DIR + "/categories"));
                    List<String> categoryLines = Files.readAllLines(oldCategoryFilePath);
                    config = categoryMigrator.migrate(categoryLines);
                    Files.delete(oldCategoryFilePath);
                } catch (IOException ignored) {
                }
            } else {
                // No data found
                config = new JsonObject();
                set(Constants.VERSION_FORMAT_KEY, CategoryMigrator.latestFormatVersion);
                set("category_country", "");
                set("category_language", "");
                set("categories", new ArrayList<LocalCategory>());
            }
        } else {
            // Data in json format found
            try {
                loadConfigFromFile(categoriesPath);
                if (!categoryMigrator.isLatestVersion(config)) {
                    // File is in a older format, create a backup of it and then upgrade to latest format
                    final String backupFileName = "/categories-" + TimeUtils.currentTimeString() + ".json";
                    Files.copy(categoriesPath, Paths.get(CONFIG_BACKUP_DIR + backupFileName));
                    categoryMigrator.migrate(config);
                    writeConfigToFile(categoriesPath);
                }
            } catch (IOException e) {
                AlertUtils.exceptionDialog("Failed to load or update categories file",
                        "The categories file could not be read. If the program have updated since last run something" +
                                " could have failed while updating the categories file to a newer version",
                        e);
            }
        }

        ArrayList<LocalCategory> tempList = getArrayList("categories", LocalCategory.class);
        tempList.sort(null);
        categories = FXCollections.observableList(tempList);
    }

    /**
     * Saves all category data to the categories save file
     *
     * @throws IOException if exception occurred when writing to the save file
     */
    public void saveCategories() throws IOException {
        set("categories", categories);
        writeConfigToFile(categoriesPath);
    }

    /**
     * @return an observable list of categories
     */
    public ObservableList<LocalCategory> getCategories() {
        return categories;
    }

    /**
     * Looks for the first LocalCategory that have a categoryName exactly matching the parameter categoryName
     *
     * @param categoryName The name of the category to find
     * @return An Optional&lt;LocalCategory&gt; that either contains the first matching LocalCategory or is empty if
     * no LocalCategory matches
     * @apiNote You should work with Ids instead of names then possible
     */
    public Optional<LocalCategory> findByName(String categoryName) {
        return categories.stream().filter(lc -> lc.getName().equals(categoryName)).findFirst();
    }

    /**
     * Looks for the first LocalCategory that have a categoryId exactly matching the parameter categoryId
     *
     * @param categoryId The id of the category to find
     * @return An Optional&lt;LocalCategory&gt; that either contains the first matching LocalCategory or is empty if
     * no LocalCategory matches
     */
    public Optional<LocalCategory> findById(String categoryId) {
        return categories.stream().filter(lc -> lc.getId().equals(categoryId)).findFirst();
    }

    /**
     * Downloads available categories from YouTube, overwriting any existing category data.
     * Observers on the categories list will (hopefully) automatically update.
     *
     * @param country  the two-character country code for the country available categories should be retrieved for
     * @param language the two-character language code for the language the category names should be localized for
     */
    public void downloadCategories(String country, String language) {
        List<VideoCategory> receivedCategories = null;
        try {
            // Perform the request
            receivedCategories = YouTubeApiLayer.requestVideoCategories(country, language);
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
            return;

        List<LocalCategory> receivedTransformedCategories = receivedCategories.stream()
                .map((category) -> new LocalCategory(
                        category.getId(),
                        category.getSnippet().getTitle()))
                .sorted()
                .collect(Collectors.toList());

        // Do not overwrite categories with receivedTransformedCategories, categories is observable and
        // it will probably make all observers lose track of it.
        categories.clear();
        categories.addAll(receivedTransformedCategories);
    }

    public String getCategoryCountry() {
        return getString("category_country");
    }

    private void setCategoryCountry(String twoCharCode) {
        set("category_country", twoCharCode.toUpperCase());
    }

    public String getCategoryLanguage() {
        return getString("category_language");
    }

    private void setCategoryLanguage(String twoCharCode) {
        set("category_language", twoCharCode.toLowerCase());
    }
}
