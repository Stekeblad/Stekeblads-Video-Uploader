package io.github.stekeblad.videouploader.tagProcessing;

import io.github.stekeblad.videouploader.youtube.VideoPreset;

import java.io.File;
import java.util.List;

public class RawnameTagProcessor implements ITagProcessor {
    @Override
    public void init(VideoPreset preset, int initialAutoNum) {

    }

    @Override
    public String processTitle(String currentTitle, File videoFile) {
        return null;
    }

    @Override
    public String processDescription(String currentDescription, File videoFile) {
        return null;
    }

    @Override
    public List<String> processTags(List<String> currentTags, File videoFile) {
        return null;
    }

    @Override
    public String processorName() {
        return null;
    }
}
