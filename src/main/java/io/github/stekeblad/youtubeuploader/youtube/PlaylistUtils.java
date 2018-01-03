package io.github.stekeblad.youtubeuploader.youtube;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistListResponse;
import com.google.api.services.youtube.model.PlaylistLocalization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public enum PlaylistUtils {
    INSTANCE;

    private List<Playlist> playlistCache;

    public ArrayList<String> getUserPlaylists() throws IOException {
        if (playlistCache == null) {
            Credential creds = Auth.authUser();
            YouTube youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, creds).setApplicationName(
                    "Stekeblads Youtube Uploader").build();

            YouTube.Playlists.List userPlaylists = youtube.playlists().list("snippet,contentDetails");
            userPlaylists.setMine(true);
            userPlaylists.setMaxResults(25L);

            PlaylistListResponse response = userPlaylists.execute();
            playlistCache = response.getItems();
        }

        ArrayList<String> returnList = new ArrayList<>();
        for(Playlist aPlaylist : playlistCache) {
            Map<String, PlaylistLocalization> localizations =aPlaylist.getLocalizations();
            if (localizations != null) {
                returnList.add("dummy playlist");
            }
        }
        //System.out.println(response);
        return returnList;
    }
}
