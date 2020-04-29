package io.github.stekeblad.videouploader.utils;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Contains different fileChoosers for different types of files (pick*), methods for directory listing,
 * reading/writing/deleting files...
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
            if (thumbnail.length() > maxFileSize) {
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
                String result = validateVideoChoice(chosenFile, maxFileSize);
                if (result == null) {
                    filesToUpload.add(chosenFile);
                } else {
                    skippedFiles.add(new Pair<>(chosenFile.getName(), result));
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
     * Video file chooser dialog. Allows only a single file to be selected and only allow files witch do have a mimeType
     * of "video/*" and are smaller than maxFileSize. Invalid choice throws exception.
     *
     * @param maxFileSize The max allowed file size in bytes, for no limit pass Long.Max_VALUE
     * @return A File if the user made a valid choice, returns null if no file was selected
     * @throws Exception                if user made an invalid choice with a message of how it was invalid
     * @throws IllegalArgumentException if maxFileSize is less than or equal to zero
     */
    public static File pickVideo(long maxFileSize) throws Exception {
        if (maxFileSize < 1)
            throw new IllegalArgumentException("Max file size can't be a negative value or zero");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose one video file");
        Stage fileChooserStage = new Stage();
        File chosenFile = fileChooser.showOpenDialog(fileChooserStage);

        if (chosenFile == null)
            return null;

        String validationResult = validateVideoChoice(chosenFile, maxFileSize);
        if (validationResult != null)
            throw new Exception(validationResult);

        return chosenFile;
    }

    public static boolean isProductionMode() {
        File isThisJar;
        try {
            isThisJar = new File(FileUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException ignored) {
            // assume its production mode
            return true;
        }
        return isThisJar.isFile();
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

    /**
     * Attempts to read an entire file and returns a list with one line from the file per item.
     * This method makes sure the file stream is closed even if an IO Exception is thrown.
     *
     * @param path path of the file to read
     * @return An ArrayList with strings where each item in the list represents one line in the file
     * @throws IOException          If the file at the given path does not exist or if an IO Exception occurs
     * @throws NullPointerException if path is null
     */
    public static ArrayList<String> readAllLines(String path) throws IOException {
        BufferedReader reader = null;
        ArrayList<String> lines = new ArrayList<>();
        try {
            reader = new BufferedReader(new FileReader(new File(path)));
            String line = reader.readLine();
            while (line != null) { // while not end of file
                lines.add(line);
                line = reader.readLine();
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return lines;
    }

    /**
     * Attempts to read an entire file and returns it as a long string.
     * This method makes sure the file stream is closed even if an IO Exception is thrown.
     *
     * @param path path of the file to read
     * @return A string with the entire content of the file at the given path
     * @throws IOException          If the file at the given path does not exist or if an IO Exception occurs
     * @throws NullPointerException if path is null
     */
    public static String readAll(String path) throws IOException {
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        try {
            reader = new BufferedReader(new FileReader(new File(path)));
            String line = reader.readLine();
            while (line != null) { // while not end of file
                builder.append(line);
                line = reader.readLine();
                if (line != null) {
                    builder.append("\n"); // do not end the last line with '\n'
                }
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return builder.toString();
    }

    /**
     * Writes data to a file. This method makes sure the file stream is closed even if an IO Exception occurs
     *
     * @param path path of the file to write
     * @param data the data to write to the file
     * @throws IOException           If the file does not exist and cannot be created, the file can not be read
     *                               or if the path points to a directory instead of a file.
     * @throws FileNotFoundException if path is null
     */
    public static void writeAll(String path, String data) throws IOException {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(new File(path)));
            writer.write(data);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Checks if a file is a video and is not larger than size limit
     *
     * @param file    file to check to see if it is a video
     * @param maxSize max allowed file size (bytes)
     * @return null if the file passed the check, otherwise a error message
     */
    private static String validateVideoChoice(File file, long maxSize) {
        try {
            String contentType = Files.probeContentType(Paths.get(file.toURI()));
            if (contentType == null || !contentType.startsWith(VIDEO_FILE_FORMAT)) {
                return "Invalid file type";
            } else if (file.length() > maxSize) {
                return "Too large";
            } else {
                return null;
            }
        } catch (Exception e) {
            return "Could not read file";
        }
    }

    /**
     * Reads all lines from a files, works even for resources inside a jar!
     *
     * @param uri The URI to the file to read
     * @return a list with all lines of the targeted file
     * @throws IOException for all types of IO Exception :P (failed to access jar and file not found for example)
     */
    public static List<String> getAllResourceLines(URI uri) throws IOException {
        try {
            // Try reading it normally
            Path path = Paths.get(uri);
            return Files.readAllLines(path);
        } catch (FileSystemNotFoundException ex) {
            // Failed, file is most likely inside a jar
            try (FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
                Path path = fs.provider().getPath(uri);
                // Read file first and then auto-close the jar-file-system (try-with-resource)
                return Files.readAllLines(path);
            }
        }

    }
}