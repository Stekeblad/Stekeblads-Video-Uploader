package io.github.stekeblad.videouploader.tagProcessing.metaDataTagProcessor;

import javafx.util.Pair;
import org.jcodec.containers.mp4.boxes.MetaValue;
import org.jcodec.movtool.MetadataEditor;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

/**
 * A MetaDataReader extracts metadata from a video file and allows you to query the tags in it using friendly names and
 * four-character codes. This does not work for all files. Depending on there in the file the metadata is located
 * the entire file may need to be read and that may take some time depending on size!
 * <p>
 * To check if the metadata could be read you can check the return value of isFileSupported().
 * <p>
 * Individual tags can be queried for with getTagValueByName() and all found tags can be retrieved with getAllTagsAndValues()
 */
public class MetaDataReader {
    private ArrayList<Pair<String, String>> _tags;
    private boolean _isFileSupported;

    /**
     * Creates a MetaDataReader and scans the given file for metadata. Depending on there in the file the metadata is located
     * the entire file may need to be read and that may take some time depending on size!
     *
     * @param videoFile
     */
    public MetaDataReader(File videoFile) {
        _isFileSupported = true;
        _tags = new ArrayList<>();
        try {
            MetadataEditor editor = MetadataEditor.createFrom(videoFile);

            Map<String, MetaValue> keyedMetaMap = editor.getKeyedMeta();
            keyedMetaMap.forEach((s, metaValue) ->
                    _tags.add(new Pair<>(s, metaValue.toString())));

            Map<Integer, MetaValue> n = editor.getItunesMeta();
            n.forEach((integer, metaValue) ->
                    _tags.add(new Pair<>(MetaDataNamesMapping.tryConvertIntToFriendlyName(integer), metaValue.toString())));

        } catch (Exception ex) {
            // set to false if file is not supported (IllegalArgumentException was thrown)
            _isFileSupported = !(ex instanceof IllegalArgumentException);
        }
    }

    /**
     * @return all tags found in the file, if the format of the scanned file is not supported the list will always be empty
     */
    public ArrayList<Pair<String, String>> getAllTagsAndValues() {
        return _tags;
    }

    /**
     * Gets the value of a specific tag
     *
     * @param tagName name of the tag to get the value for
     * @return the value of the requested tag or null if the tag was not found in the file.
     * Null will always be returned if the scanned file is not supported.
     */
    public String getTagValueByName(String tagName) {
        for (Pair<String, String> tag : _tags) {
            if (tag.getKey().equalsIgnoreCase(tagName))
                return tag.getValue();
        }

        tagName = MetaDataNamesMapping.tryConvertFriendlyNameToFourcc(tagName);
        for (Pair<String, String> tag : _tags) {
            if (tag.getKey().equalsIgnoreCase(tagName))
                return tag.getValue();
        }

        return null;
    }

    /**
     * @return false if the format of the given file is not supported by the metadata extractor, otherwise true
     */
    public boolean isFileSupported() {
        return _isFileSupported;
    }
}
