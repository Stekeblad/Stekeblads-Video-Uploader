package io.github.stekeblad.videouploader.tagProcessing.metaDataTagProcessor;

import io.github.stekeblad.videouploader.tagProcessing.ITagProcessor;
import io.github.stekeblad.videouploader.youtube.VideoPreset;
import javafx.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The metadata TagProcessor replaces occurrences of the $(metadata) tag with information from the metadata of the selected files.
 * The metadata tag takes two parameters, the name of the metadata field and a optional fallback value if the metadata
 * field was not found. A correctly formatted metadata tag will never be returned by the processor, it will always be
 * replaced by the value of the requested tag from the metadata, the given fallback value or replaced by nothing.
 */
public class MetaDataTagProcessor implements ITagProcessor {
    private boolean tagFoundInTitle;
    private boolean tagFoundInDescription;
    private boolean tagFoundInTagsList;
    private ArrayList<Pair<String, String>> tagsInTitle;
    private ArrayList<Pair<String, String>> tagsInDescription;
    private ArrayList<Pair<String, String>> tagsInTagsList;

    // Cache all metadata for the existence of this instance of the tag processor. The scanning of tags may sometimes
    // require the entire video file to be scanned. If the tag is used in more than one of [title / description / tag list]
    // we want to avoid to read the file multiple times.
    private HashMap<String, MetaDataReader> fileMetaDataCache;

    // match strings like $(metadata:nameOfTag) and $(metadata:nameOfTag,fallback)
    // nameOfTag starts after : and ends at the first occurrence of , or )
    // if , is found first it starts matching fallback until a ) is found
    private final Pattern METADATA_TAG = Pattern.compile("\\$\\(metadata:([^,)]+)(,[^)]*)?\\)");
    @Override
    public void init(VideoPreset preset, int initialAutoNum) {
        tagFoundInTitle = false;
        tagFoundInDescription = false;
        tagsInTitle = new ArrayList<>();
        tagsInDescription = new ArrayList<>();
        tagsInTagsList = new ArrayList<>();
        fileMetaDataCache = new HashMap<>();

        // Search for metadata tags in preset title
        tagsInTitle = matchPatternInString(preset.getVideoName());
        tagFoundInTitle = tagsInTitle.size() != 0;

        // Search for metadata tags in preset description
        tagsInDescription = matchPatternInString((preset.getVideoDescription()));
        tagFoundInDescription = tagsInDescription.size() != 0;

        // Search for metadata tags in preset video tags
        for (String videoTag : preset.getVideoTags()) {
            ArrayList<Pair<String, String>> matches = matchPatternInString((videoTag));
            if (matches.size() != 0) {
                tagFoundInTagsList = true;
                tagsInTagsList.addAll(matches);
            }
        }
    }

    @Override
    public String processTitle(String currentTitle, File videoFile) {
        if (!tagFoundInTitle)
            return currentTitle;

        MetaDataReader metaReader = getMetaDataReaderForFile(videoFile);
        return replaceTagsInString(currentTitle, tagsInTitle, metaReader);
    }

    @Override
    public String processDescription(String currentDescription, File videoFile) {
        if (!tagFoundInDescription)
            return currentDescription;

        MetaDataReader metaReader = getMetaDataReaderForFile(videoFile);
        return replaceTagsInString(currentDescription, tagsInDescription, metaReader);
    }

    @Override
    public List<String> processTags(List<String> currentTags, File videoFile) {
        if (!tagFoundInTagsList)
            return currentTags;

        MetaDataReader metaReader = getMetaDataReaderForFile(videoFile);
        for (int i = currentTags.size() - 1; i >= 0; i--) {
            String newTagValue = replaceTagsInString(currentTags.get(i), tagsInTagsList, metaReader);
            if (newTagValue.isEmpty())
                currentTags.remove(i);
            else
                currentTags.set(i, newTagValue);
        }
        return currentTags;
    }

    @Override
    public String processorName() {
        return "Metadata TagProcessor";
    }

    /**
     * Uses the METADATA_TAG regex pattern to find $(metadata) tags in the input string.
     *
     * @param source a string to search in
     * @return a list of pairs there the key is the metadata field name and
     * the value is the entered fallback value, or null if no fallback was set
     */
    private ArrayList<Pair<String, String>> matchPatternInString(String source) {
        ArrayList<Pair<String, String>> matches = new ArrayList<>();
        Matcher matcher = METADATA_TAG.matcher(source);
        while (matcher.find()) {
            tagFoundInTitle = true;
            String first = matcher.group(1);
            String second = matcher.group(2);
            if (second != null)
                second = second.substring(1); // the comma before fallback is included in the match, remove it
            matches.add(new Pair<>(first, second));
        }
        return matches;
    }

    /**
     * Checks if the video file has already been scanned for metadata and if so returns the result of the previous analysis.
     * If the file has not been analysed it is analysed and the result is cached before being returned
     *
     * @param videoFile a videofile to get the metadata for
     * @return a MetaDataReader with the metadata for the given file
     */
    private MetaDataReader getMetaDataReaderForFile(File videoFile) {
        if (fileMetaDataCache.containsKey(videoFile.getAbsolutePath()))
            return fileMetaDataCache.get(videoFile.getAbsolutePath());

        MetaDataReader reader = new MetaDataReader(videoFile);
        fileMetaDataCache.put(videoFile.getAbsolutePath(), reader);
        return reader;
    }

    /**
     * replaces $(metadata) tags in the source string
     *
     * @param source            the string to find and replace tags in
     * @param tagsInPresetField a list of tags and fallback values that was found in the init step
     * @param reader            a MetaDataReader with the metadata found in the file currently being processed
     * @return the source string with all properly formatted $(metadata) tags replaced with values from the video file,
     * the fallback values or just removed if a metadata value was not found and not had a fallback.
     */
    private String replaceTagsInString(String source, ArrayList<Pair<String, String>> tagsInPresetField, MetaDataReader reader) {
        for (Pair<String, String> tag : tagsInPresetField) {
            String fileTagValue = reader.getTagValueByName(tag.getKey());
            if (fileTagValue == null)
                fileTagValue = tag.getValue() == null ? "" : tag.getValue();

            source = source.replace(recreateTag(tag), fileTagValue);
        }
        return source;
    }

    /**
     * Recreates a $(metadata) tag from the metadata name and fallback value
     *
     * @param tag metadata name and fallback
     * @return The $(metadata) tag as it looks like in the preset
     */
    private String recreateTag(Pair<String, String> tag) {
        if (tag.getValue() != null && !tag.getValue().isEmpty())
            return String.format("$(metadata:%s,%s)", tag.getKey(), tag.getValue());
        return String.format("$(metadata:%s)", tag.getKey());
    }
}
