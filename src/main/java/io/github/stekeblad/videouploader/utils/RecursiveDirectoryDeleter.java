package io.github.stekeblad.videouploader.utils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.CONTINUE;

/**
 * Used together with Files.walkFileTree() to recursively delete all files and directories in the directory provided
 * as the Path. Skips files and directories that could not be deleted. Does also delete the directory given.
 * <p>
 * Example usage: Files.walkFileTree(new File(DIR_TO_DELETE).toPath(), new RecursiveDirectoryDeleter());
 */
public class RecursiveDirectoryDeleter extends SimpleFileVisitor<Path> {

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
        // If file is a file (not symlic, directory something else), attempt to delete it
        if (attr.isRegularFile()) {
            try {
                Files.delete(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException e) {
        // Attempt to delete directory after everything in it has been visited
        if (e != null) {
            e.printStackTrace();
            return CONTINUE;
        }
        try {
            Files.delete(dir);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException e) {
        // print the error that occurred
        e.printStackTrace();
        return CONTINUE;
    }
}
