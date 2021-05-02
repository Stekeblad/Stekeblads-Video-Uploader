package io.github.stekeblad.videouploader.tagProcessing;

import io.github.stekeblad.videouploader.models.NewVideoPresetModel;

import java.io.File;
import java.util.List;

/**
 * Numbers videos that uses this preset starting at initialAutoNum.
 * The number is inserted at the location of the tag $(ep) inside the title field
 */
public class EpisodeTagProcessor implements ITagProcessor {
    private int autoNum;
    private boolean tagFound;
    private final String EPISODE_TAG = "$(ep)";

    public EpisodeTagProcessor() {
    }

    @Override
    public void init(NewVideoPresetModel preset, int initialAutoNum) {
        tagFound = preset.getVideoName().contains(EPISODE_TAG);
        if (!tagFound)
            return;
        autoNum = initialAutoNum;

    }

    @Override
    public String processTitle(String currentTitle, File videoFile) {
        if (tagFound)
            return currentTitle.replace(EPISODE_TAG, Integer.toString(autoNum++));
        else
            return currentTitle;
    }

    @Override
    public String processDescription(String currentDescription, File videoFile) {
        return currentDescription;
    }

    @Override
    public List<String> processTags(List<String> currentTags, File videoFile) {
        return currentTags;
    }

    @Override
    public String processorName() {
        return "Episode TagProcessor";
    }
}
