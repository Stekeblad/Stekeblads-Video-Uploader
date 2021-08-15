package io.github.stekeblad.videouploader.managers;

import com.google.api.services.youtube.model.Playlist;
import com.google.gson.JsonObject;
import io.github.stekeblad.videouploader.managers.playlistMigrators.PlaylistMigrator;
import io.github.stekeblad.videouploader.utils.AlertUtils;
import io.github.stekeblad.videouploader.utils.Constants;
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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.github.stekeblad.videouploader.utils.Constants.*;

/**
 * Handles loading, saving, reading and manipulating playlists
 */
public class PlaylistManager extends ManagerBase {

    private static PlaylistManager _manager;

    /**
     * Returns the shared PlaylistManager, creating it if this is the first time this
     * method gets called since the program started.
     */
    public static PlaylistManager getPlaylistManager() {
        if (_manager == null)
            _manager = new PlaylistManager();

        return _manager;
    }

    private final Path playlistPath;
    private final SortedList<LocalPlaylist> rawPlaylists; // includes the dummy list
    private final FilteredList<LocalPlaylist> playlists; // all the users playlists
    private final FilteredList<LocalPlaylist> filteredVisiblePlaylists;
    private final Translations transBasic = TranslationsManager.getTranslation(TranslationBundles.BASE);

    /**
     * Loads saved playlists data and updates its data format if its using an older format
     */
    private PlaylistManager() {
        playlistPath = Paths.get(PLAYLIST_FILE).toAbsolutePath();
        PlaylistMigrator playlistMigrator = new PlaylistMigrator();

        // Does a json data file exist?
        if (!Files.exists(playlistPath)) {
            final Path oldPlaylistFilePath = Paths.get(DATA_DIR + "/playlist");
            // Does a file in the old pre-json format exist?
            if (Files.exists(oldPlaylistFilePath)) {
                try {
                    // back up, migrate, delete original (but keep copy)
                    Files.copy(oldPlaylistFilePath, Paths.get(CONFIG_BACKUP_DIR + "/playlists-" + TimeUtils.currentTimeStringPathSafe()));
                    List<String> playlistLines = Files.readAllLines(oldPlaylistFilePath);
                    config = playlistMigrator.migrate(playlistLines);
                    writeConfigToFile(playlistPath);
                    Files.delete(oldPlaylistFilePath);
                } catch (Exception e) {
                    AlertUtils.exceptionDialog("Failed to update playlists file",
                            "Something went wrong when updating playlist data to a newer version.",
                            e);
                }
            } else {
                // no file exists
                config = new JsonObject();
                set(Constants.VERSION_FORMAT_KEY, PlaylistMigrator.latestFormatVersion);
                set("playlists", new ArrayList<LocalPlaylist>());
            }
        } else {
            // File in json format found
            try {
                loadConfigFromFile(playlistPath);
                if (!playlistMigrator.isLatestVersion(config)) {
                    // File is in a older format, create a backup of it and then upgrade to latest format
                    final String backupFileName = "/playlists-" + TimeUtils.currentTimeStringPathSafe() + ".json";
                    Files.copy(playlistPath, Paths.get(CONFIG_BACKUP_DIR + backupFileName));
                    playlistMigrator.migrate(config);
                    writeConfigToFile(playlistPath);
                }
            } catch (IOException e) {
                AlertUtils.exceptionDialog("Failed to load or update playlists file",
                        "The playlists file could not be read. If the program have updated since last run something" +
                                " could have failed while updating the settings file to a newer version",
                        e);
            }
        }
        ArrayList<LocalPlaylist> tempList = getArrayList("playlists", LocalPlaylist.class);
        // giving sort null means that the items in the list must implement the Comparable interface and it's compareTo method will be used
        tempList.sort(null);
        // add the default no playlist-playlist. TODO translate the name
        tempList.add(0, new LocalPlaylist(false, LocalPlaylist.MAGIC_PLAYLIST_ID, "--No playlist--"));
        rawPlaylists = FXCollections.observableArrayList(tempList).sorted();
        playlists = rawPlaylists.filtered(localPlaylist -> !localPlaylist.getId().equals("0"));
        filteredVisiblePlaylists = rawPlaylists.filtered(LocalPlaylist::isVisible);
    }

