package io.github.stekeblad.videouploader.youtube;

import org.jetbrains.annotations.NotNull;

/**
 * A class for managing the details of a playlist that has been retrieved from YouTube.
 * visible is for filtering what playlist to show in the program, useful if the user has a lot of playlist and only
 * need to use a few of them with the program.
 * id is the playlist id on YouTube.
 * name is the name of the playlist.
 */
public class LocalPlaylist implements Comparable<LocalPlaylist> {
    private boolean visible;
    private String id;
    private String name;

    public LocalPlaylist(boolean visible, String playlistId, String playlistName) {
        this.visible = visible;
        this.id = playlistId;
        this.name = playlistName;
    }

    public boolean isVisible() {
        return visible;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String playlistUrl() {
        return "https://www.youtube.com/playlist?list=" + id;
    }

    @Override
    public int compareTo(@NotNull LocalPlaylist other) {
        return this.name.compareTo(other.getName());
    }
}
