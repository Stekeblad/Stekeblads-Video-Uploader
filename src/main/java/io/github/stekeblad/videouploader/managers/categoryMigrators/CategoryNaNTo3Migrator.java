package io.github.stekeblad.videouploader.managers.categoryMigrators;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.stekeblad.videouploader.extensions.gson.MyGsonFactory;
import io.github.stekeblad.videouploader.managers.SettingsManager;
import io.github.stekeblad.videouploader.utils.Constants;
import io.github.stekeblad.videouploader.youtube.LocalCategory;

import java.util.List;
import java.util.stream.Collectors;

class CategoryNaNTo3Migrator {
    JsonObject migrate(List<String> oldFormat) {
        JsonObject newJson = new JsonObject();
        Gson gson = MyGsonFactory.CreateGsonInstance();
        newJson.addProperty(Constants.VERSION_FORMAT_KEY, 3);

        // The language and country codes was earlier in ConfigManger but gets first migrated to SettingsManger
        // and now directly to CategoryManger
        SettingsManager settingsManager = SettingsManager.getSettingsManager();
        //noinspection deprecation
        newJson.addProperty("category_country", settingsManager.getCategoryCountry());
        //noinspection deprecation
        newJson.addProperty("category_language", settingsManager.getCategoryLanguage());

        List<LocalCategory> categories = oldFormat.stream()
                .map((str) -> {
                    String[] split = str.split(":");
                    return new LocalCategory(split[0], split[1]);
                })
                .collect(Collectors.toList());

        newJson.add("categories", gson.toJsonTree(categories));

        return newJson;
    }
}