    /**
     * Saves all playlist data to the playlists save file
     *
     * @throws IOException if exception occurred when writing to the save file
     */
    public void savePlaylists() throws IOException {
        set("playlists", playlists);
        writeConfigToFile(playlistPath);
    }

    /**
     * @return an filtered observable list with all playlists that is configured to be visible
     * * in the playlist dropdown lists
     */
    public ObservableList<LocalPlaylist> getVisiblePlaylists() {
        return filteredVisiblePlaylists;
    }

    /**
     * @return an observable list with all loaded playlists
     */
    public ObservableList<LocalPlaylist> getAllPlaylists() {
        return playlists;
    }

    /**
     * Looks for the first LocalPlaylist that have a name exactly matching the parameter playlistName
     *
     * @param playlistName The name of the playlist to find
     * @return An Optional&lt;LocalPlaylist&gt; that either contains the first matching LocalPlaylist or is empty if
     * no LocalPlaylist matches
     * @apiNote You should work with Ids instead of names then possible
     */
    public Optional<LocalPlaylist> findByName(String playlistName) {
        return playlists.stream().filter(lp -> lp.getName().equals(playlistName)).findFirst();
    }

    /**
     * Looks for the first LocalPlaylist that have a playlistId exactly matching the parameter playlistId
     *
     * @param playlistId The id of the playlist to find
     * @return An Optional&lt;LocalPlaylist&gt; that either contains the first matching LocalPlaylist or is empty if
     * no LocalPlaylist matches
     */
    public Optional<LocalPlaylist> findById(String playlistId) {
        return playlists.stream().filter(lp -> lp.getId().equals(playlistId)).findFirst();
    }

    /**
     * Downloads all the user's playlists from YouTube and updates the local list. Playlists that have been deleted on
     * YouTube is removed, renamed playlists is updated and keep their local visibility status and new playlists are
     * added defaulting to locally visible. All lists observing the allPlaylists list or the visiblePlaylists list
     * should be notified and update them self automatically, I hope.
     */
    public void updateFromYouTube() {
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
            Platform.runLater(() -> AlertUtils.unhandledExceptionDialog(e));
        }

        if (remotePlaylists == null)
            return;

        // Map the playlists retrieved from YouTube to LocalPlaylists
        List<LocalPlaylist> remoteTransformedPlaylists = remotePlaylists.stream()
                .map((ytList) -> new LocalPlaylist(
                        playlists.stream()
                                // Check if the playlist exists since before, the id is never changed
                                .filter((lp) -> lp.getId().equals(ytList.getId()))
                                .findFirst()
                                // Keep its visibility status or default to true
                                .map(LocalPlaylist::isVisible)
                                .orElse(true),
                        // set playlist id and name
                        ytList.getId(),
                        ytList.getSnippet().getTitle()))
                .sorted()
                .collect(Collectors.toList());

        // Do not overwrite playlists with remoteTransformedPlaylists, playlists is observable and
        // it will probably make all observers lose track of it.
        // Keep the first element (the no playlist-playlist)
        rawPlaylists.remove(1, rawPlaylists.size());
        rawPlaylists.addAll(remoteTransformedPlaylists);
    }

    /**
     * Creates a new playlist for the the user's channel with the given name and privacy status.
     * YouTube-type exceptions will be caught and displayed in a alert window, other exceptions
     * is not handled.
     *
     * @param name    the name of the playlist to create
     * @param privacy the privacy status for the new playlist
     */
    public void createPlaylist(String name, String privacy) {
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
            Platform.runLater(() -> AlertUtils.unhandledExceptionDialog(e));
        }

        if (newPlaylist == null)
            return;

        LocalPlaylist localPlaylist = new LocalPlaylist(
                true, newPlaylist.getId(), newPlaylist.getSnippet().getTitle());
        rawPlaylists.add(localPlaylist);
    }
}
