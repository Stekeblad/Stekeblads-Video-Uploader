package io.github.stekeblad.videouploader.tagProcessing;

import io.github.stekeblad.videouploader.models.NewVideoPresetModel;
import io.github.stekeblad.videouploader.youtube.LocalPlaylist;

import java.io.File;
import java.util.List;

/**
 * Locates the $(playlist) tag in video descriptions and replaces them with the URL to the playlist set in the preset
 */
public class PlaylistTagProcessor implements ITagProcessor {
    private String playlistUrl;
    private boolean tagFound;
    private final String PLAYLIST_TAG = "$(playlist)";

    public PlaylistTagProcessor() {
    }

    public void init(NewVideoPresetModel preset, int initialAutoNum) {
        tagFound = preset.getVideoDescription().contains(PLAYLIST_TAG);
        if (!tagFound)
            return;

        LocalPlaylist playlist = preset.getSelectedPlaylist();
        if (playlist != null) {
            playlistUrl = playlist.playlistUrl();
        } else {
            playlistUrl = "";
        }
    }

    @Override
    public String processTitle(String currentTitle, File videoFile) {
        return currentTitle;
    }

    @Override
    public String processDescription(String currentDescription, File videoFile) {
        if (tagFound)
            return currentDescription.replace(PLAYLIST_TAG, playlistUrl);
        else
            return currentDescription;
    }

    @Override
    public List<String> processTags(List<String> currentTags, File videoFile) {
        return currentTags;
    }

    @Override
    public String processorName() {
        return "Playlist TagProcessor";
    }
}
