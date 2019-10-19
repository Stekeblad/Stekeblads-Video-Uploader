package io.github.stekeblad.videouploader.tagProcessing;

import io.github.stekeblad.videouploader.youtube.VideoPreset;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetaTagProcessor implements ITagProcessor {
    private boolean tagFoundInTitle;
    private boolean tagFoundInDescription;
    private boolean tagFoundInTagsList;
    private HashMap<String, String> tagsInTitle;
    private HashMap<String, String> tagsInDescription;
    private HashMap<String, String> tagsInTagsList;
    // match strings like $(metadata:nameOfTag) and $(metadata:nameOfTag,fallback)
    // nameOfTag starts after : and ends at the first occurrence of , or )
    // if , is found first it starts matching fallback until a ) is found
    private final Pattern METADATA_TAG = Pattern.compile("\\$\\(metadata:([^,)]+)(,[^)]*)?\\)");
    @Override
    public void init(VideoPreset preset, int initialAutoNum) {
        tagFoundInTitle = false;
        tagFoundInDescription = false;
        tagsInTitle = new HashMap<>();
        tagsInDescription = new HashMap<>();
        tagsInTagsList = new HashMap<>();

        tagsInTitle = matchPatternInString(preset.getVideoName());
        tagFoundInTitle = tagsInTitle.size() != 0;

        tagsInDescription = matchPatternInString((preset.getVideoDescription()));
        tagFoundInDescription = tagsInDescription.size() != 0;

        for (String videoTag : preset.getVideoTags()) {
            HashMap<String, String> matches = matchPatternInString((videoTag));
            if (matches.size() != 0) {
                tagFoundInTagsList = true;
                tagsInTagsList.putAll(matches);
            }
        }
    }

    @Override
    public String processTitle(String currentTitle, File videoFile) {
        if (!tagFoundInTitle)
            return currentTitle;
        return String.join(", ", tagsInTitle.keySet());
    }

    @Override
    public String processDescription(String currentDescription, File videoFile) {
        if (!tagFoundInDescription)
            return currentDescription;
        return String.join(", ", tagsInDescription.keySet());
    }

    @Override
    public List<String> processTags(List<String> currentTags, File videoFile) {
        return currentTags;
    }

    @Override
    public String processorName() {
        return "Metadata TagProcessor";
    }

    private HashMap<String, String> matchPatternInString(String source) {
        HashMap<String, String> matches = new HashMap<>();
        Matcher matcher = METADATA_TAG.matcher(source);
        while (matcher.find()) {
            tagFoundInTitle = true;
            String first = matcher.group(1);
            String second = matcher.group(2);
            if (second != null)
                second = second.substring(1); // the comma before fallback is included in the match, remove it
            matches.put(first, second);
        }
        return matches;
    }
}
