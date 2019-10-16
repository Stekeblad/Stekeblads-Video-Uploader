package io.github.stekeblad.videouploader.youtube.utils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistListResponse;
import com.google.api.services.youtube.model.PlaylistSnippet;
import com.google.api.services.youtube.model.PlaylistStatus;
import io.github.stekeblad.videouploader.utils.ConfigManager;
import io.github.stekeblad.videouploader.utils.translation.TranslationBundles;
import io.github.stekeblad.videouploader.utils.translation.TranslationsManager;
import io.github.stekeblad.videouploader.youtube.Auth;
import io.github.stekeblad.videouploader.youtube.LocalPlaylist;

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

    private final ConfigManager configManager = ConfigManager.INSTANCE;
    private String noPlaylistName = "";
    private HashMap<String, LocalPlaylist> playlistCache = null;

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

            // Backup old data
            HashMap<String, LocalPlaylist> oldCache = playlistCache;
            playlistCache = new HashMap<>();

            // Get playlists
            PlaylistListResponse response;
            do {
                response = userPlaylists.execute();
                List<Playlist> playlists = response.getItems();
                for (Playlist aPlaylist : playlists) {
                    String title = aPlaylist.getSnippet().getTitle();
                    LocalPlaylist newPlaylist;
                    if (oldCache != null && oldCache.get(title) != null) {
                        newPlaylist = new LocalPlaylist(oldCache.get(title).isVisible(), aPlaylist.getId(), title);
                    } else {
                        newPlaylist = new LocalPlaylist(true, aPlaylist.getId(), title);
                    }
                    playlistCache.put(title, newPlaylist);
                }
                if(response.getNextPageToken() != null) {
                    userPlaylists.setPageToken(response.getNextPageToken());
                }
            }while(response.getNextPageToken() != null);
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
     * @return a list of all playlists (name, id, visible)
     */
    public ArrayList<LocalPlaylist> getAllPlaylists() {
        if (playlistCache == null) {
            return null;
        }
        return new ArrayList<>(playlistCache.values());
    }

    /**
     *
     * @return a list with the names of all playlists
     */
    public ArrayList<String> getPlaylistNames() {
        if(playlistCache == null) {
            getUserPlaylists();
        }
        return new ArrayList<>(playlistCache.keySet());
    }

    /**
     * This method returns all playlists that is set to be visible plus a "No playlist selected" item at index 0
     * @return a ArrayList with playlist names
     */
    public ArrayList<String> getVisiblePlaylistNames() {
        ArrayList<String> visiblePlaylists = new ArrayList<>();
        if (noPlaylistName.equals("")) {
            noPlaylistName = TranslationsManager.getTranslation(TranslationBundles.BASE).getString("noSelected");
        }
        visiblePlaylists.add(noPlaylistName);
        playlistCache.forEach((k, v) -> {
            if (v.isVisible()) {
                visiblePlaylists.add(k);
            }
        });
        return visiblePlaylists;
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
        if (playlistCache.get(playlistName) == null) {
            return null;
        }
        return playlistCache.get(playlistName).getId();
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
     * Creates a new playlist on Youtube and adds it to the local list
     * @param name the name of the new playlist
     * @return a localPlaylist with the new playlist or null if the creation of a new playlist failed
     */
    public LocalPlaylist addPlaylist(String name, String privacy) {
        try {
            // Authenticate user and create Youtube object
            Credential creds = Auth.authUser();
            YouTube youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, creds).setApplicationName(
                    "Stekeblads Youtube Uploader").build();

            // prepare playlist
            PlaylistSnippet snippet = new PlaylistSnippet();
            snippet.setTitle(name);
            PlaylistStatus status = new PlaylistStatus();
            status.setPrivacyStatus(privacy);

            Playlist unsyncedPlaylist = new Playlist();
            unsyncedPlaylist.setSnippet(snippet);
            unsyncedPlaylist.setStatus(status);

            YouTube.Playlists.Insert playlistInserter = youtube.playlists().insert("snippet,status", unsyncedPlaylist);
            Playlist syncedPlaylist = playlistInserter.execute();

            LocalPlaylist localPlaylist = new LocalPlaylist(
                    true, syncedPlaylist.getId(), syncedPlaylist.getSnippet().getTitle());
            playlistCache.put(syncedPlaylist.getSnippet().getTitle(), localPlaylist);
            return localPlaylist;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Sets the visible variable for the playlist with the name playlistName
     * @param playlistName the name of the playlist to change the visible status on
     * @param visibleStatus the new value of the visible variable
     */
    public void setVisible(String playlistName, boolean visibleStatus) {
        LocalPlaylist lp = playlistCache.get(playlistName);
        if (lp == null) {
            return;
        }
        lp.setVisible(visibleStatus);
        playlistCache.put(playlistName, lp);
    }

    /**
     * Saves the playlists to disc
     */
    public void saveCache() {
        StringBuilder saveString = new StringBuilder();
        playlistCache.forEach((k, v) -> saveString.append(Boolean.toString(v.isVisible())).append(":")
                .append(v.getId()).append(":").append(v.getName()).append("\n"));
        saveString.deleteCharAt(saveString.length() - 1);
        configManager.savePlaylistCache(saveString.toString());
    }

    /**
     * Loads playlists from disc
     */
    public void loadCache() {
        playlistCache = new HashMap<>();
        // Add default "no playlist" item
        ArrayList<String> loadedPlaylists = configManager.loadPlaylistCache();
        // If no playlists could be loaded
        if(loadedPlaylists == null || loadedPlaylists.size() == 0) {
            return;
        }
        // Check version of playlist file to determinate how to read it
        if(loadedPlaylists.get(0).startsWith("true:") || loadedPlaylists.get(0).startsWith("false:")) {
            // Newer version, from release 1.1
            for (String loadedPlaylist : loadedPlaylists) {
                // for each row (one stored playlist)
                // Locate first colon that separates if the playlist should be visible from playlists ChoiceBox or not
                int colonIndex = loadedPlaylist.indexOf(':');
                // Read if it should be visible or not
                boolean visible = Boolean.valueOf(loadedPlaylist.substring(0, colonIndex));
                // throw away used data and locate next colon that separates playlist Id and name
                loadedPlaylist = loadedPlaylist.substring(colonIndex + 1);
                colonIndex = loadedPlaylist.indexOf(':');
                // Read id and name
                String id = loadedPlaylist.substring(0, colonIndex);
                String name = loadedPlaylist.substring(colonIndex + 1);
                // save
                playlistCache.put(name, new LocalPlaylist(visible, id, name));
            }
        } else {
            // Older version, for release 1.0
            for (String loadedPlaylist : loadedPlaylists) {
                String id = loadedPlaylist.substring(0, loadedPlaylist.indexOf(':'));
                String name = loadedPlaylist.substring(loadedPlaylist.indexOf(':') + 1);
                playlistCache.put(name, new LocalPlaylist(true, id, name));
            }
        }

    }
}
