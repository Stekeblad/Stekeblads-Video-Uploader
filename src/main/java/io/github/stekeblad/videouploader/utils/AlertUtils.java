package io.github.stekeblad.videouploader.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;

import java.util.Optional;

/**
 * A set of prepared Alert dialogs that can easy be created and used.
 * Several properties is already defined you just need to provide the text to be displayed.
 */
public class AlertUtils {

    /**
     * Convenient method for creating a @code{Alert} dialog with a single CLOSE button
     * @param header Header to display in window title
     * @param content The message to display to the user
     * @return a Alert ready to display or continue to modify
     */
    public static Alert simpleClose(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setTitle(header);
        alert.setContentText(content);
        alert.getButtonTypes().setAll(ButtonType.CLOSE);
        return alert;
    }

    /**
     * Convenient method for creating a @code{Alert} dialog with a YES and a NO button.
     * To set what to do on the buttons you can do like this:
     * <pre>{@code
     * Optional<ButtonType> buttonChoice = alert.showAndWait();
     * if (buttonChoice.isPresent()) {
     *     if (buttonChoice.get() == ButtonType.YES) {
     *         doOnYes();
     *     } else { // ButtonType.NO or Closed with [X] button
     *         doOnNo();
     *     }
     * }
     *
     * @param header Header to display in window title
     * @param content The message to display to the user
     * @return a Alert that needs actions bound to the buttons but is otherwise ready to be shown
     */
    public static Alert yesNo(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setTitle(header);
        alert.setContentText(content);
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        return alert;
    }

    /**
     * A Dialog with three button where you define not only the header and content text but also the text on the buttons
     * @param header Header to display in window title
     * @param content The message to display to the user
     * @param btn1Text The text on the first button
     * @param btn2Text The text on the second button
     * @param btn3Text The text on the third button
     * @return the text on the button that was clicked or null if no button result is available
     */
    public static String threeButtons(String header, String content, String btn1Text, String btn2Text, String btn3Text) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setTitle(header);
        alert.setContentText(content);
        alert.getButtonTypes().addAll(new ButtonType(btn1Text), new ButtonType(btn2Text), new ButtonType(btn3Text));
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            return result.get().getText();
        } else {
            return null;
        }
    }
}
