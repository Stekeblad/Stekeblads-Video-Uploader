package io.github.stekeblad.videouploader.utils.state;

import io.github.stekeblad.videouploader.youtube.VideoInformationBase;
import javafx.scene.control.Button;

/**
 * VideoUploadState exists to reduce the number of lines required to change the buttons
 * for VideoUploads in MainWindowController. Instead of (in every method that changes the buttons) create new buttons,
 * setting Ids and method to call -- what buttons to display is defined in the beginning and in the code for the buttons
 * you just say witch set of buttons to use.
 */
public class VideoUploadState {
    private ButtonProperties[] lockedButtonProperties;
    private ButtonProperties[] editingButtonProperties;
    private ButtonProperties[] uploadingButtonProperties;
    private ButtonProperties[] failedButtonProperties;


    /**
     * Defines the properties of the buttons to be set when setLocked() is called
     *
     * @param properties ButtonProperties[] of length 3 with id and text for each button.
     *                   If the onClick property of ButtonProperties is null that button will be invisible when drawn
     *                   by setLocked, the text will then be as a spacer/separator
     */
    public void defineLocked(ButtonProperties[] properties) {
        lockedButtonProperties = properties;
    }

    /**
     * See defineLocked
     */
    public void defineEditing(ButtonProperties[] properties) {
        editingButtonProperties = properties;
    }

    /** See defineLocked */
    public void defineUploading(ButtonProperties[] properties) {
        uploadingButtonProperties = properties;
    }

    /** See defineLocked */
    public void defineFailed(ButtonProperties[] properties) {
        failedButtonProperties = properties;
    }

    /**
     * Sets the buttons for node using the information provided in defineLocked. If defineLocked has not properly been
     * called, Exceptions will be thrown as this is not checked by this method
     *
     * @param node the node to change the buttons on
     */
    public void setLocked(VideoInformationBase node) {
        ButtonProperties p1 = lockedButtonProperties[0];
        ButtonProperties p2 = lockedButtonProperties[1];
        ButtonProperties p3 = lockedButtonProperties[2];
        set(node, p1, p2, p3);
    }

    /** See setLocked */
    public void setEditing(VideoInformationBase node) {
        ButtonProperties p1 = editingButtonProperties[0];
        ButtonProperties p2 = editingButtonProperties[1];
        ButtonProperties p3 = editingButtonProperties[2];
        set(node, p1, p2, p3);
    }

    /** See setLocked */
    public void setUploading(VideoInformationBase node) {
        ButtonProperties p1 = uploadingButtonProperties[0];
        ButtonProperties p2 = uploadingButtonProperties[1];
        ButtonProperties p3 = uploadingButtonProperties[2];
        set(node, p1, p2, p3);
    }

    /** See setLocked */
    public void setFailed(VideoInformationBase node) {
        ButtonProperties p1 = failedButtonProperties[0];
        ButtonProperties p2 = failedButtonProperties[1];
        ButtonProperties p3 = failedButtonProperties[2];
        set(node, p1, p2, p3);
    }

    /**
     * Common private method for all set-methods
     */
    private void set(VideoInformationBase node, ButtonProperties p1, ButtonProperties p2, ButtonProperties p3) {
        String familyId = node.getPaneId();

        Button b1 = new Button(p1.getText());
        b1.setId(familyId + p1.getId());
        if (p1.getOnClick() != null) {
            b1.setOnMouseClicked(event -> p1.getOnClick().accept(b1.getId()));
        } else {
            b1.setVisible(false);
        }

        Button b2 = new Button(p2.getText());
        b2.setId(familyId + p2.getId());
        if (p2.getOnClick() != null) {
            b2.setOnMouseClicked(event -> p2.getOnClick().accept(b2.getId()));
        } else {
            b2.setVisible(false);
        }

        Button b3 = new Button(p3.getText());
        b3.setId(familyId + p3.getId());
        if (p3.getOnClick() != null) {
            b3.setOnMouseClicked(event -> p3.getOnClick().accept(b3.getId()));
        } else {
            b3.setVisible(false);
        }

        node.setButton1(b1);
        node.setButton2(b2);
        node.setButton3(b3);
    }
}
