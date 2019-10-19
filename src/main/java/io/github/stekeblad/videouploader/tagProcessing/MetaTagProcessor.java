package io.github.stekeblad.videouploader.tagProcessing;

import io.github.stekeblad.videouploader.youtube.VideoPreset;

import java.util.List;

public class MetaTagProcessor implements ITagProcessor {
    @Override
    public void init(VideoPreset preset, int initialAutoNum) {

    }

    @Override
    public String processTitle(String currentTitle) {
        return null;
    }

    @Override
    public String processDescription(String currentDescription) {
        return null;
    }

    @Override
    public List<String> processTags(List<String> currentTags) {
        return null;
    }

    @Override
    public String processorName() {
        return null;
    }
}
