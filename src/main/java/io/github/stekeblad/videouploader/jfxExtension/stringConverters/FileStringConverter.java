package io.github.stekeblad.videouploader.jfxExtension.stringConverters;

import javafx.util.StringConverter;

import java.io.File;

public class FileStringConverter extends StringConverter<File> {
    @Override
    public String toString(File object) {
        return object.getName();
    }

    @Override
    public File fromString(String string) {
        return null;
    }
}
