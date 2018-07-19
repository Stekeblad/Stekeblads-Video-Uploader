package io.github.stekeblad.videouploader.utils;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static io.github.stekeblad.videouploader.youtube.VideoInformationBase.THUMBNAIL_FILE_FORMAT;
import static io.github.stekeblad.videouploader.youtube.VideoUpload.VIDEO_FILE_FORMAT;

/**
 * Different fileChoosers for different types of files
 */
public class FileUtils {

    /**
     * Thumbnail chooser dialog. Only allows files of the types
     * io.github.stekeblad.videouploader.youtube.VideoInformationBase.THUMBNAIL_FILE_FORMAT
     * to be selected and with a max allowed size of 2MB.
     * Displays a non-blocking dialog explaining the error if the selected file is to large and returns null.
     *
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
            if (thumbnail.length() > 2 * 1024 * 1024) { // max allowed size is 2MB
                AlertUtils.simpleClose("Warning", "Image to large, YouTube do not allow thumbnails larger then 2 MB." +
                        "\n the chosen file is " + BigDecimal.valueOf((double) thumbnail.length() / (1024 * 1024)).setScale(3, BigDecimal.ROUND_HALF_UP) + "MB").show();
                return null;
            }
            return thumbnail;
        }
        return null;
    }

    /**
     * Video file chooser dialog. Allows multiple files to be selected and filters out all files witch does not have a mimeType
     * of "video/*". If one or more files was filtered out a non-blocking dialog is displayed telling some files was ignored.
     *
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

    /**
     * Returns a list of names of all files and directories in a resource directory (not recursive)
     * Most important part of this method is that it needs to be done in different ways if the program is executed
     * from a IDE compared to running it as a jar.
     *
     * @param path path to the directory to list the content of.
     * @return all files and directories at the given path as {@code List<String>} or null if something erred / the path is invalid
     */
    public static List<String> getContentOfResourceDir(String path) {
        File isThisJar = null;
        try {
            isThisJar = new File(FileUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            System.err.println("URI Syntax Exception");
            e.printStackTrace();
        }
        if (isThisJar != null && isThisJar.isFile()) { // if true, this is jar!
            try {
                JarFile jar = new JarFile(isThisJar);
                ArrayList<String> matches = new ArrayList<>();
                Enumeration<JarEntry> allEntries = jar.entries();
                while (allEntries.hasMoreElements()) {
                    JarEntry testEntry = allEntries.nextElement();
                    try {
                        // matches path and after that does not have a / before the last character,
                        // that is, list only children and not grandchildren
                        if (testEntry.getName().startsWith(path)) {
                            String partialFilename = testEntry.getName().substring(path.length() + 1, testEntry.getName().length() - 2);
                            if (!partialFilename.contains("/")) {
                                String entryName = testEntry.getName().substring(path.length());
                                entryName = entryName.replace("/", "");
                                matches.add(entryName);
                            }
                        }
                    } catch (StringIndexOutOfBoundsException e) {
                        // The entry that exactly matches path will throw a StringOutOfBoundsException
                        // This entry should not be returned so lets just continue
                        continue;
                    }

                }
                jar.close();
                return matches;
            } catch (IOException e) {
                System.err.println("IO Exception");
                e.printStackTrace();
            }
        } else { // not jar
            URL url = FileUtils.class.getClassLoader().getResource(path);
            if (url != null) {
                try {
                    File directory = new File(url.toURI());
                    String[] files = directory.list();
                    if (files != null) {
                        return Arrays.asList(files);
                    }
                } catch (URISyntaxException e) {
                    System.err.println("URI Syntax Exception");
                    e.printStackTrace();
                }
            } else {
                System.err.println("url is null");
            }
        }
        return null;
    }
}