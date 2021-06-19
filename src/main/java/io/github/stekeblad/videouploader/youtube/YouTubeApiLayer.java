package io.github.stekeblad.videouploader.youtube;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.*;
import io.github.stekeblad.videouploader.youtube.exceptions.YouTubeException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static io.github.stekeblad.videouploader.utils.Constants.AUTH_DIR;

/**
 * Class for the logic closest to the YouTube API
 */
public class YouTubeApiLayer {
    static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    static final JsonFactory JSON_FACTORY = new GsonFactory();

    /**
     * Create a new playlist for the user's channel
     *
     * @param name          Name for the new playlist
     * @param privacyStatus if it should be public or private
     * @return The new playlist created by YouTube
     * @throws YouTubeException specialized for what went wrong (see its subclasses)
     */
    public static Playlist createPlaylist(String name, String privacyStatus) throws YouTubeException {
        try {
            YouTube youtube = buildYouTube();

            PlaylistSnippet snippet = new PlaylistSnippet();
            snippet.setTitle(name);
            PlaylistStatus status = new PlaylistStatus();
            status.setPrivacyStatus(privacyStatus);

            Playlist unsyncedPlaylist = new Playlist();
            unsyncedPlaylist.setSnippet(snippet);
            unsyncedPlaylist.setStatus(status);

            List<String> playlistParts = Arrays.asList("snippet", "status");
            YouTube.Playlists.Insert createPlaylistRequest = youtube.playlists().insert(playlistParts, unsyncedPlaylist);
            return createPlaylistRequest.execute();
        } catch (IOException e) {
            WhatIsWrong.classifyException(e);
        }

        return null;
    }

    /**
     * @return the YouTube access token
     * @throws YouTubeException specialized for what went wrong (see its subclasses)
     */
    public static String getToken() throws YouTubeException {
        try {
            Credential creds = authUser();
            return creds.getAccessToken();
        } catch (IOException e) {
            WhatIsWrong.classifyException(e);
        }

        return null;
    }

    /**
     * Requests the name of the channel that the program works against
     *
     * @return the channel name as a string or null on failure.
     * @throws YouTubeException specialized for what went wrong (see its subclasses)
     */
    public static String requestChannelName() throws YouTubeException {
        try {
            YouTube youtube = buildYouTube();

            List<String> channelParts = Collections.singletonList("snippet");
            YouTube.Channels.List myChannel = youtube.channels().list(channelParts);
            myChannel.setMine(true);
            ChannelListResponse channelListResponse = myChannel.execute();
            List<Channel> channelList = channelListResponse.getItems();
            return channelList.get(0).getSnippet().getTitle();
        } catch (TokenResponseException tre) {
            tryDeleteToken();
        } catch (IOException e) {
            WhatIsWrong.classifyException(e);
        }
        return null;
    }

    /**
     * Requests a list all the user's playlists
     *
     * @return a list with the user's playlists
     * @throws YouTubeException For all types of errors that might occur related to YouTube,
     *                          specialized to one of its subclasses
     */
    public static ArrayList<Playlist> requestPlaylists() throws YouTubeException {
        ArrayList<Playlist> userPlaylists = new ArrayList<>();

        try {
            YouTube youtube = buildYouTube();

            //  Prepare request for playlists
            List<String> playlistParts = Arrays.asList("snippet", "contentDetails");
            YouTube.Playlists.List playlistRequest = youtube.playlists().list(playlistParts);
            playlistRequest.setMine(true);
            playlistRequest.setMaxResults(25L);

            PlaylistListResponse response;
            do {
                // Execute and save results to a list
                response = playlistRequest.execute();
                List<Playlist> playlists = response.getItems();
                userPlaylists.addAll(playlists);

                // Playlists are returned 25 at a time, if there are more then get a link to
                // the next "page" and repeat this loop
                if (response.getNextPageToken() != null) {
                    playlistRequest.setPageToken(response.getNextPageToken());
                }
            } while (response.getNextPageToken() != null);

        } catch (IOException e) {
            WhatIsWrong.classifyException(e);
        }

        return userPlaylists;
    }

    /**
     * Requests available video categories from YouTube
     *
     * @param region   The two-character country/region code for where the user are.
     * @param language The two-character language core for the language the category names
     *                 should be displayed in.
     * @return The VideoCategoryListResponse from YouTube please check the status code and
     * get the categories using response.getItems()
     * @throws YouTubeException specialized for what went wrong (see its subclasses)
     */
    public static List<VideoCategory> requestVideoCategories(String region, String language) throws YouTubeException {
        try {
            YouTube youtube = buildYouTube();
            List<String> categoryParts = Collections.singletonList("snippet");
            YouTube.VideoCategories.List videoCategoriesListForRegionRequest = youtube.videoCategories().list(categoryParts);
            videoCategoriesListForRegionRequest.setHl(language);
            videoCategoriesListForRegionRequest.setRegionCode(region);
            VideoCategoryListResponse listResponse = videoCategoriesListForRegionRequest.execute();
            if (listResponse == null)
                return null;
            return listResponse.getItems();
        } catch (IOException e) {
            WhatIsWrong.classifyException(e);
        }

        return null;
    }

    public static void tryDeleteToken() {
        try {
            Files.deleteIfExists(Paths.get(AUTH_DIR, "StoredCredential"));
        } catch (Exception ignored) {
        }
    }

    /* ******************************************************************
     * Package-Private methods
     * ******************************************************************* */

    /**
     * Makes sure the program have access to the user's channel, opens the Google
     * page for allowing programs to access the user account if needed.
     *
     * @return AuthResult containing if the auth was a success or a failure,
     * the credentials if it was successful and the exception if it was a failure.
     */
    static Credential authUser() throws IOException {
        List<String> scope = new ArrayList<>();
        scope.add(YouTubeScopes.YOUTUBE_UPLOAD);
        scope.add(YouTubeScopes.YOUTUBE);

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, getSecRead());
        FileDataStoreFactory fileFactory = new FileDataStoreFactory(new File(AUTH_DIR));

        GoogleAuthorizationCodeFlow authFlow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, scope).setDataStoreFactory(fileFactory)
                .build();

        LocalServerReceiver localReceiver = new LocalServerReceiver.Builder().setPort(7835).build();
        return new AuthorizationCodeInstalledApp(authFlow, localReceiver).authorize("user");

    }

    /**
     * Uses authUser to then build the YouTube class used for most interactions with YouTube
     *
     * @return a instance of YouTube or null on unsuccessful auth
     */
    static YouTube buildYouTube() throws IOException {
        Credential creds = authUser();

        return new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, creds)
                .setApplicationName("Stekeblads Video Uploader")
                .build();
    }

    /* ******************************************************************
     * Private methods
     * ******************************************************************* */

    /**
     * Gets user custom file if present, else default file
     */
    public static Reader getSecRead() throws IOException {
        Path userSecFile = Paths.get(AUTH_DIR, "client_secrets.json");
        if (Files.exists(userSecFile, LinkOption.NOFOLLOW_LINKS)) {
            return new InputStreamReader(new FileInputStream(new File(userSecFile.toUri())));
        } else {
            return new InputStreamReader(Objects.requireNonNull(
                    YouTubeApiLayer.class.getClassLoader().getResourceAsStream(".auth/client_secrets.json")));
        }
    }
}
