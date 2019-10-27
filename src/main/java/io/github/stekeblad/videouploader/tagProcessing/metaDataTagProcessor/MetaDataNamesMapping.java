package io.github.stekeblad.videouploader.tagProcessing.metaDataTagProcessor;

import javafx.util.Pair;

import java.util.ArrayList;

/**
 * Class for trying to translating the codes in the iTunes metadata format to user friendly names
 */
public class MetaDataNamesMapping {
    private static ArrayList<Pair<String, String>> namesMap = new ArrayList<>();

    /**
     * @return a list of some 4-character metadata names and their friendly alternative
     */
    public static ArrayList<Pair<String, String>> getNamesMap() {
        if (namesMap.size() > 0)
            return namesMap;

        // List taken from the following link, its the best one I could find
        // http://atomicparsley.sourceforge.net/mpeg-4files.html

        namesMap.add(new Pair<>("©alb", "Album"));
        namesMap.add(new Pair<>("©art", "Artist"));
        namesMap.add(new Pair<>("aART", "AlbumArtist"));
        namesMap.add(new Pair<>("©cmt", "Comment"));
        namesMap.add(new Pair<>("©day", "Year"));
        namesMap.add(new Pair<>("©nam", "Title"));
        namesMap.add(new Pair<>("©gen", "Genre"));
        namesMap.add(new Pair<>("gnre", "Genre"));
        namesMap.add(new Pair<>("trkn", "TrackNumber"));
        namesMap.add(new Pair<>("disk", "DiskNumber"));
        namesMap.add(new Pair<>("©wrt", "Composer"));
        namesMap.add(new Pair<>("©too", "Encoder"));
        namesMap.add(new Pair<>("tmpo", "BPM"));
        namesMap.add(new Pair<>("cprt", "Copyright"));
        namesMap.add(new Pair<>("cpil", "Compilation"));
        namesMap.add(new Pair<>("covr", "Artwork"));
        namesMap.add(new Pair<>("rtng", "RatingAdvisory"));
        namesMap.add(new Pair<>("©grp", "Grouping"));
        namesMap.add(new Pair<>("stik", "stik"));
        namesMap.add(new Pair<>("pcst", "Podcast"));
        namesMap.add(new Pair<>("catg", "Category"));
        namesMap.add(new Pair<>("keyw", "Keyword"));
        namesMap.add(new Pair<>("purl", "PodcastURL"));
        namesMap.add(new Pair<>("egid", "EpisodeGlobalUniqueID"));
        namesMap.add(new Pair<>("desc", "Description"));
        namesMap.add(new Pair<>("©lyr", "Lyrics"));
        namesMap.add(new Pair<>("tvnn", "TVNetworkName"));
        namesMap.add(new Pair<>("tvsh", "TVShowName"));
        namesMap.add(new Pair<>("tven", "TVEpisodeNumber"));
        namesMap.add(new Pair<>("tvsn", "TVSeason"));
        namesMap.add(new Pair<>("tves", "TVEpisode"));
        namesMap.add(new Pair<>("purd", "PurchaseDate"));
        namesMap.add(new Pair<>("pgap", "GaplessPlayback"));

        return namesMap;
    }

    /**
     * Converts an integer to a four character string
     *
     * @param metaNumericCode an integer
     * @return the 4-character representation of the given integer
     */
    public static String convertIntToFourcc(int metaNumericCode) {
        // Taken from the following place but with a different name.
        // https://github.com/jcodec/jcodec/blob/155e0106850381a087f7359325777e3ae190e9e8/src/main/java/org/jcodec/movtool/MetadataEditorMain.java#L201
        byte[] bytes = new byte[4];
        java.nio.ByteBuffer.wrap(bytes).order(java.nio.ByteOrder.BIG_ENDIAN).putInt(metaNumericCode);
        return org.jcodec.platform.Platform.stringFromCharset(bytes, org.jcodec.platform.Platform.ISO8859_1);
    }

    /**
     * Looks up a friendly name in the MetaDataNamesMapping.NamesMap given a 4-character name
     *
     * @param fourcc a four-character string to find a friendly name for
     * @return if fourcc is not four chacters long or the code is not found then the input will be returned.
     * If a match is found in the NamesMap the friendly name in the map will be returned
     */
    public static String tryConvertFourccToFriendlyName(String fourcc) {
        if (fourcc.length() != 4)
            return fourcc;

        for (Pair<String, String> pair : getNamesMap()) {
            if (pair.getKey().equalsIgnoreCase(fourcc))
                return pair.getValue();
        }
        return fourcc;
    }

    /**
     * Looks up a 4-character name in the MetaDataNamesMapping.NamesMap given a friendly name
     *
     * @param friendlyName a friendly name to find a 4-character code for
     * @return returns a 4-chacter code for the friendly name or the given friendly name if no matching 4-character code was found.
     */
    public static String tryConvertFriendlyNameToFourcc(String friendlyName) {
        for (Pair<String, String> pair : getNamesMap()) {
            if (pair.getValue().equalsIgnoreCase(friendlyName))
                return pair.getKey();
        }
        return friendlyName;
    }

    /**
     * Combines convertIntToFourcc() and tryConvertFourccToFriendlyName() in one convenient method
     *
     * @param metaNumericCode an integer representing a metadata name
     * @return a friendly metadata name or if no friendly name found matching the input the 4-character code of the input
     */
    public static String tryConvertIntToFriendlyName(int metaNumericCode) {
        String fourcc = convertIntToFourcc(metaNumericCode);
        return tryConvertFourccToFriendlyName(fourcc);
    }
}
