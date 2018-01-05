package io.github.stekeblad.youtubeuploader.youtube;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistListResponse;
import io.github.stekeblad.youtubeuploader.utils.ConfigManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public enum PlaylistUtils {
    INSTANCE;

    private ConfigManager configManager = ConfigManager.INSTANCE;

    private HashMap<String, String> playlistCache = null;

    public void refreshPlaylist() {
        try {
            Credential creds = Auth.authUser();
            YouTube youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, creds).setApplicationName(
                    "Stekeblads Youtube Uploader").build();

            YouTube.Playlists.List userPlaylists = youtube.playlists().list("snippet,contentDetails");
            userPlaylists.setMine(true);
            userPlaylists.setMaxResults(25L);

            PlaylistListResponse response = userPlaylists.execute();
            List<Playlist> playlists = response.getItems();

            playlistCache = new HashMap<>();
            for (Playlist aPlaylist : playlists) {
                playlistCache.put(aPlaylist.getSnippet().getTitle(), aPlaylist.getId());
            }
            saveCache();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getUserPlaylists() {
        loadCache();
        if (playlistCache.isEmpty()) {
            refreshPlaylist();
        }
    }

    public ArrayList<String> getUserPlaylistNames() {
        if(playlistCache == null) {
            getUserPlaylists();
        }
        return new ArrayList<>(playlistCache.keySet());
    }

    public String getPlaylistId(String playlistName) {
        return playlistCache.getOrDefault(playlistName, null);
    }

    public String getPlaylistUrl(String playlistName) {
        return "https://www.youtube.com/playlist?list=" + getPlaylistId(playlistName);
    }

    private void saveCache() {
        StringBuilder saveString = new StringBuilder();
        playlistCache.forEach((k, v) -> saveString.append(v).append(":").append(k).append("\n"));
        saveString.deleteCharAt(saveString.length() - 1);
        configManager.savePlaylistCache(saveString.toString());
    }

    private void loadCache() {
        playlistCache = new HashMap<>();
        ArrayList<String> loadedPlaylists = configManager.loadPlaylistCache();
        for (String loadedPlaylist : loadedPlaylists) {
            String id = loadedPlaylist.substring(0, loadedPlaylist.indexOf(':'));
            String name = loadedPlaylist.substring(loadedPlaylist.indexOf(':') + 1);
            playlistCache.put(name, id);
        }
    }
}
