package io.github.stekeblad.videouploader.youtube.tagProcessing;

import io.github.stekeblad.videouploader.youtube.VideoPreset;

import java.util.List;

public class EpisodeTagProcessor implements ITagProcessor {
    private int autoNum;
    private boolean tagFound;
    private final String EPISODE_TAG = "$(ep)";

    public EpisodeTagProcessor() {
    }

    @Override
    public void init(VideoPreset preset, int initalAutoNum) {
        tagFound = preset.getVideoName().contains(EPISODE_TAG);
        if (!tagFound)
            return;
        autoNum = initalAutoNum;

    }

    @Override
    public String processTitle(String currentTitle) {
        if (tagFound)
            return currentTitle.replace(EPISODE_TAG, Integer.toString(autoNum++));
        else
            return currentTitle;
    }

    @Override
    public String processDescription(String currentDescription) {
        return currentDescription;
    }

    @Override
    public List<String> processTags(List<String> currentTags) {
        return currentTags;
    }

    @Override
    public String processorName() {
        return "Episode TagProcessor";
    }
}
