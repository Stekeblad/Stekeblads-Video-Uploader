package io.github.stekeblad.videouploader.models;

import io.github.stekeblad.videouploader.extensions.jfx.objectProperties.LocalCategoryObjectProperty;
import io.github.stekeblad.videouploader.extensions.jfx.objectProperties.LocalPlaylistObjectProperty;
import io.github.stekeblad.videouploader.extensions.jfx.objectProperties.VisibilityStatusObjectProperty;
import io.github.stekeblad.videouploader.youtube.LocalCategory;
import io.github.stekeblad.videouploader.youtube.LocalPlaylist;
import io.github.stekeblad.videouploader.youtube.VisibilityStatus;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.UUID;

public class NewVideoInfoBaseModel {
    private final UUID uniqueId = UUID.randomUUID();

    private final StringProperty videoName = new SimpleStringProperty();
    private final StringProperty videoDescription = new SimpleStringProperty();
    private final VisibilityStatusObjectProperty visibility = new VisibilityStatusObjectProperty();
    private final ListProperty<String> videoTags = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final LocalPlaylistObjectProperty selectedPlaylist = new LocalPlaylistObjectProperty();
    private final LocalCategoryObjectProperty selectedCategory = new LocalCategoryObjectProperty();
    private final BooleanProperty tellSubs = new SimpleBooleanProperty();
    private final StringProperty thumbnailPath = new SimpleStringProperty();
    private final BooleanProperty madeForKids = new SimpleBooleanProperty();

    public UUID getUniqueId() {
        return uniqueId;
    }

    public String getVideoName() {
        return videoName.get();
    }

    public StringProperty videoNameProperty() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName.set(videoName);
    }

    public String getVideoDescription() {
        return videoDescription.get();
    }

    public StringProperty videoDescriptionProperty() {
        return videoDescription;
    }

    public void setVideoDescription(String videoDescription) {
        this.videoDescription.set(videoDescription);
    }

    public VisibilityStatus getVisibility() {
        return visibility.get();
    }

    public ObjectProperty<VisibilityStatus> visibilityProperty() {
        return visibility;
    }

    public void setVisibility(VisibilityStatus visibility) {
        this.visibility.set(visibility);
    }

    public ObservableList<String> getVideoTags() {
        return videoTags.get();
    }

    public ListProperty<String> videoTagsProperty() {
        return videoTags;
    }

    public void setVideoTags(List<String> videoTags) {
        this.videoTags.clear();
        this.videoTags.addAll(videoTags);
    }

    public LocalPlaylist getSelectedPlaylist() {
        return selectedPlaylist.get();
    }

    public ObjectProperty<LocalPlaylist> selectedPlaylistProperty() {
        return selectedPlaylist;
    }

    public void setSelectedPlaylist(LocalPlaylist selectedPlaylist) {
        this.selectedPlaylist.set(selectedPlaylist);
    }

    public LocalCategory getSelectedCategory() {
        return selectedCategory.get();
    }

    public ObjectProperty<LocalCategory> selectedCategoryProperty() {
        return selectedCategory;
    }

    public void setSelectedCategory(LocalCategory selectedCategory) {
        this.selectedCategory.set(selectedCategory);
    }

    public boolean isTellSubs() {
        return tellSubs.get();
    }

    public BooleanProperty tellSubsProperty() {
        return tellSubs;
    }

    public void setTellSubs(boolean tellSubs) {
        this.tellSubs.set(tellSubs);
    }

    public String getThumbnailPath() {
        return thumbnailPath.get();
    }

    public StringProperty thumbnailPathProperty() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath.set(thumbnailPath);
    }

    public boolean isMadeForKids() {
        return madeForKids.get();
    }

    public BooleanProperty madeForKidsProperty() {
        return madeForKids;
    }

    public void setMadeForKids(boolean madeForKids) {
        this.madeForKids.set(madeForKids);
    }
}
