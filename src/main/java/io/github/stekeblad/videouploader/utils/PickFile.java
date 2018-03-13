package io.github.stekeblad.videouploader.utils;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static io.github.stekeblad.videouploader.youtube.VideoInformationBase.THUMBNAIL_FILE_FORMAT;
import static io.github.stekeblad.videouploader.youtube.VideoUpload.VIDEO_FILE_FORMAT;

/**
 * Different fileChoosers for different types of files
 */
public class PickFile {

    /**
     * Thumbnail chooser dialog. Only allows files of the types
     * io.github.stekeblad.videouploader.youtube.VideoInformationBase.THUMBNAIL_FILE_FORMAT
     * to be selected and with a max allowed size of 2MB.
     * Displays a non-blocking dialog explaining the error if the selected file is to large and returns null.
     * @return A file object for the selected file, null if no file is selected or the selected file is to large. (2MB +)
     */
    public static File pickThumbnail() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose a thumbnail");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", THUMBNAIL_FILE_FORMAT));
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Image Files", THUMBNAIL_FILE_FORMAT));
        Stage fileChooserStage = new Stage();
        File thumbnail = fileChooser.showOpenDialog(fileChooserStage);
        if (thumbnail != null) {
            if(thumbnail.length() > 2 *1024 * 1024) { // max allowed size is 2MB
                AlertUtils.simpleClose("Warning", "Image to large, YouTube do not allow thumbnails larger then 2 MB." +
                        "\n the chosen file is " + BigDecimal.valueOf((double) thumbnail.length() /(1024 * 1024)).setScale(3, BigDecimal.ROUND_HALF_UP) + "MB").show();
                return null;
            }
            return thumbnail;
        }
        return null;
    }

    /**
     * Video file chooser dialog. Allows multiple files to be selected and filters out all files witch does not have a mimeType
     * of "video/*". If one or more files was filtered out a non-blocking dialog is displayed telling some files was ignored.
     * @return A List of File with all files that was select and not filtered out.
     */
    public static List<File> pickVideos() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose video files to upload");
        Stage fileChooserStage = new Stage();
        List<File> chosenFiles = fileChooser.showOpenMultipleDialog(fileChooserStage);
        List<File> filesToUpload = new ArrayList<>();
        if (chosenFiles != null) {
            boolean fileWasSkipped = false;
            for (File chosenFile : chosenFiles) {
                try { // Check file MIME to see if it is a video file
                    String contentType = Files.probeContentType(Paths.get(chosenFile.toURI()));
                    if (contentType == null || !contentType.startsWith(VIDEO_FILE_FORMAT)) {
                        fileWasSkipped = true; // at leased one selected file is not a video file
                    } else {
                        filesToUpload.add(chosenFile);
                    }
                } catch (Exception e) {
                    fileWasSkipped = true;
                }
            }
            if (fileWasSkipped) {
                AlertUtils.simpleClose("Invalid files",
                        "One or more of the selected files was not added because they are not video files").show();
            }
        }
        return filesToUpload;
    }
}
