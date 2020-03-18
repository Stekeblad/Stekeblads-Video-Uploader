package io.github.stekeblad.videouploader.youtube;

import io.github.stekeblad.videouploader.youtube.utils.VisibilityStatus;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.util.List;

/**
 * Represents a preset.
 */
public class VideoPreset extends VideoInformationBase {

    private static final String NODE_ID_PRESETNAME = "_presetName";

    private GridPane presetPane;

    public void setPresetName(String newName) {
        ((TextField) presetPane.lookup("#" + getPaneId() + NODE_ID_PRESETNAME)).setText(newName);
    }
    /**
     * @return returns the name of this preset
     */
    public String getPresetName() {
        return ((TextField) presetPane.lookup("#" + getPaneId() + NODE_ID_PRESETNAME)).getText();
    }

    /**
     * @return returns the entire UI pane for placement on screen
     */
    public GridPane getPane() {
        return presetPane;
    }

    // Inherit parent doc
    public void setEditable(boolean newEditStatus) {
        super.setEditable(newEditStatus);
        ((TextField) presetPane.lookup("#" + getPaneId() + NODE_ID_PRESETNAME)).setEditable(newEditStatus);
    }

    /**
     * Constructor for VideoPreset. There is also the VideoPreset.Builder class if that is preferred.
     * Everything set here can be edited later except the paneId
     * @param videoName Title for the the videos that uses this preset
     * @param videoDescription Description for the videos that uses this preset
     * @param visibility The visibility status that will be used for the video that uses this status
     * @param videoTags The tags that will be assigned to the videos that uses this preset
     * @param selectedPlaylist The name of the playlist that will be the selected one for the videos that uses this preset
     * @param category The name of the category to be selected  for the videos that uses this preset
     * @param tellSubs Set to true if subscribers should be notified when videos using this preset is uploaded, set to false to not notify
     * @param thumbNailPath File path to a thumbnail to use for all videos that uses this preset or null to let thumbnail be selected automatically
     * @param paneId A string used for naming all UI elements
     * @param presetName A name used to recognize this preset
     */
    public VideoPreset(String videoName, String videoDescription, VisibilityStatus visibility, List<String> videoTags,
                       String selectedPlaylist, String category, boolean tellSubs,
                       String thumbNailPath, boolean madeForKids, String paneId, String presetName) {

        super(videoName, videoDescription, visibility, videoTags, selectedPlaylist,
                category, tellSubs, thumbNailPath, madeForKids, paneId);
        makePresetPane(presetName);
    }

    /**
     * Reconstructs a VideoPreset form its string version created by calling toString()
     * @param fromString The string representation of a VideoPreset
     * @param paneId A string used for naming all UI elements
     * @throws Exception If the string could not be converted to a VideoPreset
     */
    public VideoPreset(String fromString, String paneId) throws Exception {
        super(fromString, paneId);

        String presetName = null;

        String[] lines = fromString.split("\n");
        for (String line : lines) {
            int colonIndex = line.indexOf(':');
            if (colonIndex > 0) {
                switch (line.substring(0, colonIndex)) {
                    case NODE_ID_PRESETNAME:
                        presetName = line.substring(colonIndex + 1);
                        break;
                    default:
                        // likely belong to parent
                }
            }
        }
        if (presetName == null) {
            throw new Exception("String representation of class does not have presetName");
        }
        makePresetPane(presetName);
    }

    /**
     * Creates a copy of this VideoPreset with the same or different paneId.
     * @param paneIdForCopy The paneId used for naming the nodes in the copy, use null to get the same as original.
     *                      if null is given then the original and the copy may not be able to be on screen at the same time,
     *                      the nodes will be considered to be the same and when placing the second one of them it will cause the
     *                      nodes from the first to be moved to the location of the second.
     * @return a copy of this VideoPreset
     */
    public VideoPreset copy(String paneIdForCopy) {
        if(paneIdForCopy == null) {
            paneIdForCopy = this.getPaneId();
        }
        String thumbnailPath;
        if(getThumbNail() == null) {
            thumbnailPath = null;
        } else {
            thumbnailPath = getThumbNail().getAbsolutePath();
        }
        return new VideoPreset(getVideoName(), getVideoDescription(), getVisibility(), getVideoTags(), getSelectedPlaylist(),
                getCategory(), isTellSubs(), thumbnailPath, isMadeForKids(), paneIdForCopy, getPresetName());
    }

    /**
     * Used for building a VideoPreset one attribute at the time.
     * Call build() to get a real VideoPreset when you are done setting attributes.
     */
    public static class Builder extends VideoInformationBase.Builder{
        String presetName;

        public String getPresetName() {
            return presetName;
        }

        public VideoPreset.Builder setPresetName(String presetName) {
            this.presetName = presetName;
            return this;
        }
        
        // Re-implementation of setters in super to get the right return type
        public VideoPreset.Builder setVideoName(String videoName) {
            this.videoName = videoName;
            return this;
        }

        public VideoPreset.Builder setVideoDescription(String videoDescription) {
            this.videoDescription = videoDescription;
            return this;
        }

        public VideoPreset.Builder setVisibility(VisibilityStatus visibility) {
            this.visibility = visibility;
            return this;
        }

        public VideoPreset.Builder setVideoTags(List<String> videoTags) {
            this.videoTags = videoTags;
            return this;
        }

        public VideoPreset.Builder setSelectedPlaylist(String selectedPlaylist) {
            this.selectedPlaylist = selectedPlaylist;
            return this;
        }

        public VideoPreset.Builder setCategory(String category) {
            this.category = category;
            return this;
        }

        public VideoPreset.Builder setTellSubs(boolean tellSubs) {
            this.tellSubs = tellSubs;
            return this;
        }

        public VideoPreset.Builder setThumbNailPath(String thumbNailPath) {
            this.thumbNailPath = thumbNailPath;
            return this;
        }

        public VideoPreset.Builder setMadeForKids(boolean madeForKids) {
            this.madeForKids = madeForKids;
            return this;
        }

        public VideoPreset.Builder setPaneName(String paneName) {
            this.paneName = paneName;
            return this;
        }

        public VideoPreset build() {
            return new VideoPreset(getVideoName(), getVideoDescription(), getVisibility(), getVideoTags(), getSelectedPlaylist(),
                    getCategory(), isTellSubs(), getThumbNailPath(), isMadeForKids(), getPresetName(), presetName);
        }
    }

    /**
     * Creates the UI Pane so it can be be retrieved by front end code with getPane()
     * @param name The preset name
     */
    private void makePresetPane(String name) {
        // The base class has already done most of the work
        presetPane = super.getPane();

        TextField presetName = new TextField();
        presetName.setId(getPaneId() + NODE_ID_PRESETNAME);
        presetName.setPromptText("Preset name");
        presetName.setText(name);
        presetName.setEditable(false);

        // Add new Nodes on new row at the bottom
        presetPane.add(presetName, 1, 5);
    }

    /**
     * Creates a string representation of the class that can be saved and later used to recreate the class as it
     * looked like before with the VideoPreset(String, String) constructor
     * @return A String representation of this class
     */
    public String toString() {
        return super.toString() + "\n" +
                NODE_ID_PRESETNAME + ":" + getPresetName();
    }
}
