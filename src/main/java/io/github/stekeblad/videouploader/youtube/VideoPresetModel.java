package io.github.stekeblad.videouploader.youtube;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class VideoPresetModel extends VideoInfoBaseModel implements Comparable<VideoPresetModel> {

    private String presetName;

    public String getPresetName() {
        return presetName;
    }

    public void setPresetName(String presetName) {
        this.presetName = presetName;
    }

    public VideoPresetModel(String videoName, String videoDescription, String visibility, List<String> videoTags,
                            LocalPlaylist selectedPlaylist, LocalCategory selectedCategory, boolean tellSubs,
                            String thumbNailPath, boolean madeForKids, String presetName) {
        super(videoName, videoDescription, visibility, videoTags, selectedPlaylist,
                selectedCategory, tellSubs, thumbNailPath, madeForKids);
        this.presetName = presetName;
    }

    public VideoPresetModel() {
        super();
    }

    // For sorting (by preset name)
    @Override
    public int compareTo(@NotNull VideoPresetModel o) {
        return this.presetName.compareTo(o.getPresetName());
    }
}
