package io.github.stekeblad.videouploader.managers.presetMigrators;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.stekeblad.videouploader.extensions.gson.MyGsonFactory;
import io.github.stekeblad.videouploader.managers.CategoryManager;
import io.github.stekeblad.videouploader.managers.PlaylistManager;
import io.github.stekeblad.videouploader.models.NewVideoPresetModel;
import io.github.stekeblad.videouploader.utils.Constants;
import io.github.stekeblad.videouploader.youtube.LocalCategory;
import io.github.stekeblad.videouploader.youtube.LocalPlaylist;
import io.github.stekeblad.videouploader.youtube.VisibilityStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

class PresetNaNTo3Migration {

    private static final String NODE_ID_TITLE = "_title";
    private static final String NODE_ID_DESCRIPTION = "_description";
    private static final String NODE_ID_CATEGORY = "_category";
    private static final String NODE_ID_TAGS = "_tags";
    private static final String NODE_ID_PLAYLIST = "_playlist";
    private static final String NODE_ID_VISIBILITY = "_visibility";
    private static final String NODE_ID_TELL_SUBS = "_tellSubs";
    private static final String NODE_ID_THUMBNAIL = "_thumbNail";
    private static final String NODE_ID_MADE_FOR_KIDS = "_madeForKids";
    private static final String NODE_ID_PRESETNAME = "_presetName";

    JsonObject migrate(ArrayList<ArrayList<String>> oldPresets) {
        Gson gson = MyGsonFactory.CreateGsonInstance();
        PlaylistManager playlistManager = PlaylistManager.getPlaylistManager();
        CategoryManager categoryManager = CategoryManager.getCategoryManager();

        JsonObject newJson = new JsonObject();
        newJson.addProperty(Constants.VERSION_FORMAT_KEY, 3);
        ArrayList<NewVideoPresetModel> newPresetList = new ArrayList<>();

        for (ArrayList<String> preset : oldPresets) {
            NewVideoPresetModel newPreset = new NewVideoPresetModel();
            for (int i = 0; i < preset.size(); i++) {

                String line = preset.get(i);

                // Locate the separator between field name and value
                int colonIndex = line.indexOf(':');

                // Switch on field name
                switch (line.substring(0, colonIndex)) {
                    case NODE_ID_TITLE:
                        newPreset.setVideoName(line.substring(colonIndex + 1));
                        break;
                    case NODE_ID_DESCRIPTION:
                        // A bit special to allow descriptions to be multi-lined with actual enters in.
                        StringBuilder descBuilder = new StringBuilder();
                        descBuilder.append(line.substring(colonIndex + 1));
                        // Skips lines in the outer loop because they are not valid
                        i++;
                        // As long as the next line not starts with "_" treat it as a part of the description
                        while (!preset.get(i).startsWith("_")) {
                            descBuilder.append("\n").append(preset.get(i));
                            i++;
                        }
                        // Line started with _ ,go back one so the increment in the loop does not cause this line to be skipped
                        i--;
                        newPreset.setVideoDescription(descBuilder.toString());
                        break;
                    case NODE_ID_VISIBILITY:
                        newPreset.setVisibility(VisibilityStatus.valueOf(line.substring(colonIndex + 1)));
                        break;
                    case NODE_ID_TAGS:
                        line = line.substring(colonIndex + 2, line.length() - 1); // remove brackets
                        newPreset.setVideoTags(new ArrayList<>(Arrays.asList(line.split(","))));
                        break;
                    case NODE_ID_PLAYLIST:
                        Optional<LocalPlaylist> playlist = playlistManager.findByName(line.substring(colonIndex + 1));
                        playlist.ifPresent(newPreset::setSelectedPlaylist);
                        break;
                    case NODE_ID_CATEGORY:
                        Optional<LocalCategory> category = categoryManager.findByName(line.substring(colonIndex + 1));
                        category.ifPresent(newPreset::setSelectedCategory);
                        break;
                    case NODE_ID_TELL_SUBS:
                        newPreset.setTellSubs(Boolean.parseBoolean(line.substring(colonIndex + 1)));
                        break;
                    case NODE_ID_THUMBNAIL:
                        newPreset.setThumbnailPath(line.substring(colonIndex + 1));
                        break;
                    case NODE_ID_MADE_FOR_KIDS:
                        newPreset.setMadeForKids(Boolean.parseBoolean(line.substring(colonIndex + 1)));
                    case NODE_ID_PRESETNAME:
                        newPreset.setPresetName(line.substring(colonIndex + 1));
                        break;
                    default:
                        // Unknown value, skip it
                }
            }
            // All lines for this file processed
            newPresetList.add(newPreset);
        }
        // All files processed
        newJson.add("presets", gson.toJsonTree(newPresetList));
        return newJson;
    }
}
