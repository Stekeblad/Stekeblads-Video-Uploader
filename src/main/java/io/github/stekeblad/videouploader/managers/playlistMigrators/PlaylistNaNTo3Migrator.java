package io.github.stekeblad.videouploader.managers.playlistMigrators;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.stekeblad.videouploader.extensions.gson.MyGsonFactory;
import io.github.stekeblad.videouploader.utils.Constants;
import io.github.stekeblad.videouploader.youtube.LocalPlaylist;

import java.util.List;
import java.util.stream.Collectors;

class PlaylistNaNTo3Migrator {
    JsonObject migrate(List<String> oldFormat) {
        JsonObject newJson = new JsonObject();
        Gson gson = MyGsonFactory.CreateGsonInstance();
        newJson.addProperty(Constants.VERSION_FORMAT_KEY, 3);

        List<LocalPlaylist> playlists = oldFormat.stream()
                .map((str) -> {
                    String[] splits = str.split(":");
                    return new LocalPlaylist(Boolean.parseBoolean(splits[0]), splits[1], splits[2]);
                })
                .collect(Collectors.toList());

        newJson.add("playlists", gson.toJsonTree(playlists));

        return newJson;
    }
}
