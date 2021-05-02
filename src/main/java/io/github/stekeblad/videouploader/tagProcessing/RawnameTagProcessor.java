package io.github.stekeblad.videouploader.tagProcessing;

import io.github.stekeblad.videouploader.models.NewVideoPresetModel;

import java.io.File;
import java.util.List;

/**
 * Replaces the $(rawname) tag with the name of the video file (excluding the file extension)
 */
public class RawnameTagProcessor implements ITagProcessor {
    private boolean tagFoundInTitle;
    private boolean tagFoundInDescription;
    private boolean tagFoundInTagsList;
    private final String RAWNAME_TAG = "$(rawname)";

    @Override
    public void init(NewVideoPresetModel preset, int initialAutoNum) {
        tagFoundInTitle = preset.getVideoName().contains(RAWNAME_TAG);
        tagFoundInDescription = preset.getVideoDescription().contains(RAWNAME_TAG);
        tagFoundInTagsList = false;
        preset.getVideoTags().forEach(videoTag -> {
            if (videoTag.contains(RAWNAME_TAG))
                tagFoundInTagsList = true;
        });
    }

    @Override
    public String processTitle(String currentTitle, File videoFile) {
        if (tagFoundInTitle)
            return currentTitle.replace(RAWNAME_TAG, nameWithoutExtension(videoFile));
        else
            return currentTitle;
    }

    @Override
    public String processDescription(String currentDescription, File videoFile) {
        if (tagFoundInDescription)
            return currentDescription.replace(RAWNAME_TAG, nameWithoutExtension(videoFile));
        else
            return currentDescription;
    }

    @Override
    public List<String> processTags(List<String> currentTags, File videoFile) {
        if (tagFoundInTagsList) {
            for (int i = 0; i < currentTags.size(); i++) {
                currentTags.set(i, currentTags.get(i).replace(RAWNAME_TAG, nameWithoutExtension(videoFile)));
            }
        }
        return currentTags;
    }

    @Override
    public String processorName() {
        return "Rawname TagProcessor";
    }

    private String nameWithoutExtension(File file) {
        return file.getName().substring(0, file.getName().lastIndexOf("."));
    }
}
