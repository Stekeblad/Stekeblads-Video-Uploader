package io.github.stekeblad.videouploader.tagProcessing;

import io.github.stekeblad.videouploader.youtube.VideoPreset;
import io.github.stekeblad.videouploader.youtube.utils.PlaylistUtils;

import java.io.File;
import java.util.List;

public class PlaylistTagProcessor implements ITagProcessor {
    private String playlistUrl;
    private boolean tagFound;
    private final String PLAYLIST_TAG = "$(playlist)";

    public PlaylistTagProcessor() {
    }

    public void init(VideoPreset preset, int initialAutoNum) {
        tagFound = preset.getVideoDescription().contains(PLAYLIST_TAG);
        if (!tagFound)
            return;
        PlaylistUtils playlistUtils = PlaylistUtils.INSTANCE;
        playlistUrl = playlistUtils.getPlaylistUrl(preset.getSelectedPlaylist());
        if (playlistUrl == null) {
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
