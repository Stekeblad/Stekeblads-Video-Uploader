package io.github.stekeblad.videouploader.ListControllers;

import io.github.stekeblad.videouploader.utils.background.OpenInBrowser;
import io.github.stekeblad.videouploader.utils.translation.TranslationBundles;
import io.github.stekeblad.videouploader.utils.translation.Translations;
import io.github.stekeblad.videouploader.utils.translation.TranslationsManager;
import io.github.stekeblad.videouploader.youtube.LocalPlaylist;
import javafx.geometry.Side;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.GridPane;

public class PlaylistItemController {
    // FX nodes
    public CheckBox checkbox;
    protected GridPane innerPane;

    protected LocalPlaylist model;

    private static final String NODE_ID_CHECKBOX = "presetName";

    public PlaylistItemController(LocalPlaylist playlist) {
        model = playlist;
        innerPane = new GridPane();
        checkbox = new CheckBox(playlist.getName());
        checkbox.setId(NODE_ID_CHECKBOX);
        checkbox.setSelected(playlist.isVisible());

        Translations transPlaylistWindow = TranslationsManager.getTranslation(TranslationBundles.WINDOW_PLAYLIST);
        ContextMenu playlistContext = new ContextMenu();
        MenuItem item1 = new MenuItem(transPlaylistWindow.getString("viewOnYouTube"));
        item1.setOnAction(event -> OpenInBrowser.openInBrowser(playlist.playlistUrl()));
        playlistContext.getItems().add(item1);
        checkbox.setOnContextMenuRequested(event -> playlistContext.show(checkbox, Side.LEFT, 250, 0));

    }
}
