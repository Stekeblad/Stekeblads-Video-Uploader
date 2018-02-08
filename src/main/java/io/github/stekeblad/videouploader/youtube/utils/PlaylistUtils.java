package io.github.stekeblad.videouploader.youtube.utils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistListResponse;
import io.github.stekeblad.videouploader.utils.ConfigManager;
import io.github.stekeblad.videouploader.youtube.Auth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Enum-Singleton class for handling playlists. Initialize ConfigManager with the configManager() method before using
 * this class
 */
public enum PlaylistUtils {
    INSTANCE;

    private ConfigManager configManager = ConfigManager.INSTANCE;

    private HashMap<String, String> playlistCache = null;

    /**
     * Gets playlists from Youtube. Does not check if permission has been given or not. If you want to display a warning
     * to the user that they will be sent to youtube for granting permission or similar, do it before calling this method
     */
    public void refreshPlaylist() {
        try {
            // Authenticate user and create Youtube object
            Credential creds = Auth.authUser();
            YouTube youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, creds).setApplicationName(
                    "Stekeblads Youtube Uploader").build();

            // Prepare request
            YouTube.Playlists.List userPlaylists = youtube.playlists().list("snippet,contentDetails");
            userPlaylists.setMine(true);
            userPlaylists.setMaxResults(25L);

            // Get response
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

    /**
     * First attempts to load local playlists from disc, if no playlists is found then attempt to get playlists from youtube
     */
    private void getUserPlaylists() {
        loadCache();
        if (playlistCache.isEmpty()) {
            refreshPlaylist();
        }
    }

    /**
     *
     * @return a list with the names of all playlists
     */
    public ArrayList<String> getUserPlaylistNames() {
        if(playlistCache == null) {
            getUserPlaylists();
        }
        return new ArrayList<>(playlistCache.keySet());
    }

    /**
     * Returns the playlist id for the playlist named playlistName
     * @param playlistName the name of the playlist to get the id for
     * @return the id of the playlist playlistName or null if their is no playlist named playlistName
     */
    public String getPlaylistId(String playlistName) {
        if (playlistName == null) {
            return null;
        }
        return playlistCache.getOrDefault(playlistName, null);
    }

    /**
     * Returns a Youtube playlist URL that links to the playlist named playlistName
     * @param playlistName the name of the playlist to get the URL to
     * @return a Youtube playlist URL that points to playlistName or null if their is no playlist named playlistName
     */
    public String getPlaylistUrl(String playlistName) {
        String id = getPlaylistId(playlistName);
        if(id != null) {
            return "https://www.youtube.com/playlist?list=" + id;
        } else {
            return null;
        }
    }

    /**
     * Saves the playlists to disc
     */
    private void saveCache() {
        StringBuilder saveString = new StringBuilder();
        playlistCache.forEach((k, v) -> saveString.append(v).append(":").append(k).append("\n"));
        saveString.deleteCharAt(saveString.length() - 1);
        configManager.savePlaylistCache(saveString.toString());
    }

    /**
     * Loads playlists from disc
     */
    public void loadCache() {
        playlistCache = new HashMap<>();
        ArrayList<String> loadedPlaylists = configManager.loadPlaylistCache();
        for (String loadedPlaylist : loadedPlaylists) {
            String id = loadedPlaylist.substring(0, loadedPlaylist.indexOf(':'));
            String name = loadedPlaylist.substring(loadedPlaylist.indexOf(':') + 1);
            playlistCache.put(name, id);
        }
    }
}
