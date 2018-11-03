package io.github.stekeblad.videouploader.utils;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

/**
 * Different fileChoosers for different types of files
 */
public class FileUtils {

    private static final String VIDEO_FILE_FORMAT = "video/";

    /**
     * Thumbnail chooser dialog. Only allows files of the specified types
     * to be selected and with a configurable max allowed file size
     * Displays a non-blocking dialog if the selected file is to large, showing the max allowed size and the size of the
     * selected file, and returns null.
     *
     * @param allowedFormats a List of allowed file formats or null for all file formats
     * @param maxFileSize The max allowed file size in bytes, for no limit pass Long.Max_VALUE
     * @return A file object for the selected file, null if no file is selected or the selected file is to large.
     * @throws IllegalArgumentException if maxFileSize is less than or equal to zero
     */
    public static File pickThumbnail(List<String> allowedFormats, long maxFileSize) {
        if (maxFileSize < 1)
            throw new IllegalArgumentException("Max file size can't be a negative value or zero");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose a thumbnail");
        if (allowedFormats != null) {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", allowedFormats));
            fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Image Files", allowedFormats));
        }
        Stage fileChooserStage = new Stage();
        File thumbnail = fileChooser.showOpenDialog(fileChooserStage);
        if (thumbnail != null) {
            if (thumbnail.length() > maxFileSize) { // max allowed size is 2MB
                AlertUtils.simpleClose("Warning", "Image to large, the max size is " +
                        BigDecimal.valueOf((double) maxFileSize / (1024 * 1024)).setScale(3, RoundingMode.HALF_UP) + "MB" +
                        "\n the chosen file is " + BigDecimal.valueOf((double) thumbnail.length() / (1024 * 1024)).setScale(3, RoundingMode.HALF_UP) + "MB").show();
                return null;
            }
            return thumbnail;
        }
        return null;
    }

    /**
     * Video file chooser dialog. Allows multiple files to be selected and filters out all files witch does not have a mimeType
     * of "video/*". If one or more files was filtered out a non-blocking dialog is displayed telling some files was ignored and why.
     * (to large or invalid file type)
     *
     * @param maxFileSize The max allowed file size in bytes, for no limit pass Long.Max_VALUE
     * @return A List of File with all files that was select and not filtered out.
     * @throws IllegalArgumentException if maxFileSize is less than or equal to zero
     */
    public static List<File> pickVideos(long maxFileSize) {
        if (maxFileSize < 1)
            throw new IllegalArgumentException("Max file size can't be a negative value or zero");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose video files to upload");
        Stage fileChooserStage = new Stage();
        List<File> chosenFiles = fileChooser.showOpenMultipleDialog(fileChooserStage);
        List<File> filesToUpload = new ArrayList<>();
        if (chosenFiles != null) {
            List<Pair<String, String>> skippedFiles = new ArrayList<>();
            for (File chosenFile : chosenFiles) {
                try { // Check file MIME to see if it is a video file
                    String contentType = Files.probeContentType(Paths.get(chosenFile.toURI()));
                    if (contentType == null || !contentType.startsWith(VIDEO_FILE_FORMAT)) {
                        skippedFiles.add(new Pair<>(chosenFile.getName(), "Invalid file type"));
                    } else if (chosenFile.length() > maxFileSize) {
                        skippedFiles.add(new Pair<>(chosenFile.getName(), "Too large"));
                    } else {
                        filesToUpload.add(chosenFile);
                    }
                } catch (Exception e) {
                    skippedFiles.add(new Pair<>(chosenFile.getName(), "Could not read file"));
                }
            }
            if (!skippedFiles.isEmpty()) {
                StringBuilder errorString = new StringBuilder("One or more of the selected files was not added. " +
                        "It may have failed because only video files is allowed");
                if (maxFileSize != Long.MAX_VALUE)
                    errorString.append(", the max allowed file size is ")
                            .append(BigDecimal.valueOf((double) maxFileSize / (1024 * 1024)).setScale(3, RoundingMode.HALF_UP))
                            .append("MB");
                errorString.append(" or they could not be read.");
                for (Pair<String, String> file : skippedFiles) {
                    errorString.append(System.lineSeparator()).append(file.getKey()).append(" - ").append(file.getValue());
                }
                AlertUtils.simpleClose_longContent("Invalid files", errorString.toString());
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