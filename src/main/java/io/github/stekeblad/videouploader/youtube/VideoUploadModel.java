package io.github.stekeblad.videouploader.youtube;

import java.io.File;
import java.util.List;

public class VideoUploadModel extends VideoInfoBaseModel {

    private File videoFile;

    public VideoUploadModel(String videoName, String videoDescription, String visibility, List<String> videoTags,
                            LocalPlaylist selectedPlaylist, LocalCategory selectedCategory, boolean tellSubs,
                            String thumbNailPath, boolean madeForKids, File videoFile) {
        super(videoName, videoDescription, visibility, videoTags, selectedPlaylist, selectedCategory,
                tellSubs, thumbNailPath, madeForKids);
        this.videoFile = videoFile;
    }

    public VideoUploadModel() {
        super();
    }
}
