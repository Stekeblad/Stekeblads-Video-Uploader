package io.github.stekeblad.videouploader.youtube.utils;

import com.google.api.services.youtube.model.Playlist;
import io.github.stekeblad.videouploader.utils.AlertUtils;
import io.github.stekeblad.videouploader.utils.ConfigManager;
import io.github.stekeblad.videouploader.utils.TimeUtils;
import io.github.stekeblad.videouploader.utils.translation.TranslationBundles;
import io.github.stekeblad.videouploader.utils.translation.Translations;
import io.github.stekeblad.videouploader.utils.translation.TranslationsManager;
import io.github.stekeblad.videouploader.youtube.LocalPlaylist;
import io.github.stekeblad.videouploader.youtube.YouTubeApiLayer;
import io.github.stekeblad.videouploader.youtube.exceptions.OtherYouTubeException;
import io.github.stekeblad.videouploader.youtube.exceptions.QuotaLimitExceededException;
import io.github.stekeblad.videouploader.youtube.exceptions.YouTubeException;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Enum-Singleton class for handling playlists. Initialize ConfigManager with the configManager() method before using
 * this class
 */
public enum PlaylistUtils {
    INSTANCE;

    private final ConfigManager configManager = ConfigManager.INSTANCE;
    private String noPlaylistName = "";
    private final Translations transBasic = TranslationsManager.getTranslation(TranslationBundles.BASE);
    private ArrayList<LocalPlaylist> playlists = null;

    /**
     * Gets playlists from Youtube. Does not check if permission has been given or not. If you want to display a warning
     * to the user that they will be sent to youtube for granting permission or similar, do it before calling this method
     */
    public void refreshPlaylist() {
        ArrayList<Playlist> remotePlaylists = null;
        try {
            // Get a list of playlists from YouTube
            remotePlaylists = YouTubeApiLayer.requestPlaylists();
            // Handle some of the errors that may be returned
        } catch (QuotaLimitExceededException quotaException) {
            String userClockAtPacificMidnight = TimeUtils.fromMidnightPacificToUserTimeZone();
            Platform.runLater(() ->
                    AlertUtils.simpleClose(transBasic.getString("app_name"), "Playlist refresh failed because " +
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

        if (remotePlaylists == null)
            return;

        HashMap<String, LocalPlaylist> comparisonMap = new HashMap<>();
        for (LocalPlaylist currentPlaylist : playlists) {
            comparisonMap.put(currentPlaylist.getId(), currentPlaylist);
        }

        ArrayList<LocalPlaylist> updatedPlaylists = new ArrayList<>();
        // Loop over all playlists returned by YouTube and check if there are any new, deleted or renamed playlists
        for (Playlist remotePlaylist : remotePlaylists) {
            String remoteName = remotePlaylist.getSnippet().getTitle();
            // Check if it is new or exists since earlier (false if new)
            if (comparisonMap.containsKey(remotePlaylist.getId())) {
                LocalPlaylist foundPlaylist = comparisonMap.get(remotePlaylist.getId());
                // Check if name has changed (false if same as earlier)
                if (!foundPlaylist.getName().equals(remoteName)) {
                    // The playlist name has been changed and needs to be updated
                    foundPlaylist.setName(remoteName);
                }
                // Save the playlist (that maybe changed name)
                updatedPlaylists.add(foundPlaylist);
            } else {
                // It is a new playlist
                updatedPlaylists.add(new LocalPlaylist(true, remotePlaylist.getId(), remoteName));
            }
        }

        // Done, overwrite old list of playlists with the updated list
        playlists = updatedPlaylists;
        saveCache();
    }

    /**
     * First attempts to load local playlists from disc, if no playlists is found then attempt to get playlists from youtube
     */
    private void getUserPlaylists() {
        loadCache();
    }

    /**
     * @return a list of all playlists (name, id, visible)
     */
    public ArrayList<LocalPlaylist> getAllPlaylists() {
        if (playlists == null) {
            return null;
        }
        return playlists;
    }

    /**
     *
     * @return a list with the names of all playlists
     */
    public ArrayList<String> getPlaylistNames() {
        if (playlists == null) {
            getUserPlaylists();
        }
        return (ArrayList<String>) playlists.stream()
                .map(LocalPlaylist::getName)
                .collect(Collectors.toList());
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

        visiblePlaylists.addAll(playlists.stream()
                .filter(LocalPlaylist::isVisible)
                .map(LocalPlaylist::getName)
                .collect(Collectors.toList()));

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

        for (LocalPlaylist playlist : playlists) {
            if (playlist.getName().equals(playlistName))
                return playlist.getId();
        }

        return null;
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
        Playlist newPlaylist = null;
        try {
            // Send request to create new playlist
            newPlaylist = YouTubeApiLayer.createPlaylist(name, privacy);
            // Handle different errors
        } catch (QuotaLimitExceededException quotaException) {
            String userClockAtPacificMidnight = TimeUtils.fromMidnightPacificToUserTimeZone();
            Platform.runLater(() ->
                    AlertUtils.simpleClose(transBasic.getString("app_name"), "Playlist could not be created because " +
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

        if (newPlaylist == null)
            return null;

        LocalPlaylist localPlaylist = new LocalPlaylist(
                true, newPlaylist.getId(), newPlaylist.getSnippet().getTitle());
        playlists.add(localPlaylist);
        return localPlaylist;
    }

    /**
     * Sets the visible variable for the playlist with the name playlistName
     * @param playlistName the name of the playlist to change the visible status on
     * @param visibleStatus the new value of the visible variable
     */
    public void setVisible(String playlistName, boolean visibleStatus) {
        for (int i = 0; i < playlists.size(); i++) {
            if (playlists.get(i).getName().equals(playlistName)) {
                LocalPlaylist playlistToModify = playlists.get(i);
                playlistToModify.setVisible(visibleStatus);
                playlists.set(i, playlistToModify);
                return;
            }
        }
    }

    /**
     * Saves the playlists to disc
     */
    public void saveCache() {
        StringBuilder saveString = new StringBuilder();
        playlists.forEach((playlist) -> saveString
                .append(playlist.isVisible()).append(":")
                .append(playlist.getId()).append(":")
                .append(playlist.getName()).append("\n"));
        // Delete last newline
        saveString.deleteCharAt(saveString.length() - 1);
        configManager.savePlaylistCache(saveString.toString());
    }

    /**
     * Loads playlists from disc
     */
    public void loadCache() {
        playlists = new ArrayList<>();
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
                boolean visible = Boolean.parseBoolean(loadedPlaylist.substring(0, colonIndex));
                // throw away used data and locate next colon that separates playlist Id and name
                loadedPlaylist = loadedPlaylist.substring(colonIndex + 1);
                colonIndex = loadedPlaylist.indexOf(':');
                // Read id and name
                String id = loadedPlaylist.substring(0, colonIndex);
                String name = loadedPlaylist.substring(colonIndex + 1);
                // save
                playlists.add(new LocalPlaylist(visible, id, name));
            }
        } else {
            // Older version, for release 1.0
            for (String loadedPlaylist : loadedPlaylists) {
                String id = loadedPlaylist.substring(0, loadedPlaylist.indexOf(':'));
                String name = loadedPlaylist.substring(loadedPlaylist.indexOf(':') + 1);
                playlists.add(new LocalPlaylist(true, id, name));
            }
        }

    }
}
