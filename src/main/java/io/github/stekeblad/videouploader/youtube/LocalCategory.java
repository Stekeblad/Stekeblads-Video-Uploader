package io.github.stekeblad.videouploader.youtube;

import org.jetbrains.annotations.NotNull;

public class LocalCategory implements Comparable<LocalCategory> {
    private String id;
    private String name;

    public LocalCategory(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(@NotNull LocalCategory other) {
        return this.name.compareTo(other.getName());
    }
}
