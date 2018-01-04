package io.github.stekeblad.youtubeuploader.youtube;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistListResponse;
import io.github.stekeblad.youtubeuploader.utils.ConfigManager;
import javafx.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public enum PlaylistUtils {
    INSTANCE;

    private ConfigManager configManager = ConfigManager.INSTANCE;

    private ArrayList<Pair<String, String>> playlistCache = null;

    public ArrayList<Pair<String, String>> getUserPlaylists() throws IOException {
        if (playlistCache == null) {
            loadCache();
        }
        if (playlistCache == null) {
            Credential creds = Auth.authUser();
            YouTube youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, creds).setApplicationName(
                    "Stekeblads Youtube Uploader").build();

            YouTube.Playlists.List userPlaylists = youtube.playlists().list("snippet,contentDetails");
            userPlaylists.setMine(true);
            userPlaylists.setMaxResults(25L);

            PlaylistListResponse response = userPlaylists.execute();
            List<Playlist> playlists = response.getItems();

            playlistCache = new ArrayList<>();
            for(Playlist aPlaylist : playlists) {
                playlistCache.add(new Pair<>(aPlaylist.getId(), aPlaylist.getSnippet().getTitle()));
            }
            saveCache();
        }
        return playlistCache;
    }

    private void saveCache() {
        StringBuilder saveString = new StringBuilder();
        for(int i = 0; i < playlistCache.size(); i++) {
            saveString.append(playlistCache.get(i).getKey()).append(":").append(playlistCache.get(i).getValue());
            if (i + 1 < playlistCache.size()) {
                saveString.append("\n");
            }
        }
        configManager.savePlaylistCache(saveString.toString());
    }

    private void loadCache() {
        ArrayList<String> loadedPlaylists = configManager.loadPlaylistCache();

        for(int i = 0; i < loadedPlaylists.size(); i++) {
            String id = loadedPlaylists.get(i).substring(0, loadedPlaylists.get(i).indexOf(':'));
            String name = loadedPlaylists.get(i).substring(loadedPlaylists.get(i).indexOf(':') + 1);
            playlistCache.add(new Pair<>(id, name));
        }
    }

    public void printAll() {
        for (Pair<String, String> pl : playlistCache) {
            System.out.println(pl.getKey() + "\t" + pl.getValue());
        }
    }
}
