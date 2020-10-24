package io.github.stekeblad.videouploader.youtube;

import io.github.stekeblad.videouploader.youtube.utils.VisibilityStatus;

import java.util.List;

public class VideoInfoBaseModel {
    private String videoName;
    private String videoDescription;
    private String visibility;
    private List<String> videoTags;
    private LocalPlaylist selectedPlaylist;
    private LocalCategory selectedCategory;
    private boolean tellSubs;
    private String /*Path?*/ thumbNailPath;
    private boolean madeForKids;

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public String getVideoDescription() {
        return videoDescription;
    }

    public void setVideoDescription(String videoDescription) {
        this.videoDescription = videoDescription;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        if (!VisibilityStatus.PUBLIC.equals(visibility)
                && !VisibilityStatus.UNLISTED.equals(visibility)
                && !VisibilityStatus.PRIVATE.equals(visibility)) {
            throw new IllegalArgumentException("Status must be \"public\", \"private\" or \"unlisted\"");
        }
        this.visibility = visibility;
    }

    public List<String> getVideoTags() {
        return videoTags;
    }

    public void setVideoTags(List<String> videoTags) {
        this.videoTags = videoTags;
    }

    public LocalPlaylist getSelectedPlaylist() {
        return selectedPlaylist;
    }

    public void setSelectedPlaylist(LocalPlaylist selectedPlaylist) {
        this.selectedPlaylist = selectedPlaylist;
    }

    public LocalCategory getSelectedCategory() {
        return selectedCategory;
    }

    public void setSelectedCategory(LocalCategory selectedCategory) {
        this.selectedCategory = selectedCategory;
    }

    public boolean isTellSubs() {
        return tellSubs;
    }

    public void setTellSubs(boolean tellSubs) {
        this.tellSubs = tellSubs;
    }

    public String getThumbNailPath() {
        return thumbNailPath;
    }

    public void setThumbNailPath(String thumbNailPath) {
        this.thumbNailPath = thumbNailPath;
    }

    public boolean isMadeForKids() {
        return madeForKids;
    }

    public void setMadeForKids(boolean madeForKids) {
        this.madeForKids = madeForKids;
    }

    public VideoInfoBaseModel(String videoName, String videoDescription, String visibility, List<String> videoTags,
                              LocalPlaylist selectedPlaylist, LocalCategory selectedCategory, boolean tellSubs,
                              String thumbNailPath, boolean madeForKids) {
        this.videoName = videoName;
        this.videoDescription = videoDescription;
        setVisibility(visibility);
        this.videoTags = videoTags;
        this.selectedPlaylist = selectedPlaylist;
        this.selectedCategory = selectedCategory;
        this.tellSubs = tellSubs;
        this.thumbNailPath = thumbNailPath;
        this.madeForKids = madeForKids;
    }

    public VideoInfoBaseModel() {

    }
}
