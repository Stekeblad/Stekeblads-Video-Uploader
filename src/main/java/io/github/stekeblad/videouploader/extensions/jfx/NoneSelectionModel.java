package io.github.stekeblad.videouploader.extensions.jfx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.MultipleSelectionModel;

/**
 * The {@link NoneSelectionModel} prevents items in a list view that uses this selection model
 * from being selected. Visually this means that the list items can never be highlighted
 * and get the "selected" background color (blue by default). In code this means that you can
 * not do stuff that requires an item in the list to be selected
 *
 * @param <T>
 */
public class NoneSelectionModel<T> extends MultipleSelectionModel<T> {
    @Override
    public ObservableList<Integer> getSelectedIndices() {
        return FXCollections.emptyObservableList();
    }

    @Override
    public ObservableList<T> getSelectedItems() {
        return FXCollections.emptyObservableList();
    }

    @Override
    public void selectIndices(int index, int... indices) {
        // select not allowed
    }

    @Override
    public void selectAll() {
        // select not allowed
    }

    @Override
    public void clearAndSelect(int index) {
        // select not allowed
    }

    @Override
    public void select(int index) {
        // select not allowed
    }

    @Override
    public void select(Object obj) {
        // select not allowed
    }

    @Override
    public void clearSelection(int index) {
        // select not allowed
    }

    @Override
    public void clearSelection() {
        // select not allowed
    }

    @Override
    public boolean isSelected(int index) {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public void selectPrevious() {
        // select not allowed
    }

    @Override
    public void selectNext() {
        // select not allowed
    }

    @Override
    public void selectFirst() {
        // select not allowed
    }

    @Override
    public void selectLast() {
        // select not allowed
    }
}
